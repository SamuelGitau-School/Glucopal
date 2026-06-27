package com.diabetesapp.controller;

import com.diabetesapp.JwtUtil;
import com.diabetesapp.model.User;
import com.diabetesapp.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepo;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // Refresh token store — swap for DB/Redis in production
    private final ConcurrentHashMap<String, Long> refreshTokenStore = new ConcurrentHashMap<>();

    @Data
    public static class SignupRequest {
        @NotBlank private String name;
        @Email @NotBlank private String email;
        @NotBlank private String password;
    }

    @Data
    public static class LoginRequest {
        @Email @NotBlank private String email;
        @NotBlank private String password;
    }
    // Add this method to AuthController.java inside the class

    @GetMapping("/me")
    public ResponseEntity<?> me(jakarta.servlet.http.HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "No token"));
        }
        try {
            Long userId = jwtUtil.getUserId(header.substring(7));
            return userRepo.findById(userId)
                    .map(user -> ResponseEntity.ok(userInfo(user)))
                    .orElse(ResponseEntity.status(404).body(null));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest req,
                                    HttpServletResponse response) {
        if (userRepo.findByEmail(req.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already in use"));
        }

        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        userRepo.save(user);

        String accessToken = jwtUtil.generateToken(user.getId(), user.getEmail());
        String refreshToken = generateRefreshToken();
        refreshTokenStore.put(refreshToken, user.getId());

        setRefreshCookie(response, refreshToken);

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "user", userInfo(user)
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req,
                                   HttpServletResponse response) {
        Optional<User> found = userRepo.findByEmail(req.getEmail());

        if (found.isEmpty() || !passwordEncoder.matches(req.getPassword(), found.get().getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        User user = found.get();

        String accessToken = jwtUtil.generateToken(user.getId(), user.getEmail());
        String refreshToken = generateRefreshToken();
        refreshTokenStore.put(refreshToken, user.getId());

        setRefreshCookie(response, refreshToken);

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "user", userInfo(user)
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request,
                                     HttpServletResponse response) {
        String refreshToken = extractRefreshCookie(request);

        if (refreshToken == null || !refreshTokenStore.containsKey(refreshToken)) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid refresh token"));
        }

        Long userId = refreshTokenStore.get(refreshToken);
        Optional<User> found = userRepo.findById(userId);

        if (found.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "User not found"));
        }

        // Rotate refresh token
        refreshTokenStore.remove(refreshToken);
        String newRefreshToken = generateRefreshToken();
        refreshTokenStore.put(newRefreshToken, userId);

        String newAccessToken = jwtUtil.generateToken(userId, found.get().getEmail());
        setRefreshCookie(response, newRefreshToken);

        return ResponseEntity.ok(Map.of(
                "accessToken", newAccessToken,
                "user", userInfo(found.get())
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request,
                                    HttpServletResponse response) {
        String refreshToken = extractRefreshCookie(request);
        if (refreshToken != null) {
            refreshTokenStore.remove(refreshToken);
        }
        response.addHeader("Set-Cookie",
        "refreshToken=; HttpOnly; Secure; SameSite=None; Path=/; Max-Age=0");



        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    private String generateRefreshToken() {
        return UUID.randomUUID().toString().replace("-", "") +
               UUID.randomUUID().toString().replace("-", "");
    }

    private String extractRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> "refreshToken".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private Map<String, Object> userInfo(User user) {
        return Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail()
        );
    }
}