package com.thewyolar.orderflow.orderservice.controller;

import com.thewyolar.orderflow.orderservice.dto.PaymentDTO;
import com.thewyolar.orderflow.orderservice.dto.PaymentResponseDTO;
import com.thewyolar.orderflow.orderservice.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payform")
public class PayformController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/payment/make")
    public ResponseEntity<PaymentResponseDTO> makePayment(@RequestBody PaymentDTO paymentDTO) {
        PaymentResponseDTO paymentResponseDTO = transactionService.makePayment(paymentDTO);
        return ResponseEntity.ok(paymentResponseDTO);
    }
}
