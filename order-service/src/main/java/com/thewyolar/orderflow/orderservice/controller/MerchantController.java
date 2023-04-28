package com.thewyolar.orderflow.orderservice.controller;

import com.thewyolar.orderflow.orderservice.dto.*;
import com.thewyolar.orderflow.orderservice.model.Merchant;
import com.thewyolar.orderflow.orderservice.service.MerchantService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping("/api/merchant")
public class MerchantController {

    private MerchantService merchantService;

    @PostMapping("/add")
    public ResponseEntity<MerchantResponseDTO> addMerchant(@RequestBody MerchantDTO merchantDTO) {
        return ResponseEntity.ok(merchantService.createMerchant(merchantDTO));
    }

    @GetMapping("/{merchantId}")
    public ResponseEntity<MerchantResponseDTO> getMerchant(@PathVariable UUID merchantId) {
        return ResponseEntity.ok(merchantService.getMerchantById(merchantId));
    }

    @DeleteMapping("/{merchantId}")
    public ResponseEntity<String> deleteOrder(@PathVariable UUID merchantId) {
        merchantService.deleteMerchantById(merchantId);
        return ResponseEntity.ok("Merchant with id=" + merchantId + " deleted successfully.");
    }

    @PatchMapping("/{merchantId}")
    public ResponseEntity<Merchant> updateMerchant(@PathVariable UUID merchantId, @RequestBody MerchantDTO merchantDTO) {
        return ResponseEntity.ok(merchantService.updateMerchant(merchantId, merchantDTO));
    }
}
