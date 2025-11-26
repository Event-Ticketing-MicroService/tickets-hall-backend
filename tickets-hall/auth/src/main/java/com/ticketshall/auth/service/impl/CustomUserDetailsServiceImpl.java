package com.ticketshall.auth.service.impl;

import com.ticketshall.auth.repository.UserCredentialsRepo;
import com.ticketshall.auth.security.SecurityUser;
import com.ticketshall.auth.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements UserDetailsService, CustomUserDetailsService {
    private final UserCredentialsRepo userCredentialsRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var user = userCredentialsRepo.findByEmail(email);
        return user.map(SecurityUser::new)
                .orElseThrow(() -> new UsernameNotFoundException("user with email " + email + " not found"));
    }
}
