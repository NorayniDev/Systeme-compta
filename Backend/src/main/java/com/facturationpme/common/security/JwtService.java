package com.facturationpme.common.security;

import com.facturationpme.common.config.JwtProperties;
import com.facturationpme.users.domain.Permission;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Emission/validation du JWT d'acces uniquement. Le refresh token est un secret opaque revocable
 * stocke en base (voir {@code auth.RefreshTokenService}) - il ne transite jamais sous forme de JWT.
 */
@Service
@RequiredArgsConstructor
public class JwtService {

  private static final String CLAIM_EMAIL = "email";
  private static final String CLAIM_ROLE = "role";
  private static final String CLAIM_PERMISSIONS = "permissions";

  private final JwtProperties jwtProperties;

  public String generateAccessToken(AppUserPrincipal principal) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + jwtProperties.accessTokenExpirationMs());
    List<String> permissionValues =
        principal.getPermissions().stream().map(Permission::getValue).toList();

    return Jwts.builder()
        .subject(principal.getId().toString())
        .claim(CLAIM_EMAIL, principal.getUsername())
        .claim(CLAIM_ROLE, principal.getUser().getRole().name())
        .claim(CLAIM_PERMISSIONS, permissionValues)
        .issuedAt(now)
        .expiration(expiry)
        .signWith(signingKey())
        .compact();
  }

  public long getAccessTokenExpirationSeconds() {
    return jwtProperties.accessTokenExpirationMs() / 1000;
  }

  public Claims parseAndValidate(String token) throws JwtException {
    return Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(token).getPayload();
  }

  private SecretKey signingKey() {
    return Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
  }
}
