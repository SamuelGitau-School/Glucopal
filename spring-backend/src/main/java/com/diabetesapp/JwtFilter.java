package com.diabetesapp;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;


    @Override
protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain chain)
    throws ServletException, IOException {

    String header = request.getHeader("Authorization");
    System.out.println("=== JWT FILTER ===");
    System.out.println("Method: " + request.getMethod());
    System.out.println("Path: " + request.getRequestURI());
    System.out.println("Auth header: " + (header != null ? "present - " + header.substring(0, Math.min(20, header.length())) + "..." : "MISSING"));

    if (header != null && header.startsWith("Bearer ")) {
        String token = header.substring(7);
        try {
            Claims claims = jwtUtil.validateToken(token);
            Long userId = Long.valueOf(claims.getSubject());
            System.out.println("Token valid - userId: " + userId);
            System.out.println("Token expiry: " + claims.getExpiration());

            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userId, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (JwtException e) {
            System.out.println("Token INVALID: " + e.getMessage());
            SecurityContextHolder.clearContext();
        }
    } else {
        System.out.println("No Bearer token found");
    }

    chain.doFilter(request, response);
}
}