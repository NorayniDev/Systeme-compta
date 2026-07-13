import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Validateur de groupe vérifiant que deux champs de mot de passe
 * correspondent. L'erreur `passwordMismatch` est posée sur le contrôle de
 * confirmation (et non sur le groupe) afin de pouvoir l'afficher directement
 * sous le champ concerné.
 */
export function passwordsMatchValidator(passwordKey: string, confirmKey: string): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    const password = group.get(passwordKey);
    const confirm = group.get(confirmKey);
    if (!password || !confirm) {
      return null;
    }

    if (confirm.value && confirm.value !== password.value) {
      confirm.setErrors({ ...confirm.errors, passwordMismatch: true });
    } else if (confirm.hasError('passwordMismatch')) {
      const remainingErrors = { ...confirm.errors };
      delete remainingErrors['passwordMismatch'];
      confirm.setErrors(Object.keys(remainingErrors).length > 0 ? remainingErrors : null);
    }

    return null;
  };
}
