package com.thewyolar.orderflow.merchantservice.service;

import com.thewyolar.orderflow.merchantservice.dto.OrderDTO;
import com.thewyolar.orderflow.merchantservice.dto.OrderResponseDTO;
import com.thewyolar.orderflow.merchantservice.model.Order;
import com.thewyolar.orderflow.merchantservice.model.Transaction;
import com.thewyolar.orderflow.merchantservice.repository.OrderRepository;
import com.thewyolar.orderflow.merchantservice.repository.TransactionRepository;
import com.thewyolar.orderflow.merchantservice.util.OrderStatus;
import com.thewyolar.orderflow.merchantservice.util.TransactionStatus;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

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
        transaction = transactionRepository.save(transaction);

        OrderResponseDTO responseDTO = modelMapper.map(order, OrderResponseDTO.class);
        responseDTO.setPayformUrl("https://site.com/order?" + responseDTO.getOrderId());

//        String message = "New order created with id: " + responseDTO.getOrderId();
//        kafkaTemplate.send("order-topic", message);

        return responseDTO;
    }
}
