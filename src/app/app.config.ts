import {
  ApplicationConfig,
  ErrorHandler,
  inject,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
  provideZoneChangeDetection,
} from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideNativeDateAdapter } from '@angular/material/core';
import { providePrimeNG } from 'primeng/config';
import Material from '@primeuix/themes/material';
import { provideTranslateService } from '@ngx-translate/core';
import { provideTranslateHttpLoader } from '@ngx-translate/http-loader';
import { provideCharts, withDefaultRegisterables } from 'ng2-charts';
import { FaIconLibrary } from '@fortawesome/angular-fontawesome';
import {
  faFileExcel,
  faFilePdf,
  faFileInvoiceDollar,
  faCircleCheck,
  faTriangleExclamation,
} from '@fortawesome/free-solid-svg-icons';

import { routes } from './app.routes';
import { loadingInterceptor } from './core/interceptors/loading.interceptor';
import { errorInterceptor } from './core/interceptors/error.interceptor';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { mockApiInterceptor } from './core/mocks/mock-api.interceptor';
import { GlobalErrorHandler } from './core/error-handler/global-error-handler';
import { APP_CONSTANTS } from './core/constants/app.constant';
import { ThemeService } from './core/services/theme.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes, withComponentInputBinding()),

    // L'ordre est significatif: les requêtes sortantes traversent ce tableau
    // dans l'ordre déclaré, les réponses/erreurs le traversent en sens
    // inverse. `authInterceptor` voit donc les erreurs HTTP en premier et
    // tente un refresh silencieux avant qu'`errorInterceptor` ne notifie
    // l'utilisateur — voir les commentaires de chaque intercepteur.
    // `mockApiInterceptor` (dernier, donc le plus proche du "backend") ne
    // s'active qu'en mode démo (`environment.useMockApi`) et court-circuite
    // l'appel réseau pour simuler l'API Spring Boot.
    provideHttpClient(
      withInterceptors([loadingInterceptor, errorInterceptor, authInterceptor, mockApiInterceptor]),
    ),

    provideAnimationsAsync(),
    provideNativeDateAdapter(),
    provideCharts(withDefaultRegisterables()),

    providePrimeNG({
      theme: {
        preset: Material,
        options: { darkModeSelector: '.dark-theme' },
      },
    }),

    provideTranslateService({
      lang: APP_CONSTANTS.DEFAULT_LANG,
      fallbackLang: APP_CONSTANTS.DEFAULT_LANG,
      loader: provideTranslateHttpLoader({ prefix: '/i18n/', suffix: '.json' }),
    }),

    { provide: ErrorHandler, useClass: GlobalErrorHandler },

    provideAppInitializer(() => {
      inject(FaIconLibrary).addIcons(
        faFileExcel,
        faFilePdf,
        faFileInvoiceDollar,
        faCircleCheck,
        faTriangleExclamation,
      );

      // Injecter ThemeService ici (plutôt qu'attendre MainLayout) applique
      // la préférence sombre/claire sauvegardée dès le démarrage, avant le
      // premier rendu (ex: page de connexion), et évite un flash de thème
      // incorrect.
      inject(ThemeService);
    }),
  ],
};
