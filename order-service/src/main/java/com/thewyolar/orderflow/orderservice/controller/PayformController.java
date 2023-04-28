package com.thewyolar.orderflow.orderservice.controller;

import com.thewyolar.orderflow.orderservice.dto.OrderStatusResponseDTO;
import com.thewyolar.orderflow.orderservice.dto.PaymentDTO;
import com.thewyolar.orderflow.orderservice.dto.PaymentResponseDTO;
import com.thewyolar.orderflow.orderservice.service.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping("/api/payform")
public class PayformController {

    private TransactionService transactionService;

    private KafkaTemplate<Long, PaymentResponseDTO> kafkaTemplate;

    @PostMapping("/payment/make")
    public ResponseEntity<PaymentResponseDTO> makePayment(@RequestBody PaymentDTO paymentDTO) {
        PaymentResponseDTO paymentResponseDTO = transactionService.makePayment(paymentDTO);
        kafkaTemplate.send("new_transactions", paymentResponseDTO);
        return ResponseEntity.ok(paymentResponseDTO);
    }

    @GetMapping("/{orderId}/status")
    public ResponseEntity<OrderStatusResponseDTO> getOrderStatus(@PathVariable UUID orderId) {
        return ResponseEntity.ok(transactionService.getOrderStatus(orderId));
    }
}
