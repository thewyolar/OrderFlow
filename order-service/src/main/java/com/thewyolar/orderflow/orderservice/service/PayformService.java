package com.thewyolar.orderflow.orderservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.thewyolar.orderflow.orderservice.dto.PaymentDTO;
import com.thewyolar.orderflow.orderservice.dto.PaymentResponseDTO;
import com.thewyolar.orderflow.orderservice.model.Order;
import com.thewyolar.orderflow.orderservice.model.Transaction;
import com.thewyolar.orderflow.orderservice.repository.OrderRepository;
import com.thewyolar.orderflow.orderservice.repository.TransactionRepository;
import com.thewyolar.orderflow.orderservice.util.*;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class PayformService {
    private final OrderRepository orderRepository;
    private final TransactionRepository transactionRepository;
    private ModelMapper modelMapper;
    private RSAEncryptor encryptor;

    private KafkaTemplate<Long, PaymentResponseDTO> kafkaTemplate;

    public PayformService(OrderRepository orderRepository, TransactionRepository transactionRepository, ModelMapper modelMapper, KafkaTemplate<Long, PaymentResponseDTO> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.transactionRepository = transactionRepository;
        this.modelMapper = modelMapper;
        this.kafkaTemplate = kafkaTemplate;
        this.encryptor = new RSAEncryptor();
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
}
