/*
 * myRC - Summary Component
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/user.model';
import { Subject, forkJoin } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ResponsibilityCentreService } from '../../services/responsibility-centre.service';
import { FiscalYearService } from '../../services/fiscal-year.service';
import { FundingItemService } from '../../services/funding-item.service';
import { SpendingItemService } from '../../services/spending-item.service';
import { ProcurementService } from '../../services/procurement.service';
import { ResponsibilityCentreDTO } from '../../models/responsibility-centre.model';
import { FiscalYear } from '../../models/fiscal-year.model';
import { FundingItem } from '../../models/funding-item.model';
import { SpendingItem } from '../../models/spending-item.model';
import { ProcurementItem } from '../../models/procurement.model';

/**
 * Summary component providing an overall financial overview of the FY.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-29
 */
@Component({
  selector: 'app-summary',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './summary.component.html',
  styleUrls: ['./summary.component.scss'],
})
export class SummaryComponent implements OnInit, OnDestroy {
  currentUser: User | null = null;
  Math = Math; // Expose Math for template use

  // Selected RC and FY
  selectedRC: ResponsibilityCentreDTO | null = null;
  selectedFY: FiscalYear | null = null;

  // Data
  fundingItems: FundingItem[] = [];
  spendingItems: SpendingItem[] = [];
  procurementItems: ProcurementItem[] = [];
  
  isLoading = false;
  errorMessage: string | null = null;

  // Calculated totals
  totalFundingCap = 0;
  totalFundingOm = 0;
  totalFunding = 0;
  
  totalSpendingCap = 0;
  totalSpendingOm = 0;
  totalSpending = 0;
  
  remainingCap = 0;
  remainingOm = 0;
  remainingTotal = 0;
  
  spendingPercentCap = 0;
  spendingPercentOm = 0;
  spendingPercentTotal = 0;

