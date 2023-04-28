package com.thewyolar.orderflow.orderservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MerchantResponseDTO {
    private UUID id;
    private String name;
    private String siteUrl;
}
