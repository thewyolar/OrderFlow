package com.thewyolar.orderflow.orderservice.dto;

import com.thewyolar.orderflow.orderservice.util.OrderStatus;
import lombok.Getter;
import lombok.Setter;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class OrderStatusResponseDTO {
    private UUID orderId;
    private String name;
    private Double amount;
    private String currency;
    private OffsetDateTime dateCreate;
    private OffsetDateTime dateUpdate;
    private OrderStatus status;
}

