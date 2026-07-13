import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { API_ENDPOINTS } from '../../../core/constants/api-endpoints.constant';
import { IUser } from '../../../core/models/user.model';
import { ICompanySettings } from '../models/company-settings.model';
import { ChangePasswordDto, ProfileUpdateDto } from '../models/settings.dto';

/**
 * Service des Paramètres: profil de l'utilisateur courant et informations
 * générales de l'entreprise (ces dernières réservées à `SETTINGS_MANAGE`).
 */
@Injectable({ providedIn: 'root' })
export class SettingsService {
  private readonly api = inject(ApiService);

  updateProfile(dto: ProfileUpdateDto): Observable<IUser> {
    return this.api.put<IUser>(API_ENDPOINTS.PROFILE.UPDATE, dto);
  }

  changePassword(dto: ChangePasswordDto): Observable<void> {
    return this.api.post<void>(API_ENDPOINTS.PROFILE.CHANGE_PASSWORD, dto);
  }

  getCompanySettings(): Observable<ICompanySettings> {
    return this.api.get<ICompanySettings>(API_ENDPOINTS.SETTINGS.COMPANY);
  }

  updateCompanySettings(dto: ICompanySettings): Observable<ICompanySettings> {
    return this.api.put<ICompanySettings>(API_ENDPOINTS.SETTINGS.COMPANY, dto);
  }
}
