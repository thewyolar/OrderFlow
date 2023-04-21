package com.thewyolar.orderflow.orderservice.controller;

import com.thewyolar.orderflow.orderservice.dto.OrderDTO;
import com.thewyolar.orderflow.orderservice.dto.OrderResponseDTO;
import com.thewyolar.orderflow.orderservice.dto.OrderResponseWrapper;
import com.thewyolar.orderflow.orderservice.dto.TransactionResponseDTO;
import com.thewyolar.orderflow.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/api/merchant/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private KafkaTemplate<Long, TransactionResponseDTO> kafkaTemplate;

    @PostMapping("/add")
    public ResponseEntity<OrderResponseDTO> addOrder(@RequestBody OrderDTO order) {
        return ResponseEntity.ok(orderService.createOrder(order));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseWrapper> getOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<String> deleteOrder(@PathVariable UUID orderId) {
        orderService.deleteOrderById(orderId);
        return ResponseEntity.ok("Заказ с id=" + orderId + " успешно удален.");
    }

    @PostMapping("/{transactionId}/refund")
    public ResponseEntity<TransactionResponseDTO> refundOrder(@PathVariable UUID transactionId) {
        TransactionResponseDTO transactionResponseDTO = orderService.refundOrder(transactionId);
        kafkaTemplate.send("refund_transactions", transactionResponseDTO);
        return ResponseEntity.ok(transactionResponseDTO);
    }
}
