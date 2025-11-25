package com.ticketshall.tickets.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.ticketshall.tickets.feign")
public class FeignConfig {
}
