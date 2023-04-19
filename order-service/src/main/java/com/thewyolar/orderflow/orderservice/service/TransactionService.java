package com.thewyolar.orderflow.orderservice.service;

import com.thewyolar.orderflow.orderservice.dto.OrderStatusResponseDTO;
import com.thewyolar.orderflow.orderservice.dto.PaymentDTO;
import com.thewyolar.orderflow.orderservice.dto.PaymentResponseDTO;
import com.thewyolar.orderflow.orderservice.service.mapper.OrderMapper;
import com.thewyolar.orderflow.orderservice.service.mapper.TransactionMapper;
import com.thewyolar.orderflow.orderservice.model.Order;
import com.thewyolar.orderflow.orderservice.model.Transaction;
import com.thewyolar.orderflow.orderservice.repository.OrderRepository;
import com.thewyolar.orderflow.orderservice.repository.TransactionRepository;
import com.thewyolar.orderflow.orderservice.util.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final OrderMapper orderMapper;
    private final TransactionMapper transactionMapper;
    private final RSAEncryptor encryptor;
    private final RedisTemplate<String, String> redisTemplate;

    public TransactionService(OrderRepository orderRepository, TransactionRepository transactionRepository, OrderMapper orderMapper, TransactionMapper transactionMapper, RedisTemplate<String, String> redisTemplate) {
        this.orderRepository = orderRepository;
        this.transactionRepository = transactionRepository;
        this.orderMapper = orderMapper;
        this.transactionMapper = transactionMapper;
        this.redisTemplate = redisTemplate;
        this.encryptor = new RSAEncryptor();
    }

    @Transactional(readOnly = true)
    public OrderStatusResponseDTO getOrderStatus(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        return orderMapper.toOrderStatusResponseDTO(order);
    }

    @Transactional
    public PaymentResponseDTO makePayment(PaymentDTO paymentDTO) {
        Order order = orderRepository.findById(paymentDTO.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order not found"));

        // Расшифровываем номер карты и CVV-код
        String cardNumber = encryptor.decrypt(paymentDTO.getCardNumber());
        String cvv = encryptor.decrypt(paymentDTO.getCvvCode());

        // Создаем транзакцию
        Transaction transaction = new Transaction();
        transaction.setOrder(order);
        transaction.setMerchant(order.getMerchant());
        transaction.setAmount(paymentDTO.getAmount());
        transaction.setCurrency(paymentDTO.getCurrency());
        transaction.setDateCreate(LocalDateTime.now());
        transaction.setDateUpdate(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.COMPLETE); // TODO нужно без этого
        transaction.setType(TransactionType.PAYMENT);
        transactionRepository.save(transaction);

        // Сохраняем расшифрованные данные в Redis
        String cardNumberKey = "cardNumber:" + transaction.getId();
        String cvvKey = "cvv:" + transaction.getId();
        redisTemplate.opsForValue().set(cardNumberKey, cardNumber, 20, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(cvvKey, cvv, 20, TimeUnit.MINUTES);

        // обновляем статус заказа
        // TODO нужно без этого
        order.setStatus(OrderStatus.PAID);
        order.setDateUpdate(OffsetDateTime.now());
        orderRepository.save(order);

        // формируем ответ
        PaymentResponseDTO paymentResponseDTO = transactionMapper.toPaymentResponseDTO(transaction);
        paymentResponseDTO.setStatus(OrderStatus.PAID);

        return paymentResponseDTO;
    }

    @Transactional
    @KafkaListener(topics = "new_transactions")
    public void processNewTransaction(PaymentResponseDTO paymentResponseDTO) {
        // найдем транзакцию в БД
        Transaction savedTransaction = transactionRepository.findById(paymentResponseDTO.getTransactionId())
                .orElseThrow(() -> new NotFoundException("Transaction not found"));

        // Получаем данные из Redis
        String cardNumberKey = "cardNumber:" + paymentResponseDTO.getTransactionId();
        String cvvKey = "cvv:" + paymentResponseDTO.getTransactionId();
        String cardNumber = redisTemplate.opsForValue().get(cardNumberKey);
        String cvv = redisTemplate.opsForValue().get(cvvKey);

        // проверяем номер карты алгоритмом LUNA
        if (!isLuhnValid(cardNumber)) {
            savedTransaction.setStatus(TransactionStatus.DECLINED);
            transactionRepository.save(savedTransaction);
        } else {
            String cardType = getCardType(cardNumber);
            if (cardType.equals("VISA")) {
                // отклоняем транзакцию
                savedTransaction.setStatus(TransactionStatus.DECLINED);
                transactionRepository.save(savedTransaction);
            } else if (cardType.equals("MasterCard") || cardType.equals("MIR")) {
                Order order = savedTransaction.getOrder();
                if (order.getStatus().equals(OrderStatus.PAID) || order.getStatus().equals(OrderStatus.PARTIAL_REFUNDED)) {
                    // отклоняем транзакцию
                    savedTransaction.setStatus(TransactionStatus.DECLINED);
                    transactionRepository.save(savedTransaction);
                } else if (order.getStatus().equals(OrderStatus.NEW) || order.getStatus().equals(OrderStatus.PARTIAL_PAID)) {
                    List<Transaction> transactions = transactionRepository.findByOrderAndStatus(order, TransactionStatus.COMPLETE);
                    Double totalAmount = 0.0;
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
            cardType = "VISA";
        } else if (cardNumber.startsWith("5")) {
            int secondDigit = Integer.parseInt(cardNumber.substring(1, 2));
            if (secondDigit >= 1 && secondDigit <= 5) {
                cardType = "MasterCard";
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
