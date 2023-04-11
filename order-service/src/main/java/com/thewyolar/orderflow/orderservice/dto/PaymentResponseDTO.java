package com.thewyolar.orderflow.orderservice.dto;

import com.thewyolar.orderflow.orderservice.util.TransactionStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class PaymentResponseDTO {
    private UUID transactionId;
    private UUID orderId;
    private Double amount;
    private String currency;
    private LocalDateTime dateCreate;
    private LocalDateTime dateUpdate;
    private TransactionStatus status;
}

