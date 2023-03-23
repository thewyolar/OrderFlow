package com.thewyolar.orderflow.orderservice.dto;

import com.thewyolar.orderflow.orderservice.util.TransactionStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class TransactionResponseDTO {
    private UUID transactionId;
    private UUID orderId;
    private double amount;
    private String currency;
    private UUID merchantId;
    private LocalDateTime dateCreate;
    private LocalDateTime dateUpdate;
    private TransactionStatus status;
}
