package com.thewyolar.orderflow.orderservice.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionContext {

    private String cardNumber;

    private String cvv;

    private String cardExpirationDate;
}
