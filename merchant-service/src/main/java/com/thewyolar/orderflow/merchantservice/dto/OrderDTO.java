package com.thewyolar.orderflow.merchantservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class OrderDTO {
    private String name;
    private double amount;
    private String currency;
    private UUID merchantId;
}

