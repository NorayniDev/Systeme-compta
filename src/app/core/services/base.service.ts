import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, QueryParams } from './api.service';
import { IPage, ISearchFilter } from '../models/pagination.model';

/**
 * Service CRUD générique paramétré par un type de ressource, sa forme de
 * création (`TCreateDto`) et de mise à jour (`TUpdateDto`).
 *
 * Chaque service de domaine (ex: `ClientService`) étend cette classe et se
 * contente de fournir le chemin de la ressource — toute la logique HTTP
 * répétitive (SOLID: Open/Closed, DRY) est centralisée ici.
 */
@Injectable()
export abstract class BaseService<
  TEntity,
  TCreateDto = Partial<TEntity>,
  TUpdateDto = Partial<TEntity>,
> {
  protected readonly api = inject(ApiService);

  protected abstract readonly resourcePath: string;

  search(filter: ISearchFilter): Observable<IPage<TEntity>> {
    return this.api.get<IPage<TEntity>>(this.resourcePath, filter as unknown as QueryParams);
  }

  getById(id: string): Observable<TEntity> {
    return this.api.get<TEntity>(`${this.resourcePath}/${id}`);
  }

  create(dto: TCreateDto): Observable<TEntity> {
    return this.api.post<TEntity>(this.resourcePath, dto);
  }

  update(id: string, dto: TUpdateDto): Observable<TEntity> {
    return this.api.put<TEntity>(`${this.resourcePath}/${id}`, dto);
  }

  delete(id: string): Observable<void> {
    return this.api.delete<void>(`${this.resourcePath}/${id}`);
  }
}
