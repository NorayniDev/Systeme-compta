import { Injectable } from '@angular/core';
import { BaseService } from '../../../core/services/base.service';
import { API_ENDPOINTS } from '../../../core/constants/api-endpoints.constant';
import { IClient } from '../models/client.model';
import { ClientCreateDto, ClientUpdateDto } from '../models/client.dto';

/**
 * Service métier du domaine Client. Hérite de toutes les opérations CRUD
 * génériques de `BaseService` (SOLID: Open/Closed) et n'ajoute que ce qui
 * est spécifique au domaine.
 */
@Injectable({ providedIn: 'root' })
export class ClientService extends BaseService<IClient, ClientCreateDto, ClientUpdateDto> {
  protected readonly resourcePath = API_ENDPOINTS.CLIENTS;
}
