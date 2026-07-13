export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1',
  tokenRefreshMarginSeconds: 60,
  // Mode démo: simule le backend Spring Boot en mémoire (voir
  // core/mocks/mock-api.interceptor.ts). Passer à `false` dès qu'une API
  // réelle est disponible.
  useMockApi: true,
};
