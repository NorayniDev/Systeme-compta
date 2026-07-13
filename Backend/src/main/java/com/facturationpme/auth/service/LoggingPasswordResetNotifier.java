package com.facturationpme.auth.service;

import com.facturationpme.users.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Implementation provisoire : journalise le jeton au lieu de l'envoyer par email. A remplacer par
 * un adaptateur SMTP/API (SendGrid, SES, ...) avant la mise en production - ne jamais journaliser
 * de jetons reels hors dev.
 */
@Component
public class LoggingPasswordResetNotifier implements PasswordResetNotifier {

  private static final Logger LOG = LoggerFactory.getLogger(LoggingPasswordResetNotifier.class);

  @Override
  public void notifyResetToken(User user, String rawToken) {
    LOG.info("[DEV ONLY] Lien de reinitialisation pour {} : token={}", user.getEmail(), rawToken);
  }
}
