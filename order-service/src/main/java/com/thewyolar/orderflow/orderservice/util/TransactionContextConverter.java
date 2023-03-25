package com.thewyolar.orderflow.orderservice.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;

@Converter
public class TransactionContextConverter implements AttributeConverter<TransactionContext, String> {

    private final ObjectMapper objectMapper;

    public TransactionContextConverter() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String convertToDatabaseColumn(TransactionContext attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка преобразования TransactionContext в строку", e);
        }
    }

    @Override
    public TransactionContext convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, TransactionContext.class);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка преобразования строки в TransactionContext", e);
        }
    }
}
