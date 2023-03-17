package com.thewyolar.orderflow.merchantservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MerchantServiceApplication {

    public static void main(String[] args) {

        SpringApplication.run(MerchantServiceApplication.class, args);
    }
}
