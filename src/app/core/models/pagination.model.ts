/**
 * Réponse paginée générique, alignée sur le format Spring Data Page<T>.
 */
export interface IPage<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

export type SortDirection = 'asc' | 'desc';

export interface IPageRequest {
  page: number;
  size: number;
  sort?: string;
  direction?: SortDirection;
}

export interface ISearchFilter extends IPageRequest {
  query?: string;
}

export const DEFAULT_PAGE_REQUEST: IPageRequest = {
  page: 0,
  size: 10,
  direction: 'desc',
};
