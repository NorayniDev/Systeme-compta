package com.facturationpme.users.repository;

import com.facturationpme.users.domain.User;
import com.facturationpme.users.domain.UserRole;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

  Optional<User> findByEmailIgnoreCase(String email);

  boolean existsByEmailIgnoreCase(String email);

  long countByRoleAndActiveTrueAndIdNot(UserRole role, UUID id);
}
