package com.facturationpme.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Verifie que les dependances entre couches ne remontent jamais a contre-sens (web -> service ->
 * repository/domain), independamment du module metier.
 */
class LayeringArchitectureTest {

  private static JavaClasses importedClasses;

  @BeforeAll
  static void importClasses() {
    importedClasses =
        new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.facturationpme");
  }

  @Test
  void controllersShouldNotAccessRepositoriesDirectly() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage("..web..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..repository..");
    rule.check(importedClasses);
  }

  @Test
  void domainShouldNotDependOnServiceOrWebLayers() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..service..", "..web..");
    rule.check(importedClasses);
  }

  @Test
  void repositoriesShouldOnlyBeAccessedByServices() {
    ArchRule rule =
        classes()
            .that()
            .resideInAPackage("..repository..")
            .and()
            .areNotInterfaces()
            .should()
            .onlyBeAccessed()
            .byAnyPackage("..repository..", "..service..");
    rule.check(importedClasses);
  }
}
