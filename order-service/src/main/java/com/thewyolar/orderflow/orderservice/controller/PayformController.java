package com.thewyolar.orderflow.orderservice.controller;

import com.thewyolar.orderflow.orderservice.dto.PaymentDTO;
import com.thewyolar.orderflow.orderservice.dto.PaymentResponseDTO;
import com.thewyolar.orderflow.orderservice.service.PayformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payform")
public class PayformController {

    @Autowired
    private PayformService payformService;

    @PostMapping("/payment/make")
    public ResponseEntity<PaymentResponseDTO> makePayment(@RequestBody PaymentDTO paymentDTO) {
        PaymentResponseDTO paymentResponseDTO = payformService.makePayment(paymentDTO);
        return ResponseEntity.ok(paymentResponseDTO);
    }
}
