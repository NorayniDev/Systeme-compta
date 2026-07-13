import { Injectable, effect, signal } from '@angular/core';
import { STORAGE_KEYS } from '../constants/storage-keys.constant';

export type ThemeMode = 'light' | 'dark';

/**
 * Gère le thème clair/sombre de l'application via un Signal, persisté en
 * `localStorage` et reflété sur `<html>` par la classe `.dark-theme` — cette
 * même classe pilote à la fois les tokens CSS d'Angular Material (M3) et le
 * `darkModeSelector` configuré pour PrimeNG (voir `app.config.ts`).
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly storedMode =
    (localStorage.getItem(STORAGE_KEYS.THEME) as ThemeMode | null) ?? this.systemPreference();

  readonly mode = signal<ThemeMode>(this.storedMode);

  constructor() {
    effect(() => {
      const currentMode = this.mode();
      document.documentElement.classList.toggle('dark-theme', currentMode === 'dark');
      localStorage.setItem(STORAGE_KEYS.THEME, currentMode);
    });
  }

  toggle(): void {
    this.mode.set(this.mode() === 'dark' ? 'light' : 'dark');
  }

  setMode(mode: ThemeMode): void {
    this.mode.set(mode);
  }

  private systemPreference(): ThemeMode {
    return window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }
}
