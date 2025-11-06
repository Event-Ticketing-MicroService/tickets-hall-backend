package com.ticketshall.auth.service;

import com.ticketshall.auth.model.UserCredentials;
import com.ticketshall.auth.repository.UserCredentialsRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCredentialsService implements UserDetailsService {
    private final UserCredentialsRepo userCredentialsRepo;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserCredentials user = userCredentialsRepo.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        return user;
    }
}
