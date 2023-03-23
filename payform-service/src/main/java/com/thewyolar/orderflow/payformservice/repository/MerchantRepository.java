package com.thewyolar.orderflow.payformservice.repository;

import com.thewyolar.orderflow.payformservice.model.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {
}
