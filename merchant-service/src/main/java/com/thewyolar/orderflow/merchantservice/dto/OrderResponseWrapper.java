package com.thewyolar.orderflow.merchantservice.dto;

import com.thewyolar.orderflow.merchantservice.util.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class OrderResponseWrapper {
    private UUID orderId;
    private String name;
    private Double amount;
    private String currency;
    private UUID merchantId;
    private OffsetDateTime dateCreate;
    private OffsetDateTime dateUpdate;
    private OrderStatus status;
    private List<TransactionResponseDTO> transactions;
}

