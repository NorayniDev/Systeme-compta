import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDividerModule } from '@angular/material/divider';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { AuthService } from '../../../core/authentication/auth.service';
import { ThemeService } from '../../../core/services/theme.service';
import { LoadingService } from '../../../core/services/loading.service';
import { NotificationCenterService } from '../../../core/services/notification-center.service';
import { getFullName, getInitials } from '../../../core/models/user.model';
import { INotification, notificationRouterLink } from '../../../core/models/notification.model';
import { RelativeTimePipe } from '../../pipes/relative-time.pipe';
import { NAV_SECTIONS } from './nav-items';
import { APP_CONSTANTS, SupportedLang } from '../../../core/constants/app.constant';
import { STORAGE_KEYS } from '../../../core/constants/storage-keys.constant';
import { environment } from '../../../../environments/environment';

/**
 * Layout applicatif principal: barre latérale de navigation filtrée par
 * rôle, barre supérieure (recherche, thème, langue, notifications, profil)
 * et zone de contenu routée.
 */
@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatSidenavModule,
    MatToolbarModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
    MatBadgeModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatDividerModule,
    TranslatePipe,
    RelativeTimePipe,
  ],
  templateUrl: './main-layout.html',
  styleUrl: './main-layout.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MainLayout {
  protected readonly authService = inject(AuthService);
  protected readonly themeService = inject(ThemeService);
  protected readonly loadingService = inject(LoadingService);
  private readonly translateService = inject(TranslateService);
  private readonly notificationCenterService = inject(NotificationCenterService);
  private readonly router = inject(Router);

  protected readonly appName = APP_CONSTANTS.APP_SHORT_NAME;
  protected readonly supportedLangs = APP_CONSTANTS.SUPPORTED_LANGS;
  protected readonly isDemoMode = environment.useMockApi;
  protected readonly isSidenavOpened = signal(true);

  protected readonly unreadNotificationsCount = signal(0);
  protected readonly notifications = signal<INotification[]>([]);
  protected readonly isLoadingNotifications = signal(false);

  protected readonly currentUser = this.authService.currentUser;
  protected readonly userFullName = computed(() => {
    const user = this.currentUser();
    return user ? getFullName(user) : '';
  });
  protected readonly userInitials = computed(() => {
    const user = this.currentUser();
    return user ? getInitials(user) : '';
  });

  protected readonly navSections = computed(() =>
    NAV_SECTIONS.map((section) => ({
      ...section,
      items: section.items.filter((item) => !item.roles || this.authService.hasRole(...item.roles)),
    })).filter((section) => section.items.length > 0),
  );

  constructor() {
    this.refreshUnreadCount();
  }

  onNotificationsMenuOpened(): void {
    this.isLoadingNotifications.set(true);
    this.notificationCenterService.search({ page: 0, size: 10 }).subscribe({
      next: (page) => {
        this.notifications.set(page.content);
        this.isLoadingNotifications.set(false);
      },
      error: () => this.isLoadingNotifications.set(false),
    });
  }

  onNotificationClick(notification: INotification): void {
    const link = notificationRouterLink(notification);
    if (link) {
      this.router.navigate(link);
    }
    if (notification.read) {
      return;
    }
    this.notifications.update((items) =>
      items.map((n) => (n.id === notification.id ? { ...n, read: true } : n)),
    );
    this.unreadNotificationsCount.update((count) => Math.max(0, count - 1));
    this.notificationCenterService.markAsRead(notification.id).subscribe();
  }

  markAllNotificationsAsRead(): void {
    this.notificationCenterService.markAllAsRead().subscribe(() => {
      this.notifications.update((items) => items.map((n) => ({ ...n, read: true })));
      this.unreadNotificationsCount.set(0);
    });
  }

  private refreshUnreadCount(): void {
    this.notificationCenterService.getUnreadCount().subscribe(({ count }) => {
      this.unreadNotificationsCount.set(count);
    });
  }

  toggleSidenav(): void {
    this.isSidenavOpened.update((opened) => !opened);
  }

  toggleTheme(): void {
    this.themeService.toggle();
  }

  setLang(lang: SupportedLang): void {
    this.translateService.use(lang);
    localStorage.setItem(STORAGE_KEYS.LANG, lang);
  }

  logout(): void {
    this.authService.logout();
  }
}
