package com.facturationpme.auth.service;

import com.facturationpme.users.domain.User;

/**
 * Abstraction du canal d'envoi du lien de reinitialisation de mot de passe. Une seule
 * implementation existe pour l'instant ({@link LoggingPasswordResetNotifier}) en attendant
 * l'integration d'un vrai fournisseur d'emailing.
 */
public interface PasswordResetNotifier {

  void notifyResetToken(User user, String rawToken);
}
