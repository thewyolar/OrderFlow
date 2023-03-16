package com.thewyolar.orderflow.merchantservice.dto;

import com.thewyolar.orderflow.merchantservice.util.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class OrderResponseDTO {
    private UUID orderId;
    private String name;
    private double amount;
    private String currency;
    private UUID merchantId;
    private OffsetDateTime dateCreate;
    private OffsetDateTime dateUpdate;
    private OrderStatus status;
    private String payformUrl;
}

