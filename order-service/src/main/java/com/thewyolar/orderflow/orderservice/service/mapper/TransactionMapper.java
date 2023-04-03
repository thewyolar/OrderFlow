package com.thewyolar.orderflow.orderservice.service.mapper;

import com.thewyolar.orderflow.orderservice.dto.PaymentResponseDTO;
import com.thewyolar.orderflow.orderservice.dto.TransactionResponseDTO;
import com.thewyolar.orderflow.orderservice.model.Transaction;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {
    private final ModelMapper modelMapper;

    public TransactionMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public TransactionResponseDTO toTransactionResponseDTO(Transaction transaction) {
        return modelMapper.map(transaction, TransactionResponseDTO.class);
    }

    public PaymentResponseDTO toPaymentResponseDTO(Transaction transaction) {
        return modelMapper.map(transaction, PaymentResponseDTO.class);
    }
}
