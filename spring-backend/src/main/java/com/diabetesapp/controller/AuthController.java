package com.diabetesapp.controller;

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
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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

        String accessToken = generateToken();
        String refreshToken = generateToken();
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
        String accessToken = generateToken();
        String refreshToken = generateToken();
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
        String newRefreshToken = generateToken();
        refreshTokenStore.put(newRefreshToken, userId);

        String newAccessToken = generateToken();
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

        // Clear the cookie
        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }


    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "") +
            UUID.randomUUID().toString().replace("-", "");
    }

    private void setRefreshCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("refreshToken", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        // cookie.setSecure(true); // uncomment in production (HTTPS)
        response.addCookie(cookie);
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
