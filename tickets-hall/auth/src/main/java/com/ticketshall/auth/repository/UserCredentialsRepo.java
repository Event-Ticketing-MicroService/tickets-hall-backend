package com.ticketshall.auth.repository;

import com.ticketshall.auth.model.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserCredentialsRepo extends JpaRepository<UserCredentials, UUID> {
    Optional<UserCredentials> findByEmail(String email);
    Boolean existsByEmail(String email);
}
