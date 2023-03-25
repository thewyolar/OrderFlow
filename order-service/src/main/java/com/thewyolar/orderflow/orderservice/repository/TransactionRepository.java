package com.thewyolar.orderflow.orderservice.repository;

import com.thewyolar.orderflow.orderservice.model.Order;
import com.thewyolar.orderflow.orderservice.model.Transaction;
import com.thewyolar.orderflow.orderservice.util.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByOrderAndStatus(Order order, TransactionStatus transactionStatus);
}
