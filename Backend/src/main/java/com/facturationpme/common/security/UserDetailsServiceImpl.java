package com.facturationpme.common.security;

import com.facturationpme.users.domain.RolePermissions;
import com.facturationpme.users.domain.User;
import com.facturationpme.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl
    implements org.springframework.security.core.userdetails.UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public AppUserPrincipal loadUserByUsername(String email) {
    User user =
        userRepository
            .findByEmailIgnoreCase(email)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + email));
    return new AppUserPrincipal(user, RolePermissions.of(user.getRole()));
  }
}
