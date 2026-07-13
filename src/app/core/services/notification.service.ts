import { Injectable, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

export type NotificationType = 'success' | 'error' | 'warning' | 'info';

/**
 * Point d'entrée unique pour les notifications toast de l'application.
 * S'appuie sur `MatSnackBar` et applique une classe CSS par type pour styliser
 * chaque variante (voir `src/styles.scss`).
 */
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly snackBar = inject(MatSnackBar);

  success(message: string): void {
    this.show(message, 'success');
  }

  error(message: string): void {
    this.show(message, 'error', 6000);
  }

  warning(message: string): void {
    this.show(message, 'warning');
  }

  info(message: string): void {
    this.show(message, 'info');
  }

  private show(message: string, type: NotificationType, durationMs = 4000): void {
    this.snackBar.open(message, 'Fermer', {
      duration: durationMs,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: [`toast-${type}`],
    });
  }
}
