package com.example.auth.repository;

import com.example.auth.model.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.UUID;

public interface UserCredentialsRepo extends JpaRepository<UserCredentials, UUID> {
    UserCredentials findByEmail(String email);
    Boolean existsByEmail(String email);
}
