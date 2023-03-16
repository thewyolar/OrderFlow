package com.thewyolar.orderflow.merchantservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderResponseWrapper extends OrderResponseDTO {
    private List<TransactionResponseDTO> transactions;
}

