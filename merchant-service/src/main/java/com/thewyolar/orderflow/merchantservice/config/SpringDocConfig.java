package com.thewyolar.orderflow.merchantservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition
@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI api() {
        return new OpenAPI().info(new Info()
                .title("Spring Doc")
                .version("1.0.0")
                .description("Spring Doc"));
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
