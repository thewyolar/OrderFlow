package com.thewyolar.orderflow.orderservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PaymentDTO {
    private UUID orderId;
    private Double amount;
    private String currency;
    private String cardNumber;
    private String cardExpirationDate;
    private String cvvCode;
}
