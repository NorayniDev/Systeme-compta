package com.facturationpme.common.security;

import com.facturationpme.users.domain.Permission;
import com.facturationpme.users.domain.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";
  private static final Logger LOG = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

  private final JwtService jwtService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String token = extractToken(request);

    if (StringUtils.hasText(token)
        && SecurityContextHolder.getContext().getAuthentication() == null) {
      try {
        Claims claims = jwtService.parseAndValidate(token);
        JwtPrincipal principal = toPrincipal(claims);
        List<GrantedAuthority> authorities = toAuthorities(principal);

        var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (JwtException | IllegalArgumentException ex) {
        LOG.debug("Jeton JWT invalide ou expire : {}", ex.getMessage());
        SecurityContextHolder.clearContext();
      }
    }

    filterChain.doFilter(request, response);
  }

  private String extractToken(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
      return header.substring(BEARER_PREFIX.length());
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private JwtPrincipal toPrincipal(Claims claims) {
    UUID userId = UUID.fromString(claims.getSubject());
    String email = claims.get("email", String.class);
    UserRole role = UserRole.valueOf(claims.get("role", String.class));
    List<String> permissionValues = claims.get("permissions", List.class);
    Set<Permission> permissions =
        permissionValues.stream().map(Permission::fromValue).collect(Collectors.toSet());
    return new JwtPrincipal(userId, email, role, permissions);
  }

  private List<GrantedAuthority> toAuthorities(JwtPrincipal principal) {
    List<GrantedAuthority> authorities =
        principal.permissions().stream()
            .map(p -> (GrantedAuthority) new SimpleGrantedAuthority(p.getValue()))
            .collect(Collectors.toCollection(java.util.ArrayList::new));
    authorities.add(new SimpleGrantedAuthority("ROLE_" + principal.role().name()));
    return authorities;
  }
}
