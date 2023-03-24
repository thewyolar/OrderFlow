package com.thewyolar.orderflow.orderservice.service;

import com.thewyolar.orderflow.orderservice.dto.PaymentDTO;
import com.thewyolar.orderflow.orderservice.dto.PaymentResponseDTO;
import com.thewyolar.orderflow.orderservice.model.Order;
import com.thewyolar.orderflow.orderservice.model.Transaction;
import com.thewyolar.orderflow.orderservice.repository.MerchantRepository;
import com.thewyolar.orderflow.orderservice.repository.OrderRepository;
import com.thewyolar.orderflow.orderservice.repository.TransactionRepository;
import com.thewyolar.orderflow.orderservice.util.OrderStatus;
import com.thewyolar.orderflow.orderservice.util.TransactionStatus;
import com.thewyolar.orderflow.orderservice.util.TransactionType;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Service
public class PayformService {
    private final OrderRepository orderRepository;
    private final TransactionRepository transactionRepository;
    private ModelMapper modelMapper;

    public PayformService(OrderRepository orderRepository, TransactionRepository transactionRepository, ModelMapper modelMapper) {
        this.orderRepository = orderRepository;
        this.transactionRepository = transactionRepository;
        this.modelMapper = modelMapper;
    }

    public PaymentResponseDTO makePayment(PaymentDTO paymentDTO) {
        Order order = orderRepository.findById(paymentDTO.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order not found"));

        // perform payment processing and create transaction
        Transaction transaction = new Transaction();
        transaction.setOrder(order);
        transaction.setAmount(paymentDTO.getAmount());
        transaction.setCurrency(paymentDTO.getCurrency());
        transaction.setDateCreate(LocalDateTime.now());
        transaction.setDateUpdate(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.NEW);
        transaction.setType(TransactionType.PAYMENT);
        transaction.setContext(paymentDTO.getCardNumber() + " " + paymentDTO.getCardExpirationDate() + " " + paymentDTO.getCvvCode());
        transactionRepository.save(transaction);

        // update order status
        order.setStatus(OrderStatus.PAID);
        order.setDateUpdate(OffsetDateTime.now());
        orderRepository.save(order);

        // prepare response DTO
        PaymentResponseDTO paymentResponseDTO = new PaymentResponseDTO();
        paymentResponseDTO.setTransactionId(transaction.getId());
        paymentResponseDTO.setOrderId(order.getId());
        paymentResponseDTO.setAmount(transaction.getAmount());
        paymentResponseDTO.setCurrency(transaction.getCurrency());
        paymentResponseDTO.setDateCreate(transaction.getDateCreate());
        paymentResponseDTO.setDateUpdate(transaction.getDateUpdate());
        paymentResponseDTO.setStatus(order.getStatus());

        return paymentResponseDTO;
    }
}
