package com.facturationpme.auth.service;

import com.facturationpme.audit.domain.AuditAction;
import com.facturationpme.audit.event.AuditableActionEvent;
import com.facturationpme.auth.dto.LoginRequest;
import com.facturationpme.auth.dto.LoginResponse;
import com.facturationpme.common.exception.ResourceNotFoundException;
import com.facturationpme.common.security.AppUserPrincipal;
import com.facturationpme.common.security.JwtService;
import com.facturationpme.users.domain.RolePermissions;
import com.facturationpme.users.domain.User;
import com.facturationpme.users.dto.UserResponse;
import com.facturationpme.users.repository.UserRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final RefreshTokenService refreshTokenService;
  private final UserRepository userRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public LoginResponse login(LoginRequest request) {
    var authentication = authenticate(request);
    AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
    User user = principal.getUser();
    user.setLastLoginAt(Instant.now());
    userRepository.save(user);
    eventPublisher.publishEvent(
        new AuditableActionEvent(AuditAction.LOGIN, "Auth", "Connexion", user.getId()));

    return buildLoginResponse(principal);
  }

  @Transactional
  public LoginResponse refresh(String rawRefreshToken) {
    UUID userId = refreshTokenService.rotate(rawRefreshToken);
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", userId));
    AppUserPrincipal principal = new AppUserPrincipal(user, RolePermissions.of(user.getRole()));
    return buildLoginResponse(principal);
  }

  public UserResponse me(UUID userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", userId));
    return UserResponse.from(user);
  }

  @Transactional
  public void logout(UUID userId) {
    refreshTokenService.revokeAllForUser(userId);
  }

  private org.springframework.security.core.Authentication authenticate(LoginRequest request) {
    try {
      return authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.email(), request.password()));
    } catch (AuthenticationException ex) {
      // Traduit le message d'authentification (potentiellement non localise selon la locale du
      // conteneur) par un message metier explicite, sans reveler si c'est l'email ou le mot de
      // passe qui est incorrect.
      throw new BadCredentialsException("Email ou mot de passe incorrect.");
    }
  }

  private LoginResponse buildLoginResponse(AppUserPrincipal principal) {
    String accessToken = jwtService.generateAccessToken(principal);
    String refreshToken = refreshTokenService.issue(principal.getId());
    return new LoginResponse(
        accessToken,
        refreshToken,
        jwtService.getAccessTokenExpirationSeconds(),
        UserResponse.from(principal.getUser()));
  }
}
