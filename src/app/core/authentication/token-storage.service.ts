import { Injectable } from '@angular/core';
import { jwtDecode } from 'jwt-decode';
import { IJwtPayload } from '../models/auth.model';
import { STORAGE_KEYS } from '../constants/storage-keys.constant';

/**
 * Encapsule la persistance des tokens JWT.
 * Utilise `localStorage` lorsque "Se souvenir de moi" est actif, sinon
 * `sessionStorage` (le token est alors effacé à la fermeture de l'onglet).
 *
 * OWASP: en production, préférer un cookie HttpOnly + Secure géré par le
 * backend pour le refresh token. Le stockage Web Storage est utilisé ici
 * pour simplifier l'intégration avec une API REST stateless côté Spring Boot.
 */
@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  private getStorage(): Storage {
    const remember = localStorage.getItem(STORAGE_KEYS.REMEMBER_ME) === 'true';
    return remember ? localStorage : sessionStorage;
  }

  saveTokens(accessToken: string, refreshToken: string, rememberMe: boolean): void {
    localStorage.setItem(STORAGE_KEYS.REMEMBER_ME, String(rememberMe));
    const storage = rememberMe ? localStorage : sessionStorage;
    storage.setItem(STORAGE_KEYS.ACCESS_TOKEN, accessToken);
    storage.setItem(STORAGE_KEYS.REFRESH_TOKEN, refreshToken);
  }

  getAccessToken(): string | null {
    return this.getStorage().getItem(STORAGE_KEYS.ACCESS_TOKEN);
  }

  getRefreshToken(): string | null {
    return this.getStorage().getItem(STORAGE_KEYS.REFRESH_TOKEN);
  }

  clear(): void {
    for (const storage of [localStorage, sessionStorage]) {
      storage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
      storage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
      storage.removeItem(STORAGE_KEYS.CURRENT_USER);
    }
    localStorage.removeItem(STORAGE_KEYS.REMEMBER_ME);
  }

  decodeToken(token: string): IJwtPayload | null {
    try {
      return jwtDecode<IJwtPayload>(token);
    } catch {
      return null;
    }
  }

  isTokenExpired(token: string): boolean {
    const payload = this.decodeToken(token);
    if (!payload) {
      return true;
    }
    return payload.exp * 1000 < Date.now();
  }
}
