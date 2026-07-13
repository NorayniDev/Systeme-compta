import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog } from '@angular/material/dialog';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { PaymentService } from '../../services/payment.service';
import { IPayment, PAYMENT_STATUS_ACCENT, PaymentStatus } from '../../models/payment.model';
import { CurrencyXofPipe } from '../../../../shared/pipes/currency-xof.pipe';
import { HasPermissionDirective } from '../../../../shared/directives/has-permission.directive';
import { Permission } from '../../../../core/constants/roles.constant';
import { formatDate } from '../../../../core/helpers/date.helper';
import { NotificationService } from '../../../../core/services/notification.service';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';
import { downloadBlob } from '../../../../shared/helpers/table-export.helper';

/** Fiche de détail d'un paiement: informations générales et remboursement. */
@Component({
  selector: 'app-payment-detail',
  standalone: true,
  imports: [
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    TranslatePipe,
    CurrencyXofPipe,
    HasPermissionDirective,
  ],
  templateUrl: './payment-detail.html',
  styleUrl: './payment-detail.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PaymentDetail implements OnInit {
  private readonly paymentService = inject(PaymentService);
  private readonly notificationService = inject(NotificationService);
  private readonly translateService = inject(TranslateService);
  private readonly route = inject(ActivatedRoute);
  private readonly dialog = inject(MatDialog);

  protected readonly Permission = Permission;
  protected readonly PaymentStatus = PaymentStatus;
  protected readonly statusAccent = PAYMENT_STATUS_ACCENT;
  protected readonly formatDate = formatDate;
  protected readonly isLoading = signal(true);
  protected readonly isDownloading = signal(false);
  protected readonly payment = signal<IPayment | null>(null);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      return;
    }
    this.paymentService.getById(id).subscribe((payment) => {
      this.payment.set(payment);
      this.isLoading.set(false);
    });
  }

  downloadReceipt(): void {
    const current = this.payment();
    if (!current) return;

    this.isDownloading.set(true);
    this.paymentService.downloadReceipt(current.id).subscribe({
      next: (blob) => {
        downloadBlob(blob, `recu-${current.reference}.pdf`);
        this.isDownloading.set(false);
      },
      error: () => {
        this.notificationService.error(this.translateService.instant('common.downloadError'));
        this.isDownloading.set(false);
      },
    });
  }

  confirmRefund(): void {
    const dialogRef = this.dialog.open(ConfirmDialog, {
      data: {
        title: this.translateService.instant('payments.refundConfirmTitle'),
        message: this.translateService.instant('payments.refundConfirmMessage'),
        destructive: true,
      },
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.refund();
      }
    });
  }

  private refund(): void {
    const current = this.payment();
    if (!current) return;

    this.paymentService.refund(current.id).subscribe((updated) => {
      this.payment.set(updated);
      this.notificationService.success(this.translateService.instant('payments.refundedSuccess'));
    });
  }
}
