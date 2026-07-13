package com.facturationpme.auth.web;

import com.facturationpme.auth.dto.ForgotPasswordRequest;
import com.facturationpme.auth.dto.LoginRequest;
import com.facturationpme.auth.dto.LoginResponse;
import com.facturationpme.auth.dto.RefreshTokenRequest;
import com.facturationpme.auth.dto.ResetPasswordRequest;
import com.facturationpme.auth.service.AuthService;
import com.facturationpme.auth.service.PasswordResetService;
import com.facturationpme.common.security.JwtPrincipal;
import com.facturationpme.users.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final PasswordResetService passwordResetService;

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }

  @PostMapping("/refresh")
  public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
    return ResponseEntity.ok(authService.refresh(request.refreshToken()));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@AuthenticationPrincipal JwtPrincipal principal) {
    authService.logout(principal.userId());
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/me")
  public ResponseEntity<UserResponse> me(@AuthenticationPrincipal JwtPrincipal principal) {
    return ResponseEntity.ok(authService.me(principal.userId()));
  }

  @PostMapping("/forgot-password")
  public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
    passwordResetService.requestReset(request.email());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/reset-password")
  public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    passwordResetService.reset(request.token(), request.newPassword());
    return ResponseEntity.noContent().build();
  }
}
