package com.thewyolar.orderflow.orderservice.service;

import com.thewyolar.orderflow.orderservice.dto.MerchantDTO;
import com.thewyolar.orderflow.orderservice.model.Merchant;
import com.thewyolar.orderflow.orderservice.repository.MerchantRepository;
import com.thewyolar.orderflow.orderservice.service.mapper.MerchantMapper;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;

import java.util.UUID;

@Service
public class MerchantService {
    private final MerchantRepository merchantRepository;

    private final MerchantMapper merchantMapper;

    public MerchantService(MerchantRepository merchantRepository, MerchantMapper merchantMapper) {
        this.merchantRepository = merchantRepository;
        this.merchantMapper = merchantMapper;
    }

    public Merchant getMerchantById(UUID id) {
        return merchantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Мерчант не найден"));
    }

    public Merchant createMerchant(MerchantDTO merchantDTO) {
        Merchant merchant = merchantMapper.toMerchant(merchantDTO);
        return merchantRepository.save(merchant);
    }

    public Merchant updateMerchant(UUID merchantId, MerchantDTO merchant) {
        Merchant existingMerchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new NotFoundException("Мерчант не найден"));
        existingMerchant.setName(merchant.getName());
        existingMerchant.setSiteUrl(merchant.getSiteUrl());
        return merchantRepository.save(existingMerchant);
    }

    public void deleteMerchantById(UUID merchantId) {
        merchantRepository.deleteById(merchantId);
    }
}
