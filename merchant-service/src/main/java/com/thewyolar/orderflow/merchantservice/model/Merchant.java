package com.thewyolar.orderflow.merchantservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "merchants")
public class Merchant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "site_url")
    private String siteUrl;
}
