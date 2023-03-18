package com.thewyolar.orderflow.merchantservice.repository;

import com.thewyolar.orderflow.merchantservice.model.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {
}
