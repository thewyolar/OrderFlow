package com.thewyolar.orderflow.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.httpBasic()
            .and()
            .csrf().disable()
            .cors().disable()
            .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/api/payform/**").permitAll()
                .requestMatchers("/api/merchant/**").hasRole("MERCHANT")
                .anyRequest().authenticated())
            .formLogin((form) -> form
                .defaultSuccessUrl("/"))
            .logout((logout) -> logout
                    .logoutSuccessUrl("/")
        );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        BCryptPasswordEncoder encoder = passwordEncoder();
        UserDetails merchant =
                User.builder()
                        .username("merchant")
                        .password(encoder.encode("1234567890"))
                        .roles("MERCHANT")
                        .build();
        return new InMemoryUserDetailsManager(merchant);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
