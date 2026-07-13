package com.facturationpme.common.security;

import com.facturationpme.users.domain.Permission;
import com.facturationpme.users.domain.User;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Adaptateur Spring Security au-dessus de l'entite {@link User} : le domaine ne depend d'aucune API
 * de securite, seule cette classe fait le pont.
 */
public class AppUserPrincipal implements UserDetails {

  private final User user;
  private final Set<Permission> permissions;

  public AppUserPrincipal(User user, Set<Permission> permissions) {
    this.user = user;
    this.permissions = permissions;
  }

  public UUID getId() {
    return user.getId();
  }

  public User getUser() {
    return user;
  }

  public Set<Permission> getPermissions() {
    return permissions;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    List<GrantedAuthority> authorities =
        new java.util.ArrayList<>(
            permissions.stream().map(p -> new SimpleGrantedAuthority(p.getValue())).toList());
    authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    return authorities;
  }

  @Override
  public String getPassword() {
    return user.getPasswordHash();
  }

  @Override
  public String getUsername() {
    return user.getEmail();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return user.isActive();
  }
}
