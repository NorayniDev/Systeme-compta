package com.facturationpme.settings.web;

import com.facturationpme.settings.dto.CompanySettingsDto;
import com.facturationpme.settings.service.CompanySettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/settings/company")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('settings:manage')")
public class CompanySettingsController {

  private final CompanySettingsService companySettingsService;

  @GetMapping
  public CompanySettingsDto get() {
    return companySettingsService.get();
  }

  @PutMapping
  public CompanySettingsDto update(@Valid @RequestBody CompanySettingsDto dto) {
    return companySettingsService.update(dto);
  }
}
