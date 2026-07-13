import { HttpParams } from '@angular/common/http';
import { IPage } from '../models/pagination.model';

/** Filtre, trie et pagine un tableau en mémoire à partir des `HttpParams` reçus par le mock. */
export function paginateMock<T extends Record<string, unknown>>(
  items: T[],
  params: HttpParams,
  searchableFields: (keyof T)[],
): IPage<T> {
  const query = (params.get('query') ?? '').toLowerCase().trim();
  const page = Number(params.get('page') ?? 0);
  const size = Number(params.get('size') ?? 10);
  const sort = params.get('sort');
  const direction = params.get('direction') === 'asc' ? 1 : -1;

  let filtered = items;
  if (query) {
    filtered = items.filter((item) =>
      searchableFields.some((field) =>
        String(item[field] ?? '')
          .toLowerCase()
          .includes(query),
      ),
    );
  }

  if (sort) {
    filtered = [...filtered].sort((a, b) => {
      const left = a[sort as keyof T];
      const right = b[sort as keyof T];
      if (left === right) return 0;
      return left! > right! ? direction : -direction;
    });
  }

  const totalElements = filtered.length;
  const totalPages = Math.max(1, Math.ceil(totalElements / size));
  const start = page * size;
  const content = filtered.slice(start, start + size);

  return {
    content,
    totalElements,
    totalPages,
    number: page,
    size,
    first: page === 0,
    last: page >= totalPages - 1,
  };
}
