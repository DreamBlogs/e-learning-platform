package com.example.learning.auth.api;

import com.example.learning.auth.application.AuthService;
import com.example.learning.common.api.ApiResponse;
import com.example.learning.common.application.CurrentUser;
import com.example.learning.common.application.CurrentUserProvider;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CurrentUserProvider currentUserProvider;

    public AuthController(AuthService authService, CurrentUserProvider currentUserProvider) {
        this.authService = authService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> me() {
        CurrentUser user = currentUserProvider.get();
        return ApiResponse.ok(new CurrentUserResponse(user.id(), user.email()));
    }

    @GetMapping("/profile")
    public ApiResponse<ProfileResponse> getProfile() {
        return ApiResponse.ok(authService.getProfile(currentUserProvider.get().id()));
    }

    @PatchMapping("/profile")
    public ApiResponse<ProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.ok(authService.updateProfile(currentUserProvider.get().id(), request));
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(currentUserProvider.get().id(), request);
        return ApiResponse.ok(null);
    }
}
