package com.thewyolar.orderflow.orderservice.repository;

import com.thewyolar.orderflow.orderservice.model.Order;
import com.thewyolar.orderflow.orderservice.util.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByStatusAndTransactionsIsEmptyAndExpiredDateBefore(OrderStatus status, OffsetDateTime expiredDate);
}
