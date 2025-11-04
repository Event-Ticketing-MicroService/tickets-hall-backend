package com.ticketshall.auth;

import java.util.UUID;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import com.ticketshall.auth.Enums.Role;
import com.ticketshall.auth.config.JwtUtil;

@SpringBootApplication
public class AuthApplication {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(AuthApplication.class, args);
        var jwtUtil = ctx.getBean(JwtUtil.class);
        System.out.println(jwtUtil.generateToken(Role.USER, UUID.fromString("550e8400-e29b-41d4-a716-446655440000")));
    }

}
