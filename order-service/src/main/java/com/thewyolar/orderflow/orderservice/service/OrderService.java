package com.thewyolar.orderflow.orderservice.service;

import com.thewyolar.orderflow.orderservice.dto.OrderDTO;
import com.thewyolar.orderflow.orderservice.dto.OrderResponseDTO;
import com.thewyolar.orderflow.orderservice.dto.OrderResponseWrapper;
import com.thewyolar.orderflow.orderservice.dto.TransactionResponseDTO;
import com.thewyolar.orderflow.orderservice.exception.NotFoundException;
import com.thewyolar.orderflow.orderservice.service.mapper.OrderMapper;
import com.thewyolar.orderflow.orderservice.service.mapper.TransactionMapper;
import com.thewyolar.orderflow.orderservice.model.Merchant;
import com.thewyolar.orderflow.orderservice.model.Order;
import com.thewyolar.orderflow.orderservice.model.Transaction;
import com.thewyolar.orderflow.orderservice.repository.MerchantRepository;
import com.thewyolar.orderflow.orderservice.repository.OrderRepository;
import com.thewyolar.orderflow.orderservice.repository.TransactionRepository;
import com.thewyolar.orderflow.orderservice.util.OrderStatus;
import com.thewyolar.orderflow.orderservice.util.TransactionStatus;
import com.thewyolar.orderflow.orderservice.util.TransactionType;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final TransactionRepository transactionRepository;

    private final MerchantRepository merchantRepository;

    private final OrderMapper orderMapper;

    private final TransactionMapper transactionMapper;

    @Transactional
    public OrderResponseDTO createOrder(OrderDTO orderDTO) throws NotFoundException {
        Order order = orderMapper.toOrder(orderDTO);
        order.setStatus(OrderStatus.NEW);

        Merchant merchant = merchantRepository.findById(orderDTO.getMerchantId())
                .orElseThrow(() -> new NotFoundException("Мерчант с id=" + orderDTO.getMerchantId() + " не найден"));

        order.setMerchant(merchant);
        OffsetDateTime dateCreate = OffsetDateTime.now();
        OffsetDateTime expiredDate = dateCreate.plusMinutes(10);
        order.setDateCreate(dateCreate);
        order.setDateUpdate(dateCreate);
        order.setExpiredDate(expiredDate);
        order = orderRepository.save(order);

        OrderResponseDTO orderResponseDTO = orderMapper.toOrderResponseDTO(order);
        orderResponseDTO.setPayformUrl("https://site.com/order?" + orderResponseDTO.getOrderId());

        return orderResponseDTO;
    }

    @Transactional(readOnly = true)
    public OrderResponseWrapper getOrderById(UUID orderId) throws NotFoundException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ с id=" + orderId + " не найден"));

        return orderMapper.toOrderResponseWrapper(order);
    }

    @Transactional
    public void deleteOrderById(UUID orderId) throws NotFoundException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ с id=" + orderId + " не найден"));

        orderRepository.delete(order);
    }

    @Transactional
    public TransactionResponseDTO refundOrder(UUID transactionId) throws NotFoundException {
        Transaction initialTransaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("Транзакция с id=" + transactionId + " не найдена"));

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

        // Сохраняем транзакцию в БД
        refundTransaction = transactionRepository.save(refundTransaction);
        TransactionResponseDTO transactionResponseDTO = transactionMapper.toTransactionResponseDTO(refundTransaction);

        // Если первоначальной транзакции нет или она не является транзакцией оплаты со статусом COMPLETE, формируем сообщение и отправляем в Kafka с транзакцией типа REFUND
        if (!initialTransaction.getType().equals(TransactionType.PAYMENT) || !initialTransaction.getStatus().equals(TransactionStatus.COMPLETE)) {
            transactionResponseDTO.setStatus(TransactionStatus.DECLINED);
        }

        return transactionResponseDTO;
    }

    @Transactional
    @KafkaListener(topics = "refund_transactions")
    public void processRefundTransaction(TransactionResponseDTO transactionResponseDTO) throws NotFoundException {
        // Находим транзакцию в БД
        Transaction transaction = transactionRepository.findById(transactionResponseDTO.getTransactionId())
                .orElseThrow(() -> new NotFoundException("Транзакция с id=" + transactionResponseDTO.getTransactionId() + " не найдена"));

        // Проверяем тип транзакции
        if (transaction.getType() == TransactionType.REFUND) {
            // Проверяем статус транзакции, и если он DECLINED, меняем статус на DECLINED и сохраняем транзакцию в БД
            if (transactionResponseDTO.getStatus() == TransactionStatus.DECLINED) {
                transaction.setStatus(TransactionStatus.DECLINED);
                transactionRepository.save(transaction);
            }

            // Находим все транзакции с типом REFUND и со статусом COMPLETE для данного ордера
            List<Transaction> refundTransactions = transactionRepository
                    .findByOrderAndTypeAndStatus(transaction.getOrder(), TransactionType.REFUND, TransactionStatus.COMPLETE);

            // Считаем сумму возврата для всех найденных транзакций
            Double refundAmount = 0.0;
            for (Transaction refundTransaction : refundTransactions) {
                refundAmount += refundTransaction.getAmount();
            }

            // Если сумма возврата меньше или равна сумме ордера, меняем статус ордера на PARTIAL_REFUNDED или REFUNDED
            if (refundAmount.compareTo(transaction.getOrder().getAmount()) <= 0) {
                if (refundAmount == 0) {
                    transaction.getOrder().setStatus(OrderStatus.REFUNDED);
                } else {
                    transaction.getOrder().setStatus(OrderStatus.PARTIAL_REFUNDED);
                }
            }

            // Меняем статус транзакции с типом REFUND на COMPLETE
            transaction.setStatus(TransactionStatus.COMPLETE);
            transactionRepository.save(transaction);
        }
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
