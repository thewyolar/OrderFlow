package com.thewyolar.orderflow.orderservice.repository;

import com.thewyolar.orderflow.orderservice.model.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {
}
