package com.ticketshall.auth.service;

import com.ticketshall.auth.DTO.LoginRequestDTO;
import com.ticketshall.auth.DTO.RefreshResponseDTO;
import com.ticketshall.auth.DTO.SignupLoginResponseDTO;
import com.ticketshall.auth.DTO.SignupRequestDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;


public interface AuthService {
    SignupLoginResponseDTO login(LoginRequestDTO request);
    SignupLoginResponseDTO signup(SignupRequestDTO request, MultipartFile image);
    HttpHeaders validate(String authorizationHeader, String role);
    RefreshResponseDTO refreshToken(String refreshToken);
}
