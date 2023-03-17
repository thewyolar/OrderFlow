package com.thewyolar.orderflow.merchantservice.service;

import com.thewyolar.orderflow.merchantservice.dto.OrderDTO;
import com.thewyolar.orderflow.merchantservice.dto.OrderResponseDTO;
import com.thewyolar.orderflow.merchantservice.dto.OrderResponseWrapper;
import com.thewyolar.orderflow.merchantservice.dto.TransactionResponseDTO;
import com.thewyolar.orderflow.merchantservice.model.Order;
import com.thewyolar.orderflow.merchantservice.model.Transaction;
import com.thewyolar.orderflow.merchantservice.repository.OrderRepository;
import com.thewyolar.orderflow.merchantservice.repository.TransactionRepository;
import com.thewyolar.orderflow.merchantservice.util.OrderStatus;
import com.thewyolar.orderflow.merchantservice.util.TransactionStatus;
import com.thewyolar.orderflow.merchantservice.util.TransactionType;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final TransactionRepository transactionRepository;
    private ModelMapper modelMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OrderService(OrderRepository orderRepository, TransactionRepository transactionRepository, ModelMapper modelMapper, KafkaTemplate<String, String> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.transactionRepository = transactionRepository;
        this.modelMapper = modelMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    public OrderResponseDTO createOrder(OrderDTO orderDTO) {
        Order order = new Order();
        order.setName(orderDTO.getName());
        order.setAmount(orderDTO.getAmount());
        order.setCurrency(orderDTO.getCurrency());
        order.setStatus(OrderStatus.NEW);
        order.setMerchantId(orderDTO.getMerchantId());
        OffsetDateTime dateCreate = OffsetDateTime.now();
        OffsetDateTime expiredDate = dateCreate.plusMinutes(10);
        order.setDateCreate(dateCreate);
        order.setDateUpdate(dateCreate);
        order.setExpiredDate(expiredDate);
        order = orderRepository.save(order);

        Transaction transaction = new Transaction();
        transaction.setOrder(order);
        transaction.setAmount(order.getAmount());
        transaction.setCurrency(order.getCurrency());
        transaction.setDateCreate(order.getDateCreate().toLocalDateTime());
        transaction.setDateUpdate(order.getDateUpdate().toLocalDateTime());
        transaction.setMerchantId(order.getMerchantId());
        transaction.setStatus(TransactionStatus.NEW);
        transaction.setType(TransactionType.PAYMENT);
        transactionRepository.save(transaction);

        OrderResponseDTO orderResponseDTO = modelMapper.map(order, OrderResponseDTO.class);
        orderResponseDTO.setPayformUrl("https://site.com/order?" + orderResponseDTO.getOrderId());

//        String message = "New order created with id: " + orderResponseDTO.getOrderId();
//        kafkaTemplate.send("order-topic", message);

        return orderResponseDTO;
    }

    public OrderResponseWrapper getOrderById(UUID orderId) throws NotFoundException {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException());
        OrderResponseWrapper orderResponseWrapper = modelMapper.map(order, OrderResponseWrapper.class);
        return orderResponseWrapper;
    }

    public TransactionResponseDTO refundOrder(UUID transactionId) throws NotFoundException {
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(() -> new NotFoundException());
        transaction.setStatus(TransactionStatus.DECLINED);
        transactionRepository.save(transaction);

        Transaction refundTransaction = new Transaction();
        refundTransaction.setOrder(transaction.getOrder());
        refundTransaction.setMerchantId(transaction.getMerchantId());
        refundTransaction.setAmount(transaction.getAmount());
        refundTransaction.setCurrency(transaction.getCurrency());
        refundTransaction.setDateCreate(transaction.getDateCreate());
        refundTransaction.setDateUpdate(transaction.getDateUpdate());
        refundTransaction.setContext(transaction.getContext());
        refundTransaction.setStatus(TransactionStatus.NEW);
        refundTransaction.setType(transaction.getType());
        transactionRepository.save(refundTransaction);

        TransactionResponseDTO transactionResponseDTO = modelMapper.map(refundTransaction, TransactionResponseDTO.class);
        return transactionResponseDTO;
    }

    //@Transactional
    @Scheduled(fixedDelay = 60000)
    public void updateExpiredOrders() {
        List<Order> orders = orderRepository.findAll();
        for (Order order: orders) {
            if (order.getTransactions().isEmpty() || OffsetDateTime.now().compareTo(order.getExpiredDate()) > 0) {
                if (order.getStatus() == OrderStatus.NEW) {
                    order.setStatus(OrderStatus.EXPIRED);
                }
            }
        }
    }
}
