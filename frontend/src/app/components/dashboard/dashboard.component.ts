/*
 * myRC - Dashboard Component
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/user.model';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ResponsibilityCentreService } from '../../services/responsibility-centre.service';
import { FiscalYearService } from '../../services/fiscal-year.service';
import { FundingItemService } from '../../services/funding-item.service';
import { CurrencyService } from '../../services/currency.service';
import { MoneyService } from '../../services/money.service';
import { ResponsibilityCentreDTO } from '../../models/responsibility-centre.model';
import { FiscalYear } from '../../models/fiscal-year.model';
import { FundingItem, FundingItemCreateRequest, getStatusLabel, getStatusClass, FundingItemStatus, MoneyAllocation } from '../../models/funding-item.model';
import { Currency, DEFAULT_CURRENCY } from '../../models/currency.model';
import { Money } from '../../models/money.model';

/**
 * Dashboard component showing funding items for the selected RC and FY.
 *
 * @author myRC Team
 * @version 2.0.0
 * @since 2026-01-17
 */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DashboardComponent implements OnInit, OnDestroy {
  currentUser: User | null = null;

  // Selected RC and FY
  selectedRC: ResponsibilityCentreDTO | null = null;
  selectedFY: FiscalYear | null = null;

  // Funding Items
  fundingItems: FundingItem[] = [];
  isLoadingItems = false;

  // Currencies
  currencies: Currency[] = [];
  isLoadingCurrencies = false;

  // Monies
  monies: Money[] = [];
  isLoadingMonies = false;

  // Create Form
  showCreateForm = false;
  isCreating = false;
  newItemName = '';
  newItemDescription = '';
  newItemBudget: number | null = null;
  newItemStatus: FundingItemStatus = 'DRAFT';
  newItemCurrency = DEFAULT_CURRENCY;
  newItemExchangeRate: number | null = null;
  newItemMoneyAllocations: MoneyAllocation[] = [];

  // Messages
  errorMessage: string | null = null;
  successMessage: string | null = null;

  // Status list for dropdown
  statusOptions: FundingItemStatus[] = ['DRAFT', 'PENDING', 'APPROVED', 'ACTIVE', 'CLOSED'];

  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private router: Router,
    private rcService: ResponsibilityCentreService,
    private fyService: FiscalYearService,
    private fundingItemService: FundingItemService,
    private currencyService: CurrencyService,
    private moneyService: MoneyService
  ) {}

  ngOnInit(): void {
    // Load currencies
    this.loadCurrencies();

    // Subscribe to current user
    this.authService.currentUser$.pipe(takeUntil(this.destroy$)).subscribe((user: User | null) => {
      this.currentUser = user;
      if (!user) {
        this.router.navigate(['/login']);
      } else {
        this.loadSelectedContext();
      }
    });
  }

  /**
   * Load supported currencies from the backend.
   */
  private loadCurrencies(): void {
    this.isLoadingCurrencies = true;
    this.currencyService.getCurrencies()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (currencies) => {
          this.currencies = currencies;
          this.isLoadingCurrencies = false;
        },
        error: (error) => {
          console.error('Failed to load currencies:', error);
          this.isLoadingCurrencies = false;
          // Default to CAD if currencies fail to load
          this.currencies = [{
            code: 'CAD',
            name: 'Canadian Dollar',
            symbol: '$',
            isDefault: true
          }];
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Load the selected RC and FY context from service.
   */
  private loadSelectedContext(): void {
    const rcId = this.rcService.getSelectedRC();
    const fyId = this.rcService.getSelectedFY();

    if (!rcId || !fyId) {
      // Redirect to RC selection if not selected
      this.router.navigate(['/rc-selection']);
      return;
    }

    // Load RC details
    this.rcService.getResponsibilityCentre(rcId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (rc) => {
          this.selectedRC = rc;
        },
        error: (error) => {
          console.error('Failed to load RC:', error);
          this.router.navigate(['/rc-selection']);
        }
      });

    // Load FY details
    this.fyService.getFiscalYear(rcId, fyId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (fy) => {
          this.selectedFY = fy;
          this.loadFundingItems();
          this.loadMonies();
        },
        error: (error) => {
          console.error('Failed to load FY:', error);
          this.router.navigate(['/rc-selection']);
        }
      });
  }

  /**
   * Load money types for the selected FY.
   */
  loadMonies(): void {
    if (!this.selectedRC || !this.selectedFY) {
      return;
    }

    this.isLoadingMonies = true;
    this.moneyService.getMoniesByFiscalYear(this.selectedRC.id, this.selectedFY.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (monies) => {
          this.monies = monies;
          this.isLoadingMonies = false;
          this.initializeMoneyAllocations();
        },
        error: (error) => {
          console.error('Failed to load monies:', error);
          this.isLoadingMonies = false;
          this.monies = [];
        }
      });
  }

  /**
   * Initialize money allocations for new funding item with default values.
   */
  private initializeMoneyAllocations(): void {
    this.newItemMoneyAllocations = this.monies.map(money => ({
      moneyId: money.id,
      moneyCode: money.code,
      moneyName: money.name,
      capAmount: 0,
      omAmount: 0
    }));
  }

  /**
   * Load funding items for the selected FY.
   */
  loadFundingItems(): void {
    if (!this.selectedFY) {
      return;
    }

    this.isLoadingItems = true;
    this.fundingItemService.getFundingItemsByFY(this.selectedFY.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (items) => {
          this.fundingItems = items;
          this.isLoadingItems = false;
        },
        error: (error) => {
          this.errorMessage = error.message || 'Failed to load funding items.';
          this.isLoadingItems = false;
        }
      });
  }

  /**
   * Get sorted list of funding items (alphabetical by name).
   */
  get sortedFundingItems(): FundingItem[] {
    return [...this.fundingItems].sort((a, b) =>
      a.name.localeCompare(b.name, undefined, { sensitivity: 'base' })
    );
  }

  /**
   * Check if user can write to the selected RC.
   */
  get canWrite(): boolean {
    if (!this.selectedRC) {
      return false;
    }
    return this.selectedRC.isOwner || this.selectedRC.accessLevel === 'READ_WRITE';
  }

  /**
   * Toggle the create form visibility.
   */
  toggleCreateForm(): void {
    this.showCreateForm = !this.showCreateForm;
    if (!this.showCreateForm) {
      this.resetForm();
    }
  }

  /**
   * Create a new funding item.
   */
  createFundingItem(): void {
    if (!this.selectedFY || !this.newItemName.trim()) {
      this.errorMessage = 'Name is required';
      return;
    }

    // Validate exchange rate for non-CAD currencies
    if (this.newItemCurrency !== DEFAULT_CURRENCY && 
        (this.newItemExchangeRate === null || this.newItemExchangeRate <= 0)) {
      this.errorMessage = 'Exchange rate is required for non-CAD currencies and must be greater than zero';
      return;
    }

    this.isCreating = true;
    this.errorMessage = null;
    this.successMessage = null;

    const request: FundingItemCreateRequest = {
      name: this.newItemName.trim(),
      description: this.newItemDescription.trim(),
      budgetAmount: this.newItemBudget || undefined,
      status: this.newItemStatus,
      currency: this.newItemCurrency,
      exchangeRate: this.newItemCurrency !== DEFAULT_CURRENCY ? this.newItemExchangeRate || undefined : undefined,
      moneyAllocations: this.newItemMoneyAllocations
    };

    this.fundingItemService.createFundingItem(this.selectedFY.id, request)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (newItem) => {
          this.fundingItems.push(newItem);
          this.resetForm();
          this.showCreateForm = false;
          this.isCreating = false;
          this.successMessage = `Funding Item "${newItem.name}" created successfully.`;
          setTimeout(() => this.clearSuccess(), 5000);
        },
        error: (error) => {
          this.isCreating = false;
          this.errorMessage = error.message || 'Failed to create funding item.';
        }
      });
  }

  /**
   * Delete a funding item.
   */
  deleteFundingItem(item: FundingItem): void {
    if (!this.selectedFY) {
      return;
    }

    if (!confirm(`Are you sure you want to delete "${item.name}"?`)) {
      return;
    }

    this.fundingItemService.deleteFundingItem(this.selectedFY.id, item.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.fundingItems = this.fundingItems.filter(fi => fi.id !== item.id);
          this.successMessage = `Funding Item "${item.name}" deleted successfully.`;
          setTimeout(() => this.clearSuccess(), 5000);
        },
        error: (error) => {
          this.errorMessage = error.message || 'Failed to delete funding item.';
        }
      });
  }

  /**
   * Reset the create form.
   */
  private resetForm(): void {
    this.newItemName = '';
    this.newItemDescription = '';
    this.newItemBudget = null;
    this.newItemStatus = 'DRAFT';
    this.newItemCurrency = DEFAULT_CURRENCY;
    this.newItemExchangeRate = null;
    this.initializeMoneyAllocations();
  }

  /**
   * Get status display label.
   */
  getStatusLabel(status: FundingItemStatus): string {
    return getStatusLabel(status);
  }

  /**
   * Get status CSS class.
   */
  getStatusClass(status: FundingItemStatus): string {
    return getStatusClass(status);
  }

  /**
   * Format currency for display.
   */
  formatCurrency(amount: number | null, currencyCode?: string): string {
    if (amount === null || amount === undefined) {
      return '-';
    }
    const code = currencyCode || DEFAULT_CURRENCY;
    const locale = this.getLocaleForCurrency(code);
    return new Intl.NumberFormat(locale, {
      style: 'currency',
      currency: code
    }).format(amount);
  }

  /**
   * Get the appropriate locale for a currency.
   */
  private getLocaleForCurrency(currencyCode: string): string {
    switch (currencyCode) {
      case 'CAD':
        return 'en-CA';
      case 'USD':
        return 'en-US';
      case 'GBP':
        return 'en-GB';
      case 'EUR':
        return 'de-DE';
      case 'AUD':
        return 'en-AU';
      case 'NZD':
        return 'en-NZ';
      default:
        return 'en-CA';
    }
  }

  /**
   * Check if a currency requires an exchange rate (non-CAD currencies).
   */
  requiresExchangeRate(currencyCode: string): boolean {
    return currencyCode !== DEFAULT_CURRENCY;
  }

  /**
   * Handle currency change in the form.
   */
  onCurrencyChange(): void {
    if (this.newItemCurrency === DEFAULT_CURRENCY) {
      this.newItemExchangeRate = null;
    }
  }

  /**
   * Get the currency symbol for a currency code.
   */
  getCurrencySymbol(currencyCode: string): string {
    const currency = this.currencies.find(c => c.code === currencyCode);
    return currency?.symbol || '$';
  }

  /**
   * Navigate back to RC selection.
   */
  navigateToRCSelection(): void {
    this.router.navigate(['/rc-selection']);
  }

  /**
   * Clear error message.
   */
  clearError(): void {
    this.errorMessage = null;
  }

  /**
   * Clear success message.
   */
  clearSuccess(): void {
    this.successMessage = null;
  }
}
