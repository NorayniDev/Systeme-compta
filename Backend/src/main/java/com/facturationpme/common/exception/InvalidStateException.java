package com.facturationpme.common.exception;

/**
 * Levee quand une operation est refusee a cause de l'etat courant de la ressource (ex: valider une
 * facture deja payee), pas d'un defaut de permission - mappee en 409 avec le message explicatif,
 * pas en 403 generique qui laisserait croire a un probleme d'habilitation.
 */
public class InvalidStateException extends RuntimeException {

  public InvalidStateException(String message) {
    super(message);
  }
}
