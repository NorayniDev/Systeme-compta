import { IJwtPayload } from '../models/auth.model';

/**
 * Génère un JWT structurellement valide (header.payload.signature encodés en
 * base64url) mais NON signé cryptographiquement — utilisé uniquement par
 * `mock-api.interceptor.ts` en mode démo (`environment.useMockApi`).
 * `jwt-decode` ne vérifie pas la signature, seul le payload est lu côté front.
 */
export function createMockAccessToken(
  claims: Omit<IJwtPayload, 'iat' | 'exp'>,
  expiresInSeconds = 3600,
): string {
  const nowSeconds = Math.floor(Date.now() / 1000);
  const header = { alg: 'none', typ: 'JWT' };
  const payload: IJwtPayload = { ...claims, iat: nowSeconds, exp: nowSeconds + expiresInSeconds };

  return [base64UrlEncode(header), base64UrlEncode(payload), 'mock-signature'].join('.');
}

function base64UrlEncode(value: unknown): string {
  const json = JSON.stringify(value);
  const base64 = btoa(unescape(encodeURIComponent(json)));
  return base64.replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}
