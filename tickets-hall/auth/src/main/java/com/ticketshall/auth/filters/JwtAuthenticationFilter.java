package com.ticketshall.auth.filters;

import com.ticketshall.auth.config.JwtUtil;
import com.ticketshall.auth.service.CustomUserDetailsService;
import com.ticketshall.auth.service.RedisService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final RedisService redisService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwtToken;
        final String userEmail;

        log.info("=== JwtAuthenticationFilter triggered for request: {} {}", request.getMethod(), request.getRequestURI());

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("No Authorization header or Bearer token not found.");
            filterChain.doFilter(request, response);
            return;
        }

        jwtToken = authHeader.substring(7);
        log.debug("Extracted JWT token: {}", jwtToken);

        try {
            if (jwtUtil.isTokenExpired(jwtToken)) {
                log.warn("JWT token is expired.");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String jti = jwtUtil.extractClaim(jwtToken, Claims::getId);
            log.debug("Extracted JTI: {}", jti);

            if (Objects.nonNull(jti) && redisService.hasKey(jti)) {
                log.warn("JWT token with JTI {} found in Redis blacklist.", jti);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            userEmail = jwtUtil.extractEmail(jwtToken);
            log.info("Extracted username/email from token: {}", userEmail);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                log.debug("No authentication found in SecurityContext, loading user details...");

                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                log.debug("UserDetails loaded: username={}, authorities={}", userDetails.getUsername(), userDetails.getAuthorities());

                if (jwtUtil.validateToken(jwtToken, userDetails)) {
                    log.info("JWT token is valid. Setting authentication for user: {}", userEmail);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    log.warn("JWT token is not valid for user: {}", userEmail);
                }
            }

        } catch (ExpiredJwtException e) {
            log.error("ExpiredJwtException: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } catch (MalformedJwtException e) {
            log.error("MalformedJwtException: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } catch (UsernameNotFoundException e) {
            log.error("UsernameNotFoundException: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } catch (Exception e) {
            log.error("Unexpected exception during JWT filter: ", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        log.info("JwtAuthenticationFilter finished successfully for request: {}", request.getRequestURI());
        filterChain.doFilter(request, response);
    }
}
