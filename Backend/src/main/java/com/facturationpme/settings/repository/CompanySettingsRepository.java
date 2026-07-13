package com.facturationpme.settings.repository;

import com.facturationpme.settings.domain.CompanySettings;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanySettingsRepository extends JpaRepository<CompanySettings, UUID> {}
