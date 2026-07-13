import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';

export type KpiTrend = 'up' | 'down' | 'neutral';

/**
 * Tuile KPI réutilisable (CA, factures, créances, ...) affichée sur le
 * tableau de bord. Composant "atome" au sens Atomic Design: pas de logique
 * métier, uniquement de la présentation pilotée par ses `input()`.
 */
@Component({
  selector: 'app-kpi-card',
  standalone: true,
  imports: [MatIconModule, MatCardModule],
  templateUrl: './kpi-card.html',
  styleUrl: './kpi-card.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class KpiCard {
  readonly label = input.required<string>();
  readonly value = input.required<string>();
  readonly icon = input.required<string>();
  readonly trend = input<KpiTrend>('neutral');
  readonly trendLabel = input<string>('');
  readonly accent = input<'primary' | 'success' | 'warning' | 'error'>('primary');
}
