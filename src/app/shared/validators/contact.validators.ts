import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Valide un numéro de téléphone international simplifié
 * (chiffres, espaces, +, -, entre 8 et 15 chiffres significatifs).
 * Partagé entre les modules Clients et Fournisseurs.
 */
export function phoneNumberValidator(): ValidatorFn {
  const pattern = /^\+?[0-9\s-]{8,15}$/;
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) {
      return null;
    }
    return pattern.test(control.value) ? null : { phoneNumber: true };
  };
}

/**
 * Valide un identifiant fiscal (NINEA/NIF) — chiffres et lettres, 6 à 20
 * caractères. Le format exact doit être aligné avec la règle du backend.
 * Partagé entre les modules Clients et Fournisseurs.
 */
export function taxIdValidator(): ValidatorFn {
  const pattern = /^[A-Za-z0-9-]{6,20}$/;
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) {
      return null;
    }
    return pattern.test(control.value) ? null : { taxId: true };
  };
}
