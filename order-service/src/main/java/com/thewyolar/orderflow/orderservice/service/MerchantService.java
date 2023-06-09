package com.thewyolar.orderflow.orderservice.service;

import com.thewyolar.orderflow.orderservice.dto.MerchantDTO;
import com.thewyolar.orderflow.orderservice.dto.MerchantResponseDTO;
import com.thewyolar.orderflow.orderservice.exception.NotFoundException;
import com.thewyolar.orderflow.orderservice.model.Merchant;
import com.thewyolar.orderflow.orderservice.repository.MerchantRepository;
import com.thewyolar.orderflow.orderservice.service.mapper.MerchantMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
public class MerchantService {

    private final MerchantRepository merchantRepository;

    private final MerchantMapper merchantMapper;

    @Transactional(readOnly = true)
    public MerchantResponseDTO getMerchantById(UUID id) throws NotFoundException {
        Merchant merchant = merchantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Мерчант с id=" + id + " не найден"));
        return merchantMapper.toMerchantResponseDTO(merchant);
    }

    @Transactional
    public MerchantResponseDTO createMerchant(MerchantDTO merchantDTO) {
        Merchant merchant = merchantMapper.toMerchant(merchantDTO);
        merchantRepository.save(merchant);
        return merchantMapper.toMerchantResponseDTO(merchant);
    }

    @Transactional
    public Merchant updateMerchant(UUID merchantId, MerchantDTO merchantDTO) throws NotFoundException {
        Merchant existingMerchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new NotFoundException("Мерчант с id=" + merchantId + " не найден"));
        existingMerchant.setName(merchantDTO.getName());
        existingMerchant.setSiteUrl(merchantDTO.getSiteUrl());
        return merchantRepository.save(existingMerchant);
    }

    @Transactional
    public void deleteMerchantById(UUID merchantId) throws NotFoundException {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new NotFoundException("Мерчант с id=" + merchantId + " не найден"));

        merchantRepository.delete(merchant);
    }
}
