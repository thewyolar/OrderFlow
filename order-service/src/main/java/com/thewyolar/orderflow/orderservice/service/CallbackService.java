package com.thewyolar.orderflow.orderservice.service;

import com.thewyolar.orderflow.orderservice.model.Merchant;
import com.thewyolar.orderflow.orderservice.model.Order;
import org.hibernate.CallbackException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CallbackService {
    private final RestTemplate restTemplate;

    public CallbackService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public void sendCallback(Merchant merchant, Order order) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Order> requestEntity = new HttpEntity<>(order, headers);
        String callbackUrl = merchant.getSiteUrl();
        ResponseEntity<Void> response = restTemplate.postForEntity(callbackUrl, requestEntity, Void.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new CallbackException("Не удалось отправить колбэк мерчанту");
        }
    }
}
