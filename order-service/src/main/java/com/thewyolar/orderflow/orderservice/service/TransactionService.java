package com.thewyolar.orderflow.orderservice.service;

import com.thewyolar.orderflow.orderservice.dto.OrderStatusResponseDTO;
import com.thewyolar.orderflow.orderservice.dto.PaymentDTO;
import com.thewyolar.orderflow.orderservice.dto.PaymentResponseDTO;
import com.thewyolar.orderflow.orderservice.model.Order;
import com.thewyolar.orderflow.orderservice.model.Transaction;
import com.thewyolar.orderflow.orderservice.repository.OrderRepository;
import com.thewyolar.orderflow.orderservice.repository.TransactionRepository;
import com.thewyolar.orderflow.orderservice.util.*;
import org.modelmapper.ModelMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TransactionService {
    private final OrderRepository orderRepository;
    private final TransactionRepository transactionRepository;
    private ModelMapper modelMapper;
    private RSAEncryptor encryptor;
    private KafkaTemplate<Long, PaymentResponseDTO> kafkaTemplate;
    private RedisTemplate<String, String> redisTemplate;

    public TransactionService(OrderRepository orderRepository, TransactionRepository transactionRepository, ModelMapper modelMapper, KafkaTemplate<Long, PaymentResponseDTO> kafkaTemplate, RedisTemplate<String, String> redisTemplate) {
        this.orderRepository = orderRepository;
        this.transactionRepository = transactionRepository;
        this.modelMapper = modelMapper;
        this.kafkaTemplate = kafkaTemplate;
        this.redisTemplate = redisTemplate;
        this.encryptor = new RSAEncryptor();
    }

    public OrderStatusResponseDTO getOrderStatus(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        OrderStatusResponseDTO response = new OrderStatusResponseDTO();
        response.setOrderId(order.getId());
        response.setName(order.getName());
        response.setAmount(order.getAmount());
        response.setCurrency(order.getCurrency());
        response.setDateCreate(order.getDateCreate());
        response.setDateUpdate(order.getDateUpdate());
        response.setStatus(order.getStatus());

        return response;
    }

    public PaymentResponseDTO makePayment(PaymentDTO paymentDTO) {
        Order order = orderRepository.findById(paymentDTO.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order not found"));

        // Шифруем номер карты и CVV-код
        String encryptedCardNumber = encryptor.encrypt(paymentDTO.getCardNumber());
        String encryptedCvv = encryptor.encrypt(paymentDTO.getCvvCode());

        // Создаем транзакцию и сохраняем зашифрованные данные в контексте
        Transaction transaction = new Transaction();
        transaction.setOrder(order);
        transaction.setMerchant(order.getMerchant());
        transaction.setAmount(paymentDTO.getAmount());
        transaction.setCurrency(paymentDTO.getCurrency());
        transaction.setDateCreate(LocalDateTime.now());
        transaction.setDateUpdate(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.NEW);
        transaction.setType(TransactionType.PAYMENT);
        transaction.setContext(new TransactionContext(encryptedCardNumber, encryptedCvv, paymentDTO.getCardExpirationDate()));
        transactionRepository.save(transaction);

        // Сохраняем расшифрованные данные в Redis
        String cardNumberKey = "cardNumber:" + transaction.getId();
        String cvvKey = "cvv:" + transaction.getId();
        redisTemplate.opsForValue().set(cardNumberKey, paymentDTO.getCardNumber(), 20, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(cvvKey, paymentDTO.getCvvCode(), 20, TimeUnit.MINUTES);

        // обновляем статус заказа
        order.setStatus(OrderStatus.PAID);
        order.setDateUpdate(OffsetDateTime.now());
        orderRepository.save(order);

        // формируем ответ
        PaymentResponseDTO paymentResponseDTO = new PaymentResponseDTO();
        paymentResponseDTO.setTransactionId(transaction.getId());
        paymentResponseDTO.setOrderId(order.getId());
        paymentResponseDTO.setAmount(transaction.getAmount());
        paymentResponseDTO.setCurrency(transaction.getCurrency());
        paymentResponseDTO.setDateCreate(transaction.getDateCreate());
        paymentResponseDTO.setDateUpdate(transaction.getDateUpdate());
        paymentResponseDTO.setStatus(order.getStatus());

        kafkaTemplate.send("new_transactions", paymentResponseDTO);

        return paymentResponseDTO;
    }

    @KafkaListener(topics = "new_transactions")
    public void processNewTransaction(PaymentResponseDTO paymentResponseDTO) {
        // найдем транзакцию в БД
        Transaction savedTransaction = transactionRepository.findById(paymentResponseDTO.getTransactionId()).orElse(null);
        if (savedTransaction == null) {
            // обработка ошибки
        }

        // Получаем контекст из транзакции и расшифровываем карточные данные
        TransactionContext transactionContext = savedTransaction.getContext();
        String decryptedCardNumber = encryptor.decrypt(transactionContext.getCardNumber());
        String decryptedCvv = encryptor.decrypt(transactionContext.getCvv());

        // Сохраняем расшифрованные данные в Redis
//        String key = "transaction:" + savedTransaction.getId();
//        redisTemplate.opsForHash().put(key, "cardNumber", decryptedCardNumber);
//        redisTemplate.opsForHash().put(key, "cvv", decryptedCvv);
//        redisTemplate.expire(key, 20, TimeUnit.MINUTES);

        // Сохраняем расшифрованные данные карты в Redis
//        redisTemplate.opsForValue().set("cardNumber:" + savedTransaction.getId(), decryptedCardNumber, 20, TimeUnit.MINUTES);
//        redisTemplate.opsForValue().set("cvv:" + savedTransaction.getId(), decryptedCvv, 20, TimeUnit.MINUTES);
//        redisTemplate.opsForValue().set("cardExpirationDate:" + savedTransaction.getId(), transactionContext.getCardExpirationDate(), 20, TimeUnit.MINUTES);

        // проверяем номер карты алгоритмом LUNA
        if (!isLuhnValid(decryptedCardNumber)) {
            savedTransaction.setStatus(TransactionStatus.DECLINED);
            transactionRepository.save(savedTransaction);
        } else {
            String cardType = getCardType(decryptedCardNumber);
            if (cardType.equals("VISA")) {
                // отклоняем транзакцию
                savedTransaction.setStatus(TransactionStatus.DECLINED);
                transactionRepository.save(savedTransaction);
            } else if (cardType.equals("MasterCard") || cardType.equals("MIR")) {
                Order order = savedTransaction.getOrder();
                if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.PARTIAL_REFUNDED) {
                    // отклоняем транзакцию
                    savedTransaction.setStatus(TransactionStatus.DECLINED);
                    transactionRepository.save(savedTransaction);
                } else if (order.getStatus() == OrderStatus.NEW || order.getStatus() == OrderStatus.PARTIAL_PAID) {
                    List<Transaction> transactions = transactionRepository.findByOrderAndStatus(order, TransactionStatus.COMPLETE);
                    Double totalAmount = null;
                    for (Transaction transaction : transactions) {
                        totalAmount += transaction.getAmount();
                    }
                    if (totalAmount.compareTo(order.getAmount()) > 0) {
                        // отклоняем транзакцию
                        savedTransaction.setStatus(TransactionStatus.DECLINED);
                        transactionRepository.save(savedTransaction);
                    } else {
                        // обновляем статус ордера на PAID
                        order.setStatus(OrderStatus.PAID);
                        orderRepository.save(order);
                    }
                }
            }
        }
    }

    private boolean isLuhnValid(String value) {
        int sum = Character.getNumericValue(value.charAt(value.length() - 1));
        int parity = value.length() % 2;
        for (int i = value.length() - 2; i >= 0; i--) {
            int summand = Character.getNumericValue(value.charAt(i));
            if (i % 2 == parity) {
                int product = summand * 2;
                summand = (product > 9) ? (product - 9) : product;
            }
            sum += summand;
        }
        return (sum % 10) == 0;
    }

    private String getCardType(String cardNumber) {
        String cardType = null;
        if (cardNumber.startsWith("4")) {
            cardType = "Visa";
        } else if (cardNumber.startsWith("5")) {
            int secondDigit = Integer.parseInt(cardNumber.substring(1, 2));
            if (secondDigit >= 1 && secondDigit <= 5) {
                cardType = "Mastercard";
            }
        } else if (cardNumber.startsWith("2")) {
            int secondDigit = Integer.parseInt(cardNumber.substring(1, 2));
            if (secondDigit == 2 || secondDigit == 4) {
                cardType = "MIR";
            }
        }
        return cardType;
    }
}
