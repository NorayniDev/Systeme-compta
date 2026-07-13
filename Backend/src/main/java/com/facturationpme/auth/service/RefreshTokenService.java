package com.facturationpme.auth.service;

import com.facturationpme.auth.domain.RefreshToken;
import com.facturationpme.auth.repository.RefreshTokenRepository;
import com.facturationpme.common.config.JwtProperties;
import com.facturationpme.common.security.TokenHasher;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtProperties jwtProperties;

  @Transactional
  public String issue(UUID userId) {
    String rawToken = TokenHasher.generateOpaqueToken();
    RefreshToken refreshToken =
        RefreshToken.builder()
            .userId(userId)
            .tokenHash(TokenHasher.hash(rawToken))
            .expiresAt(Instant.now().plusMillis(jwtProperties.refreshTokenExpirationMs()))
            .revoked(false)
            .createdAt(Instant.now())
            .build();
    refreshTokenRepository.save(refreshToken);
    return rawToken;
  }

  /**
   * Valide le refresh token presente, le revoque (rotation a usage unique) et renvoie l'utilisateur
   * associe.
   */
  @Transactional
  public UUID rotate(String rawToken) {
    RefreshToken refreshToken =
        refreshTokenRepository
            .findByTokenHash(TokenHasher.hash(rawToken))
            .orElseThrow(() -> new BadCredentialsException("Refresh token invalide."));

    if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(Instant.now())) {
      throw new BadCredentialsException("Refresh token expire ou revoque.");
    }

    refreshToken.setRevoked(true);
    refreshTokenRepository.save(refreshToken);
    return refreshToken.getUserId();
  }

  @Transactional
  public void revokeAllForUser(UUID userId) {
    refreshTokenRepository.revokeAllForUser(userId);
  }
}
