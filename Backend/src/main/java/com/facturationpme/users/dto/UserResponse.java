package com.facturationpme.users.dto;

import com.facturationpme.users.domain.Permission;
import com.facturationpme.users.domain.RolePermissions;
import com.facturationpme.users.domain.User;
import com.facturationpme.users.domain.UserRole;
import java.time.Instant;
import java.util.Set;

/** Reponse alignee sur {@code IUser} (core/models/user.model.ts). */
public record UserResponse(
    String id,
    String firstName,
    String lastName,
    String email,
    UserRole role,
    Set<Permission> permissions,
    String avatarUrl,
    boolean isActive,
    Instant lastLoginAt,
    Instant createdAt) {

  public static UserResponse from(User user) {
    return new UserResponse(
        user.getId().toString(),
        user.getFirstName(),
        user.getLastName(),
        user.getEmail(),
        user.getRole(),
        RolePermissions.of(user.getRole()),
        user.getAvatarUrl(),
        user.isActive(),
        user.getLastLoginAt(),
        user.getCreatedAt());
  }
}
