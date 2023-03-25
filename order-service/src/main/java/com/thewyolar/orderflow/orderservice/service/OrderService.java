package com.thewyolar.orderflow.orderservice.service;

import com.thewyolar.orderflow.orderservice.dto.OrderDTO;
import com.thewyolar.orderflow.orderservice.dto.OrderResponseDTO;
import com.thewyolar.orderflow.orderservice.dto.OrderResponseWrapper;
import com.thewyolar.orderflow.orderservice.dto.TransactionResponseDTO;
import com.thewyolar.orderflow.orderservice.model.Merchant;
import com.thewyolar.orderflow.orderservice.model.Order;
import com.thewyolar.orderflow.orderservice.model.Transaction;
import com.thewyolar.orderflow.orderservice.repository.MerchantRepository;
import com.thewyolar.orderflow.orderservice.repository.OrderRepository;
import com.thewyolar.orderflow.orderservice.repository.TransactionRepository;
import com.thewyolar.orderflow.orderservice.util.OrderStatus;
import com.thewyolar.orderflow.orderservice.util.TransactionStatus;
import com.thewyolar.orderflow.orderservice.util.TransactionType;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final TransactionRepository transactionRepository;
    private final MerchantRepository merchantRepository;
    private ModelMapper modelMapper;
    private KafkaTemplate<Long, TransactionResponseDTO> kafkaTemplate;

    public OrderService(OrderRepository orderRepository, TransactionRepository transactionRepository, MerchantRepository merchantRepository, ModelMapper modelMapper, KafkaTemplate<Long, TransactionResponseDTO> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.transactionRepository = transactionRepository;
        this.merchantRepository = merchantRepository;
        this.modelMapper = modelMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    public OrderResponseDTO createOrder(OrderDTO orderDTO) {
        Order order = new Order();
        order.setName(orderDTO.getName());
        order.setAmount(orderDTO.getAmount());
        order.setCurrency(orderDTO.getCurrency());
        order.setStatus(OrderStatus.NEW);

        Merchant merchant = merchantRepository.findById(orderDTO.getMerchantId())
                .orElseThrow(() -> new NotFoundException("Merchant not found"));

        order.setMerchant(merchant);
        OffsetDateTime dateCreate = OffsetDateTime.now();
        OffsetDateTime expiredDate = dateCreate.plusMinutes(10);
        order.setDateCreate(dateCreate);
        order.setDateUpdate(dateCreate);
        order.setExpiredDate(expiredDate);
        order = orderRepository.save(order);

//        Transaction transaction = new Transaction();
//        transaction.setOrder(order);
//        transaction.setAmount(order.getAmount());
//        transaction.setCurrency(order.getCurrency());
//        transaction.setDateCreate(dateCreate.toLocalDateTime());
//        transaction.setDateUpdate(order.getDateUpdate().toLocalDateTime());
//        transaction.setMerchant(merchant);
//        transaction.setStatus(TransactionStatus.NEW);
//        transaction.setType(TransactionType.PAYMENT);
//        transactionRepository.save(transaction);

        OrderResponseDTO orderResponseDTO = modelMapper.map(order, OrderResponseDTO.class);
        orderResponseDTO.setPayformUrl("https://site.com/order?" + orderResponseDTO.getOrderId());

//        String message = "New order created with id: " + orderResponseDTO.getOrderId();
//        kafkaTemplate.send("order-topic", message);

        return orderResponseDTO;
    }

    public OrderResponseWrapper getOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        OrderResponseWrapper orderResponseWrapper = modelMapper.map(order, OrderResponseWrapper.class);
        return orderResponseWrapper;
    }

    public void deleteOrderById(UUID orderId) {
        orderRepository.deleteById(orderId);
    }

    public OrderResponseWrapper updateOrder(UUID orderId, OrderDTO updatedOrder) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with id=" + orderId));

        order.setName(updatedOrder.getName());
        order.setAmount(updatedOrder.getAmount());
        order.setCurrency(updatedOrder.getCurrency());

        Merchant merchant = merchantRepository.findById(updatedOrder.getMerchantId())
                .orElseThrow(() -> new NotFoundException("Merchant not found"));

        order.setMerchant(merchant);
        order.setDateUpdate(OffsetDateTime.now());

        Order savedOrder = orderRepository.save(order);
        OrderResponseWrapper orderResponseWrapper = modelMapper.map(savedOrder, OrderResponseWrapper.class);

        return orderResponseWrapper;
    }

    public TransactionResponseDTO refundOrder(UUID transactionId) {
        Transaction initialTransaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));
        initialTransaction.setStatus(TransactionStatus.DECLINED);
        transactionRepository.save(initialTransaction);

