package com.thewyolar.orderflow.orderservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
public class ApiError {

    private int code;

    private HttpStatus status;

    private String message;
}

