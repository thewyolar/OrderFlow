package com.thewyolar.orderflow.orderservice.service.mapper;

import com.thewyolar.orderflow.orderservice.dto.MerchantDTO;
import com.thewyolar.orderflow.orderservice.model.Merchant;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class MerchantMapper {

    private final ModelMapper modelMapper;

    public MerchantMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public Merchant toMerchant(MerchantDTO merchantDTO) {
        return modelMapper.map(merchantDTO, Merchant.class);
    }

    public MerchantDTO toMerchantDTO(Merchant merchant) {
        return modelMapper.map(merchant, MerchantDTO.class);
    }
}