//        Transaction refundTransaction = new Transaction();
//        refundTransaction.setOrder(initialTransaction.getOrder());
//        refundTransaction.setMerchant(initialTransaction.getMerchant());
//        refundTransaction.setAmount(initialTransaction.getAmount());
//        refundTransaction.setCurrency(initialTransaction.getCurrency());
//        refundTransaction.setDateCreate(initialTransaction.getDateCreate());
//        refundTransaction.setDateUpdate(initialTransaction.getDateUpdate());
//        refundTransaction.setContext(initialTransaction.getContext());
//        refundTransaction.setStatus(TransactionStatus.NEW);
//        refundTransaction.setType(initialTransaction.getType());
//        transactionRepository.save(refundTransaction);
//
//        TransactionResponseDTO transactionResponseDTO = modelMapper.map(refundTransaction, TransactionResponseDTO.class);
//        return transactionResponseDTO;

        // Проверяем, что первоначальная транзакция оплаты имеет статус COMPLETE
        if (initialTransaction.getType() != TransactionType.PAYMENT || initialTransaction.getStatus() != TransactionStatus.COMPLETE) {
            // Если первоначальной транзакции нет или она не является транзакцией оплаты со статусом COMPLETE, формируем сообщение и отправляем в Kafka с транзакцией типа REFUND и статусом DECLINED
            TransactionResponseDTO transactionResponseDTO = modelMapper.map(initialTransaction, TransactionResponseDTO.class);
            kafkaTemplate.send("refund_transactions", transactionResponseDTO);
            return transactionResponseDTO;
        }

        // Создаем новую транзакцию с типом REFUND и статусом NEW
        Transaction refundTransaction = new Transaction();
        refundTransaction.setType(TransactionType.REFUND);
        refundTransaction.setStatus(TransactionStatus.NEW);
        refundTransaction.setOrder(initialTransaction.getOrder());
        refundTransaction.setMerchant(initialTransaction.getMerchant());
        refundTransaction.setAmount(initialTransaction.getAmount());
        refundTransaction.setCurrency(initialTransaction.getCurrency());
        refundTransaction.setDateCreate(initialTransaction.getDateCreate());
        refundTransaction.setDateUpdate(initialTransaction.getDateUpdate());
        refundTransaction.setContext(initialTransaction.getContext());

        // Сохраняем транзакцию в БД и отправляем сообщение в Kafka с транзакцией типа REFUND и статусом NEW
        refundTransaction = transactionRepository.save(refundTransaction);
        TransactionResponseDTO transactionResponseDTO = modelMapper.map(refundTransaction, TransactionResponseDTO.class);
        kafkaTemplate.send("refund_transactions", transactionResponseDTO);

        return transactionResponseDTO;
    }

    @Transactional
    @Scheduled(fixedDelay = 60000)
    public void updateExpiredOrders() {
        List<Order> orders = orderRepository.findByStatusAndTransactionsIsEmptyAndExpiredDateBefore(OrderStatus.NEW, OffsetDateTime.now());
        for (Order order : orders) {
            order.setStatus(OrderStatus.EXPIRED);
        }
    }
}
