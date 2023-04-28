package com.thewyolar.orderflow.orderservice.controller;

import com.thewyolar.orderflow.orderservice.dto.OrderDTO;
import com.thewyolar.orderflow.orderservice.dto.OrderResponseDTO;
import com.thewyolar.orderflow.orderservice.dto.OrderResponseWrapper;
import com.thewyolar.orderflow.orderservice.dto.TransactionResponseDTO;
import com.thewyolar.orderflow.orderservice.exception.MerchantNotFoundException;
import com.thewyolar.orderflow.orderservice.exception.OrderNotFoundException;
import com.thewyolar.orderflow.orderservice.exception.TransactionNotFoundException;
import com.thewyolar.orderflow.orderservice.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping("/api/merchant/orders")
public class OrderController {

    private OrderService orderService;

    private KafkaTemplate<Long, TransactionResponseDTO> kafkaTemplate;

    @PostMapping("/add")
    public ResponseEntity<OrderResponseDTO> addOrder(@RequestBody OrderDTO order) throws MerchantNotFoundException {
        return ResponseEntity.ok(orderService.createOrder(order));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseWrapper> getOrder(@PathVariable UUID orderId) throws OrderNotFoundException {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<String> deleteOrder(@PathVariable UUID orderId) throws OrderNotFoundException {
        orderService.deleteOrderById(orderId);
        return ResponseEntity.ok("Заказ с id=" + orderId + " успешно удален.");
    }

    @PostMapping("/{transactionId}/refund")
    public ResponseEntity<TransactionResponseDTO> refundOrder(@PathVariable UUID transactionId) throws TransactionNotFoundException {
        TransactionResponseDTO transactionResponseDTO = orderService.refundOrder(transactionId);
        kafkaTemplate.send("refund_transactions", transactionResponseDTO);
        return ResponseEntity.ok(transactionResponseDTO);
    }
}
