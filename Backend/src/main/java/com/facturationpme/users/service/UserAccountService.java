package com.facturationpme.users.service;

import com.facturationpme.audit.domain.AuditAction;
import com.facturationpme.audit.event.AuditableActionEvent;
import com.facturationpme.auth.service.RefreshTokenService;
import com.facturationpme.common.exception.DuplicateResourceException;
import com.facturationpme.common.exception.InvalidStateException;
import com.facturationpme.common.exception.ResourceNotFoundException;
import com.facturationpme.common.security.SecurityUtils;
import com.facturationpme.common.security.TokenHasher;
import com.facturationpme.users.domain.User;
import com.facturationpme.users.domain.UserRole;
import com.facturationpme.users.dto.UserAccountCreateDto;
import com.facturationpme.users.dto.UserAccountUpdateDto;
import com.facturationpme.users.dto.UserResponse;
import com.facturationpme.users.repository.UserRepository;
import com.facturationpme.users.repository.UserSpecifications;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAccountService {

  private static final Logger LOG = LoggerFactory.getLogger(UserAccountService.class);

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final RefreshTokenService refreshTokenService;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional(readOnly = true)
  public Page<UserResponse> search(String query, Pageable pageable) {
    Specification<User> specification =
        Specification.where(UserSpecifications.matchingQuery(query));
    return userRepository.findAll(specification, pageable).map(UserResponse::from);
  }

  @Transactional(readOnly = true)
  public UserResponse findById(UUID id) {
    return UserResponse.from(getOrThrow(id));
  }

  @Transactional
  public UserResponse create(UserAccountCreateDto dto) {
    if (userRepository.existsByEmailIgnoreCase(dto.email())) {
      throw new DuplicateResourceException(
          "Un utilisateur utilise deja l'adresse email : " + dto.email());
    }
    User user =
        User.builder()
            .firstName(dto.firstName())
            .lastName(dto.lastName())
            .email(dto.email())
            .passwordHash(passwordEncoder.encode(dto.temporaryPassword()))
            .role(dto.role())
            .active(true)
            .build();
    User saved = userRepository.save(user);
    eventPublisher.publishEvent(
        new AuditableActionEvent(
            AuditAction.CREATE,
            "Utilisateur",
            saved.getFirstName() + " " + saved.getLastName(),
            SecurityUtils.currentUserId()));
    return UserResponse.from(saved);
  }

  @Transactional
  public UserResponse update(UUID id, UserAccountUpdateDto dto) {
    User user = getOrThrow(id);
    if (!user.getEmail().equalsIgnoreCase(dto.email())
        && userRepository.existsByEmailIgnoreCase(dto.email())) {
      throw new DuplicateResourceException(
          "Un utilisateur utilise deja l'adresse email : " + dto.email());
    }
    boolean losesActiveAdminStatus =
        user.getRole() == UserRole.ADMIN
            && user.isActive()
            && (dto.role() != UserRole.ADMIN || !dto.isActive());
    if (losesActiveAdminStatus && isLastActiveAdmin(id)) {
      throw new InvalidStateException(
          "Impossible de retirer les droits administrateur au dernier administrateur actif.");
    }

    boolean activationChanged = user.isActive() != dto.isActive();
    user.setFirstName(dto.firstName());
    user.setLastName(dto.lastName());
    user.setEmail(dto.email());
    user.setRole(dto.role());
    user.setActive(dto.isActive());
    User saved = userRepository.save(user);

    if (!dto.isActive()) {
      refreshTokenService.revokeAllForUser(id);
    }
    String details =
        activationChanged ? (dto.isActive() ? "Compte reactive" : "Compte desactive") : null;
    eventPublisher.publishEvent(
        new AuditableActionEvent(
            AuditAction.UPDATE,
            "Utilisateur",
            saved.getFirstName() + " " + saved.getLastName(),
            details,
            SecurityUtils.currentUserId()));
    return UserResponse.from(saved);
  }

  @Transactional
  public void delete(UUID id) {
    User user = getOrThrow(id);
    if (user.getRole() == UserRole.ADMIN && user.isActive() && isLastActiveAdmin(id)) {
      throw new InvalidStateException("Impossible de supprimer le dernier administrateur actif.");
    }
    userRepository.delete(user);
    refreshTokenService.revokeAllForUser(id);
    eventPublisher.publishEvent(
        new AuditableActionEvent(
            AuditAction.DELETE,
            "Utilisateur",
            user.getFirstName() + " " + user.getLastName(),
            SecurityUtils.currentUserId()));
  }

  /** Reinitialisation par un administrateur : nouveau mot de passe temporaire, sans jeton. */
  @Transactional
  public void resetPassword(UUID id) {
    User user = getOrThrow(id);
    String temporaryPassword = TokenHasher.generateOpaqueToken();
    user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
    userRepository.save(user);
    refreshTokenService.revokeAllForUser(id);
    LOG.info(
        "[DEV ONLY] Nouveau mot de passe temporaire pour {} : {}",
        user.getEmail(),
        temporaryPassword);
    eventPublisher.publishEvent(
        new AuditableActionEvent(
            AuditAction.UPDATE,
            "Utilisateur",
            user.getFirstName() + " " + user.getLastName(),
            "Mot de passe reinitialise",
            SecurityUtils.currentUserId()));
  }

  private boolean isLastActiveAdmin(UUID excludingId) {
    return userRepository.countByRoleAndActiveTrueAndIdNot(UserRole.ADMIN, excludingId) == 0;
  }

  private User getOrThrow(UUID id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", id));
  }
}
