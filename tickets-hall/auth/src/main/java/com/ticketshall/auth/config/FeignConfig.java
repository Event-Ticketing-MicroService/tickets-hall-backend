package com.ticketshall.auth.config;

import com.ticketshall.auth.feign.FeignErrorDecoder;
import feign.Retryer;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableFeignClients(basePackages = "com.ticketshall.auth.feign")
public class FeignConfig {

    @Bean
    public Encoder feignFormEncoder(org.springframework.beans.factory.ObjectFactory<org.springframework.boot.autoconfigure.http.HttpMessageConverters> messageConverters) {
        return new SpringFormEncoder(new org.springframework.cloud.openfeign.support.SpringEncoder(messageConverters));
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }
}
