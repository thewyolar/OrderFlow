package com.thewyolar.orderflow.merchantservice.dto;

import com.thewyolar.orderflow.merchantservice.util.TransactionStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class TransactionResponseDTO {
    private UUID transactionId;
    private UUID orderId;
    private double amount;
    private String currency;
    private UUID merchantId;
    private OffsetDateTime dateCreate;
    private OffsetDateTime dateUpdate;
    private TransactionStatus status;
}
