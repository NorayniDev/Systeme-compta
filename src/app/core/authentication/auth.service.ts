import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { ApiService } from '../services/api.service';
import { TokenStorageService } from './token-storage.service';
import { API_ENDPOINTS } from '../constants/api-endpoints.constant';
import { STORAGE_KEYS } from '../constants/storage-keys.constant';
import { Permission, UserRole } from '../constants/roles.constant';
import { IUser } from '../models/user.model';
import {
  IForgotPasswordRequest,
  ILoginRequest,
  ILoginResponse,
  IResetPasswordRequest,
} from '../models/auth.model';

/**
 * Source de vérité unique pour l'état d'authentification.
 *
 * Expose l'utilisateur courant et les vérifications RBAC via des Signals,
 * consommés par les guards, la directive `*hasPermission` et les layouts.
 * Toute la logique métier d'authentification vit ici — les composants ne
 * font qu'appeler ce service (aucune logique métier dans les composants).
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = inject(ApiService);
  private readonly tokenStorage = inject(TokenStorageService);

  private readonly currentUserSignal = signal<IUser | null>(this.readPersistedUser());

  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isAuthenticated = computed(() => this.currentUserSignal() !== null);
  readonly role = computed(() => this.currentUserSignal()?.role ?? null);
  readonly permissions = computed(() => this.currentUserSignal()?.permissions ?? []);

  login(request: ILoginRequest): Observable<ILoginResponse> {
    return this.api
      .post<ILoginResponse>(API_ENDPOINTS.AUTH.LOGIN, request)
      .pipe(tap((response) => this.setSession(response, request.rememberMe)));
  }

  logout(): void {
    this.tokenStorage.clear();
    this.currentUserSignal.set(null);
  }

  /**
   * Met à jour localement l'utilisateur courant (ex: après modification du
   * profil) sans repasser par un login complet — persiste aussi la copie
   * locale utilisée au démarrage de l'application.
   */
  updateCurrentUser(patch: Partial<IUser>): void {
    const current = this.currentUserSignal();
    if (!current) {
      return;
    }
    const updated = { ...current, ...patch };
    localStorage.setItem(STORAGE_KEYS.CURRENT_USER, JSON.stringify(updated));
    this.currentUserSignal.set(updated);
  }

  refreshAccessToken(): Observable<ILoginResponse> {
    const refreshToken = this.tokenStorage.getRefreshToken();
    return this.api
      .post<ILoginResponse>(API_ENDPOINTS.AUTH.REFRESH, { refreshToken })
      .pipe(tap((response) => this.setSession(response, this.wasRememberMeEnabled())));
  }

  forgotPassword(request: IForgotPasswordRequest): Observable<void> {
    return this.api.post<void>(API_ENDPOINTS.AUTH.FORGOT_PASSWORD, request);
  }

  resetPassword(request: IResetPasswordRequest): Observable<void> {
    return this.api.post<void>(API_ENDPOINTS.AUTH.RESET_PASSWORD, request);
  }

  hasRole(...roles: UserRole[]): boolean {
    const currentRole = this.role();
    return currentRole !== null && roles.includes(currentRole);
  }

  hasPermission(...required: Permission[]): boolean {
    const granted = this.permissions();
    return required.every((permission) => granted.includes(permission));
  }

  hasAnyPermission(...required: Permission[]): boolean {
    const granted = this.permissions();
    return required.some((permission) => granted.includes(permission));
  }

  getAccessToken(): string | null {
    return this.tokenStorage.getAccessToken();
  }

  isAccessTokenExpired(): boolean {
    const token = this.tokenStorage.getAccessToken();
    return token === null || this.tokenStorage.isTokenExpired(token);
  }

  private setSession(response: ILoginResponse, rememberMe: boolean): void {
    this.tokenStorage.saveTokens(response.accessToken, response.refreshToken, rememberMe);
    localStorage.setItem(STORAGE_KEYS.CURRENT_USER, JSON.stringify(response.user));
    this.currentUserSignal.set(response.user);
  }

  private wasRememberMeEnabled(): boolean {
    return localStorage.getItem(STORAGE_KEYS.REMEMBER_ME) === 'true';
  }

  private readPersistedUser(): IUser | null {
    const raw = localStorage.getItem(STORAGE_KEYS.CURRENT_USER);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw) as IUser;
    } catch {
      return null;
    }
  }
}
