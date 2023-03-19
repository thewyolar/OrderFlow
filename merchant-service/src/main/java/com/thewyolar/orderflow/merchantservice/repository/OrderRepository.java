package com.thewyolar.orderflow.merchantservice.repository;

import com.thewyolar.orderflow.merchantservice.model.Order;
import com.thewyolar.orderflow.merchantservice.util.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByStatusAndTransactionsIsEmptyAndExpiredDateBefore(OrderStatus status, OffsetDateTime expiredDate);
}
