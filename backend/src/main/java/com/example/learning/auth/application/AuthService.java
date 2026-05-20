package com.example.learning.auth.application;

import com.example.learning.auth.api.AuthResponse;
import com.example.learning.auth.api.ChangePasswordRequest;
import com.example.learning.auth.api.LoginRequest;
import com.example.learning.auth.api.ProfileResponse;
import com.example.learning.auth.api.RegisterRequest;
import com.example.learning.auth.api.UpdateProfileRequest;
import com.example.learning.auth.domain.User;
import com.example.learning.auth.infrastructure.JwtTokenService;
import com.example.learning.auth.infrastructure.UserRepository;
import com.example.learning.common.exception.BusinessException;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessException("EMAIL_ALREADY_EXISTS", "Email already exists");
        }

        User user = new User(
                normalizedEmail,
                passwordEncoder.encode(request.password()),
                request.displayName().trim()
        );
        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BusinessException("INVALID_CREDENTIALS", "Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException("INVALID_CREDENTIALS", "Invalid credentials");
        }

        return toResponse(user);
    }

    private AuthResponse toResponse(User user) {
        String token = jwtTokenService.createAccessToken(user.getId(), user.getEmail(), user.getRole());
        return new AuthResponse(user.getId(), user.getEmail(), user.getDisplayName(), token);
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));
        return new ProfileResponse(user.getId(), user.getEmail(), user.getDisplayName());
    }

    @Transactional
    public ProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));
        user.updateDisplayName(request.displayName().trim());
        userRepository.save(user);
        return new ProfileResponse(user.getId(), user.getEmail(), user.getDisplayName());
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException("INVALID_CURRENT_PASSWORD", "Current password is incorrect");
        }
        user.updatePasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }
}
