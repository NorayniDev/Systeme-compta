package com.facturationpme.users.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.facturationpme.auth.service.RefreshTokenService;
import com.facturationpme.common.exception.DuplicateResourceException;
import com.facturationpme.common.exception.InvalidStateException;
import com.facturationpme.common.exception.ResourceNotFoundException;
import com.facturationpme.users.domain.User;
import com.facturationpme.users.domain.UserRole;
import com.facturationpme.users.dto.UserAccountCreateDto;
import com.facturationpme.users.dto.UserAccountUpdateDto;
import com.facturationpme.users.dto.UserResponse;
import com.facturationpme.users.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private RefreshTokenService refreshTokenService;
  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private UserAccountService userAccountService;

  private User user;
  private UUID userId;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    user =
        User.builder()
            .id(userId)
            .firstName("Awa")
            .lastName("Ndiaye")
            .email("awa.ndiaye@facturation-pme.sn")
            .passwordHash("hash")
            .role(UserRole.GESTIONNAIRE)
            .active(true)
            .build();
  }

  @Test
  void createShouldRejectDuplicateEmail() {
    UserAccountCreateDto dto =
        new UserAccountCreateDto(
            "Awa", "Ndiaye", "awa.ndiaye@facturation-pme.sn", UserRole.GESTIONNAIRE, "Passw0rd!");
    when(userRepository.existsByEmailIgnoreCase(dto.email())).thenReturn(true);

    assertThatThrownBy(() -> userAccountService.create(dto))
        .isInstanceOf(DuplicateResourceException.class);
  }

  @Test
  void createShouldHashTemporaryPasswordAndPersistActiveUser() {
    UserAccountCreateDto dto =
        new UserAccountCreateDto(
            "Awa", "Ndiaye", "awa.ndiaye@facturation-pme.sn", UserRole.GESTIONNAIRE, "Passw0rd!");
    when(userRepository.existsByEmailIgnoreCase(dto.email())).thenReturn(false);
    when(passwordEncoder.encode("Passw0rd!")).thenReturn("hashed-value");
    when(userRepository.save(ArgumentMatchers.any(User.class)))
        .thenAnswer(
            inv -> {
              User argument = inv.getArgument(0);
              argument.setId(UUID.randomUUID());
              return argument;
            });

    UserResponse response = userAccountService.create(dto);

    assertThat(response.isActive()).isTrue();
    assertThat(response.role()).isEqualTo(UserRole.GESTIONNAIRE);
    verify(passwordEncoder).encode("Passw0rd!");
  }

  @Test
  void findByIdShouldThrowWhenMissing() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userAccountService.findById(userId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void findByIdShouldReturnUserWhenFound() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    UserResponse response = userAccountService.findById(userId);

    assertThat(response.email()).isEqualTo("awa.ndiaye@facturation-pme.sn");
  }

  @Test
  void searchShouldMapRepositoryPageToResponsePage() {
    Pageable pageable = PageRequest.of(0, 10);
    when(userRepository.findAll(ArgumentMatchers.<Specification<User>>any(), eq(pageable)))
        .thenReturn(new PageImpl<>(List.of(user)));

    Page<UserResponse> result = userAccountService.search("awa", pageable);

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).email()).isEqualTo("awa.ndiaye@facturation-pme.sn");
  }

  @Test
  void updateShouldRejectEmailAlreadyUsedByAnotherUser() {
    UserAccountUpdateDto dto =
        new UserAccountUpdateDto(
            "Awa", "Ndiaye", "other@facturation-pme.sn", UserRole.GESTIONNAIRE, true);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userRepository.existsByEmailIgnoreCase("other@facturation-pme.sn")).thenReturn(true);

    assertThatThrownBy(() -> userAccountService.update(userId, dto))
        .isInstanceOf(DuplicateResourceException.class);
  }

  @Test
  void updateShouldPersistChangesAndRevokeTokensWhenDeactivated() {
    UserAccountUpdateDto dto =
        new UserAccountUpdateDto(
            "Awa", "Ndiaye", "awa.ndiaye@facturation-pme.sn", UserRole.GESTIONNAIRE, false);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userRepository.save(user)).thenReturn(user);

    UserResponse response = userAccountService.update(userId, dto);

    assertThat(response.isActive()).isFalse();
    verify(refreshTokenService).revokeAllForUser(userId);
  }

  @Test
  void updateShouldRejectDemotingTheLastActiveAdmin() {
    user.setRole(UserRole.ADMIN);
    UserAccountUpdateDto dto =
        new UserAccountUpdateDto(
            "Awa", "Ndiaye", "awa.ndiaye@facturation-pme.sn", UserRole.GESTIONNAIRE, true);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userRepository.countByRoleAndActiveTrueAndIdNot(UserRole.ADMIN, userId)).thenReturn(0L);

    assertThatThrownBy(() -> userAccountService.update(userId, dto))
        .isInstanceOf(InvalidStateException.class);
    verify(userRepository, never()).save(ArgumentMatchers.any(User.class));
  }

  @Test
  void updateShouldAllowDemotingAdminWhenAnotherActiveAdminExists() {
    user.setRole(UserRole.ADMIN);
    UserAccountUpdateDto dto =
        new UserAccountUpdateDto(
            "Awa", "Ndiaye", "awa.ndiaye@facturation-pme.sn", UserRole.GESTIONNAIRE, true);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userRepository.countByRoleAndActiveTrueAndIdNot(UserRole.ADMIN, userId)).thenReturn(1L);
    when(userRepository.save(user)).thenReturn(user);

    UserResponse response = userAccountService.update(userId, dto);

    assertThat(response.role()).isEqualTo(UserRole.GESTIONNAIRE);
  }

  @Test
  void deleteShouldRejectDeletingTheLastActiveAdmin() {
    user.setRole(UserRole.ADMIN);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userRepository.countByRoleAndActiveTrueAndIdNot(UserRole.ADMIN, userId)).thenReturn(0L);

    assertThatThrownBy(() -> userAccountService.delete(userId))
        .isInstanceOf(InvalidStateException.class);
    verify(userRepository, never()).delete(ArgumentMatchers.any(User.class));
  }

  @Test
  void deleteShouldRemoveExistingUserAndRevokeTokens() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    userAccountService.delete(userId);

    verify(userRepository).delete(user);
    verify(refreshTokenService).revokeAllForUser(userId);
  }

  @Test
  void deleteShouldThrowWhenUserMissing() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userAccountService.delete(userId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void resetPasswordShouldHashNewPasswordAndRevokeTokens() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(passwordEncoder.encode(ArgumentMatchers.anyString())).thenReturn("new-hash");

    userAccountService.resetPassword(userId);

    verify(userRepository).save(user);
    assertThat(user.getPasswordHash()).isEqualTo("new-hash");
    verify(refreshTokenService).revokeAllForUser(userId);
  }
}
