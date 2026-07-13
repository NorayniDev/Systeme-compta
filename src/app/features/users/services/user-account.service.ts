import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseService } from '../../../core/services/base.service';
import { API_ENDPOINTS } from '../../../core/constants/api-endpoints.constant';
import { IUser } from '../../../core/models/user.model';
import { UserAccountCreateDto, UserAccountUpdateDto } from '../models/user-account.dto';

/**
 * Service métier du domaine Utilisateur (administration des comptes).
 * Nommé `UserAccountService` (plutôt que `UserService`) pour le distinguer
 * sans ambiguïté de la notion d'utilisateur courant gérée par `AuthService`.
 */
@Injectable({ providedIn: 'root' })
export class UserAccountService extends BaseService<
  IUser,
  UserAccountCreateDto,
  UserAccountUpdateDto
> {
  protected readonly resourcePath = API_ENDPOINTS.USERS;

  resetPassword(id: string): Observable<void> {
    return this.api.post<void>(`${this.resourcePath}/${id}/reset-password`, {});
  }
}
