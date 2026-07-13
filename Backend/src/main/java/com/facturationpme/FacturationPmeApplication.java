package com.facturationpme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "securityAuditorAware")
@ConfigurationPropertiesScan
public class FacturationPmeApplication {

  public static void main(String[] args) {
    SpringApplication.run(FacturationPmeApplication.class, args);
  }
}
