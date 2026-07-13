package com.facturationpme.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.facturationpme.auth.dto.LoginRequest;
import com.facturationpme.auth.dto.LoginResponse;
import com.facturationpme.common.exception.ResourceNotFoundException;
import com.facturationpme.common.security.AppUserPrincipal;
import com.facturationpme.common.security.JwtService;
import com.facturationpme.users.domain.RolePermissions;
import com.facturationpme.users.domain.User;
import com.facturationpme.users.domain.UserRole;
import com.facturationpme.users.dto.UserResponse;
import com.facturationpme.users.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private AuthenticationManager authenticationManager;
  @Mock private JwtService jwtService;
  @Mock private RefreshTokenService refreshTokenService;
  @Mock private UserRepository userRepository;
  @Mock private ApplicationEventPublisher eventPublisher;

  private AuthService authService;
  private User user;
  private UUID userId;

  @BeforeEach
  void setUp() {
    authService =
        new AuthService(
            authenticationManager, jwtService, refreshTokenService, userRepository, eventPublisher);
    userId = UUID.randomUUID();
    user =
        User.builder()
            .id(userId)
            .firstName("Admin")
            .lastName("Demo")
            .email("admin@facturation-pme.sn")
            .role(UserRole.ADMIN)
            .active(true)
            .build();
  }

  @Test
  void loginShouldWrapAuthenticationFailureInBusinessMessage() {
    when(authenticationManager.authenticate(any()))
        .thenThrow(new BadCredentialsException("Bad credentials"));

    assertThatThrownBy(
            () -> authService.login(new LoginRequest("admin@facturation-pme.sn", "wrong", false)))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage("Email ou mot de passe incorrect.");
  }

  @Test
  void loginShouldUpdateLastLoginAtAndReturnTokens() {
    AppUserPrincipal principal = new AppUserPrincipal(user, RolePermissions.of(UserRole.ADMIN));
    var authentication =
        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    when(authenticationManager.authenticate(any())).thenReturn(authentication);
    when(jwtService.generateAccessToken(principal)).thenReturn("access-token");
    when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(900L);
    when(refreshTokenService.issue(userId)).thenReturn("refresh-token");

    LoginResponse response =
        authService.login(new LoginRequest("admin@facturation-pme.sn", "Admin123!", false));

    assertThat(response.accessToken()).isEqualTo("access-token");
    assertThat(response.refreshToken()).isEqualTo("refresh-token");
    assertThat(response.expiresIn()).isEqualTo(900L);
    assertThat(user.getLastLoginAt()).isNotNull();
    verify(userRepository).save(user);
  }

  @Test
  void refreshShouldRotateTokenAndReturnNewSession() {
    when(refreshTokenService.rotate("raw-refresh")).thenReturn(userId);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(jwtService.generateAccessToken(any())).thenReturn("new-access-token");
    when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(900L);
    when(refreshTokenService.issue(userId)).thenReturn("new-refresh-token");

    LoginResponse response = authService.refresh("raw-refresh");

    assertThat(response.accessToken()).isEqualTo("new-access-token");
    assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
  }

  @Test
  void refreshShouldThrowWhenUserNoLongerExists() {
    when(refreshTokenService.rotate(anyString())).thenReturn(userId);
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.refresh("raw-refresh"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void meShouldReturnUserResponseWhenFound() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    UserResponse response = authService.me(userId);

    assertThat(response.email()).isEqualTo("admin@facturation-pme.sn");
  }

  @Test
  void meShouldThrowWhenUserNotFound() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.me(userId)).isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void logoutShouldRevokeAllRefreshTokensForUser() {
    authService.logout(userId);

    verify(refreshTokenService).revokeAllForUser(userId);
  }
}
