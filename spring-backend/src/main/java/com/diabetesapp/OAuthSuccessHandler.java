package com.diabetesapp;

import com.diabetesapp.model.User;
import com.diabetesapp.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;

    @Value("${cors.allowed-origins:http://localhost:5173}")
    private String allowedOriginsRaw;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String email = oauthUser.getAttribute("email");
        String name  = oauthUser.getAttribute("name");

        // Find or create the user
        User user = userRepo.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(name != null ? name : email);
            newUser.setPasswordHash(""); // no password for OAuth users
            return userRepo.save(newUser);
        });

        // Generate real JWT
        String accessToken = jwtUtil.generateToken(user.getId(), user.getEmail());

        // Redirect to Vue frontend with token as query param
        // Vue will pick it up, store in auth store, then redirect to /onboarding or /dashboard
        String frontendUrl = allowedOriginsRaw.split(",")[0].trim();
        boolean isNewUser = user.getPhone() == null && user.getDiabetesType() == null;
        String redirectPath = isNewUser ? "/onboarding" : "/dashboard";

        response.sendRedirect(frontendUrl + "/oauth-callback?token=" + accessToken + "&redirect=" + redirectPath);
    }
}