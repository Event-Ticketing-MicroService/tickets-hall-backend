package com.example.auth.model;

import com.example.auth.Enums.Role;
import com.example.auth.Enums.UserType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user_credentials")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCredentials implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID userId;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private Long externalId;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private UserType userType;

// -------- SPRING SECURITY METHODS --------

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convert your role enum (e.g., Role.ADMIN) into a Spring Security authority
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        // Since you want to log in using email
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // can add real logic later if needed
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // can add real logic later if needed
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // can add real logic later if needed
    }

    @Override
    public boolean isEnabled() {
        return true; // can disable accounts later if needed
    }

}