  // Procurement stats
  procurementCompleted = 0;
  procurementPending = 0;
  procurementInProgress = 0;

  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private router: Router,
    private rcService: ResponsibilityCentreService,
    private fyService: FiscalYearService,
    private fundingItemService: FundingItemService,
    private spendingItemService: SpendingItemService,
    private procurementService: ProcurementService
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.pipe(takeUntil(this.destroy$)).subscribe((user: User | null) => {
      this.currentUser = user;
      if (!user) {
        this.router.navigate(['/login']);
      } else {
        this.loadSelectedContext();
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Load the selected RC and FY context.
   */
  private loadSelectedContext(): void {
    const rcId = this.rcService.getSelectedRC();
    const fyId = this.rcService.getSelectedFY();

    if (!rcId || !fyId) {
      this.router.navigate(['/app/select']);
      return;
    }

    this.rcService.getResponsibilityCentre(rcId).subscribe({
      next: (rc) => {
        this.selectedRC = rc;
        this.fyService.getFiscalYear(rcId, fyId).subscribe({
          next: (fy) => {
            this.selectedFY = fy;
            this.loadAllData();
          },
          error: () => {
            this.router.navigate(['/app/select']);
          }
        });
      },
      error: () => {
        this.router.navigate(['/app/select']);
      }
    });
  }

  /**
   * Load all data for summary.
   */
  private loadAllData(): void {
    if (!this.selectedRC || !this.selectedFY) return;

    this.isLoading = true;

    forkJoin({
      funding: this.fundingItemService.getFundingItemsByFY(this.selectedFY.id),
      spending: this.spendingItemService.getSpendingItemsByFY(this.selectedRC.id, this.selectedFY.id),
      procurement: this.procurementService.getProcurementItems(this.selectedRC.id, this.selectedFY.id)
    }).subscribe({
      next: (data) => {
        this.fundingItems = data.funding;
        this.spendingItems = data.spending;
        this.procurementItems = data.procurement;
        this.isLoading = false;
        this.calculateTotals();
      },
      error: (error) => {
        this.errorMessage = 'Failed to load data: ' + error.message;
        this.isLoading = false;
      }
    });
  }

  /**
   * Calculate all totals and percentages.
   */
  private calculateTotals(): void {
    // Reset totals
    this.totalFundingCap = 0;
    this.totalFundingOm = 0;
    this.totalSpendingCap = 0;
    this.totalSpendingOm = 0;

    // Calculate funding totals
    for (const item of this.fundingItems) {
      if (item.moneyAllocations) {
        for (const allocation of item.moneyAllocations) {
          this.totalFundingCap += allocation.capAmount || 0;
          this.totalFundingOm += allocation.omAmount || 0;
        }
      }
    }
    this.totalFunding = this.totalFundingCap + this.totalFundingOm;

    // Calculate spending totals
    for (const item of this.spendingItems) {
      if (item.moneyAllocations) {
        for (const allocation of item.moneyAllocations) {
          this.totalSpendingCap += allocation.capAmount || 0;
          this.totalSpendingOm += allocation.omAmount || 0;
        }
      }
    }
    this.totalSpending = this.totalSpendingCap + this.totalSpendingOm;

    // Calculate remaining
    this.remainingCap = this.totalFundingCap - this.totalSpendingCap;
    this.remainingOm = this.totalFundingOm - this.totalSpendingOm;
    this.remainingTotal = this.totalFunding - this.totalSpending;

    // Calculate percentages
    this.spendingPercentCap = this.totalFundingCap > 0 
      ? (this.totalSpendingCap / this.totalFundingCap) * 100 
      : 0;
    this.spendingPercentOm = this.totalFundingOm > 0 
      ? (this.totalSpendingOm / this.totalFundingOm) * 100 
      : 0;
    this.spendingPercentTotal = this.totalFunding > 0 
      ? (this.totalSpending / this.totalFunding) * 100 
      : 0;

    // Calculate procurement stats
    this.procurementCompleted = this.procurementItems.filter(
      p => p.status === 'COMPLETED'
    ).length;
    this.procurementPending = this.procurementItems.filter(
      p => ['DRAFT', 'PENDING_QUOTES', 'QUOTES_RECEIVED'].includes(p.status)
    ).length;
    this.procurementInProgress = this.procurementItems.filter(
      p => ['UNDER_REVIEW', 'APPROVED', 'PO_ISSUED'].includes(p.status)
    ).length;
  }

  /**
   * Format currency for display.
   */
  formatCurrency(value: number | null | undefined, currency = 'CAD'): string {
    if (value === null || value === undefined) return '$0.00';
    return new Intl.NumberFormat('en-CA', {
      style: 'currency',
      currency: currency
    }).format(value);
  }

  /**
   * Get status color class based on funding vs spending percentage difference.
   * Uses the On Target thresholds from FY settings.
   */
  getStatusClass(percentage: number): string {
    const difference = 100 - percentage; // How much is remaining (positive = under budget)
    const onTargetMin = this.selectedFY?.onTargetMin ?? -10;
    const onTargetMax = this.selectedFY?.onTargetMax ?? 10;

    // If spending exceeds funding (negative remaining = over budget)
    if (difference < onTargetMin) {
      return 'status-danger'; // Over budget - outside lower bound
    }
    // If we're within the on-target range
    if (difference >= onTargetMin && difference <= onTargetMax) {
      return 'status-good'; // On target
    }
    // If we're under budget but outside the target range
    if (difference > onTargetMax && difference <= onTargetMax + 15) {
      return 'status-caution'; // Slightly under budget
    }
    return 'status-warning'; // Significantly under budget
  }

  /**
   * Get On Target indicator based on funding vs spending difference.
   * Uses the On Target thresholds from FY settings.
   */
  getOnTargetIndicator(): string {
    const difference = 100 - this.spendingPercentTotal; // Positive = under budget, negative = over budget
    const onTargetMin = this.selectedFY?.onTargetMin ?? -10;
    const onTargetMax = this.selectedFY?.onTargetMax ?? 10;

    // If spending exceeds funding significantly (over budget)
    if (difference < onTargetMin) {
      return '🔴'; // Over budget
    }
    // If within the on-target range
    if (difference >= onTargetMin && difference <= onTargetMax) {
      return '🟢'; // On target
    }
    // Slightly outside range
    if (difference > onTargetMax && difference <= onTargetMax + 20) {
      return '🟡'; // Slightly under budget
    }
    // Significantly under budget
    return '🟠'; // Under budget
  }

  /**
   * Get On Target status text description.
   * Uses the On Target thresholds from FY settings.
   */
  getOnTargetText(): string {
    const difference = 100 - this.spendingPercentTotal; // Positive = under budget, negative = over budget
    const onTargetMin = this.selectedFY?.onTargetMin ?? -10;
    const onTargetMax = this.selectedFY?.onTargetMax ?? 10;

    // If spending exceeds funding significantly (over budget)
    if (difference < onTargetMin) {
      return 'Over Budget';
    }
    // If within the on-target range
    if (difference >= onTargetMin && difference <= onTargetMax) {
      return 'On Target';
    }
    // Slightly outside range
    if (difference > onTargetMax && difference <= onTargetMax + 20) {
      return 'Slightly Under Budget';
    }
    // Significantly under budget
    return 'Under Budget';
  }

  /**
   * @deprecated Use getOnTargetIndicator() instead
   * Get health indicator based on remaining budget.
   */
  getHealthIndicator(): string {
    return this.getOnTargetIndicator();
  }

  /**
   * @deprecated Use getOnTargetText() instead
   * Get health text description.
   */
  getHealthText(): string {
    return this.getOnTargetText();
  }
}
