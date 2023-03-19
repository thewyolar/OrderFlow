package com.thewyolar.orderflow.merchantservice.controller;

import com.thewyolar.orderflow.merchantservice.dto.OrderDTO;
import com.thewyolar.orderflow.merchantservice.dto.OrderResponseDTO;
import com.thewyolar.orderflow.merchantservice.dto.OrderResponseWrapper;
import com.thewyolar.orderflow.merchantservice.dto.TransactionResponseDTO;
import com.thewyolar.orderflow.merchantservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/api/merchant/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/add")
    public ResponseEntity<OrderResponseDTO> addOrder(@RequestBody OrderDTO order) {
        OrderResponseDTO orderResponseDTO = orderService.createOrder(order);
        return ResponseEntity.ok(orderResponseDTO);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseWrapper> getOrder(@PathVariable UUID orderId) {
        OrderResponseWrapper orderResponseWrapper = orderService.getOrderById(orderId);
        return ResponseEntity.ok(orderResponseWrapper);
    }

    @PostMapping("/order/{transactionId}/refund")
    public ResponseEntity<TransactionResponseDTO> refundOrder(@PathVariable UUID transactionId) throws NotFoundException {
        TransactionResponseDTO transactionResponseDTO = orderService.refundOrder(transactionId);
        return ResponseEntity.ok(transactionResponseDTO);
    }
}
