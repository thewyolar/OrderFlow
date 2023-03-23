package com.thewyolar.orderflow.orderservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class OrderDTO {
    private String name;
    private Double amount;
    private String currency;
    private UUID merchantId;
}

