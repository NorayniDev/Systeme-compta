import { Injectable, signal } from '@angular/core';

/**
 * Compteur de requêtes HTTP en vol, utilisé par `loadingInterceptor` pour
 * piloter un indicateur de chargement global (barre de progression du layout).
 */
@Injectable({ providedIn: 'root' })
export class LoadingService {
  private readonly pendingRequests = signal(0);

  readonly isLoading = signal(false);

  start(): void {
    this.pendingRequests.update((count) => count + 1);
    this.isLoading.set(true);
  }

  stop(): void {
    this.pendingRequests.update((count) => Math.max(0, count - 1));
    this.isLoading.set(this.pendingRequests() > 0);
  }
}
