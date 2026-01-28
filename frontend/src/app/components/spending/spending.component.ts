/*
 * myRC - Spending Component
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
import { SpendingItemService, SpendingItemCreateRequest } from '../../services/spending-item.service';
import { CategoryService } from '../../services/category.service';
import { MoneyService } from '../../services/money.service';
import { CurrencyService } from '../../services/currency.service';
import { ResponsibilityCentreDTO } from '../../models/responsibility-centre.model';
import { FiscalYear } from '../../models/fiscal-year.model';
import { SpendingItem, SpendingMoneyAllocation, SpendingItemStatus, SPENDING_STATUS_INFO } from '../../models/spending-item.model';
import { Category } from '../../models/category.model';
import { Money } from '../../models/money.model';
import { Currency, DEFAULT_CURRENCY, getCurrencyFlag } from '../../models/currency.model';

/**
 * Spending component showing spending items for the selected RC and FY.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-26
 */
@Component({
  selector: 'app-spending',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './spending.component.html',
  styleUrls: ['./spending.component.scss'],
})
export class SpendingComponent implements OnInit, OnDestroy {
  currentUser: User | null = null;

  // Selected RC and FY
  selectedRC: ResponsibilityCentreDTO | null = null;
  selectedFY: FiscalYear | null = null;

  // Spending Items
  spendingItems: SpendingItem[] = [];
  isLoadingItems = false;

  // Categories
  categories: Category[] = [];
  isLoadingCategories = false;
  selectedCategoryId: number | null = null;

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
  newItemVendor = '';
  newItemReferenceNumber = '';
  newItemAmount: number | null = null;
  newItemStatus: SpendingItemStatus = 'DRAFT';
  newItemCurrency = DEFAULT_CURRENCY;
  newItemExchangeRate: number | null = null;
  newItemCategoryId: number | null = null;
  newItemMoneyAllocations: SpendingMoneyAllocation[] = [];

  // Messages
  errorMessage: string | null = null;
  successMessage: string | null = null;

  // Status list for dropdown
  statusOptions: SpendingItemStatus[] = ['DRAFT', 'PENDING', 'APPROVED', 'COMMITTED', 'PAID', 'CANCELLED'];

  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private router: Router,
    private rcService: ResponsibilityCentreService,
    private fyService: FiscalYearService,
    private spendingItemService: SpendingItemService,
    private categoryService: CategoryService,
    private moneyService: MoneyService,
    private currencyService: CurrencyService
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

    // Load RC
    this.rcService.getResponsibilityCentre(rcId).subscribe({
      next: (rc) => {
        this.selectedRC = rc;
        // Load FY
        this.fyService.getFiscalYear(rcId, fyId).subscribe({
          next: (fy) => {
            this.selectedFY = fy;
            this.loadMonies();
            this.loadCategories();
            this.loadSpendingItems();
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
   * Load currencies from the API.
   */
  private loadCurrencies(): void {
    this.isLoadingCurrencies = true;
    this.currencyService.getCurrencies().subscribe({
      next: (currencies) => {
        this.currencies = currencies;
        this.isLoadingCurrencies = false;
      },
      error: () => {
        // Use default currency list if API fails
        this.currencies = [
          { code: 'CAD', name: 'Canadian Dollar', symbol: '$', isDefault: true }
        ];
        this.isLoadingCurrencies = false;
      }
    });
  }

  /**
   * Load monies for the current FY.
   */
  private loadMonies(): void {
    if (!this.selectedRC || !this.selectedFY) return;

    this.isLoadingMonies = true;
    this.moneyService.getMoniesByFiscalYear(this.selectedRC.id, this.selectedFY.id).subscribe({
      next: (monies: Money[]) => {
        this.monies = monies;
        this.isLoadingMonies = false;
        this.initializeNewMoneyAllocations();
      },
      error: () => {
        this.monies = [];
        this.isLoadingMonies = false;
      }
    });
  }

  /**
   * Load categories for the current FY.
   */
  private loadCategories(): void {
    if (!this.selectedRC || !this.selectedFY) return;

    this.isLoadingCategories = true;
    this.categoryService.getCategoriesByFY(this.selectedRC.id, this.selectedFY.id).subscribe({
      next: (categories) => {
        this.categories = categories;
        this.isLoadingCategories = false;
        // Set default category if available
        if (categories.length > 0 && !this.newItemCategoryId) {
          this.newItemCategoryId = categories[0].id;
        }
      },
      error: (error) => {
        this.categories = [];
        this.isLoadingCategories = false;
        this.showError('Failed to load categories: ' + error.message);
      }
    });
  }

  /**
   * Load spending items for the current FY.
   */
  private loadSpendingItems(): void {
    if (!this.selectedRC || !this.selectedFY) return;

    this.isLoadingItems = true;
    this.spendingItemService.getSpendingItemsByFY(
      this.selectedRC.id, 
      this.selectedFY.id, 
      this.selectedCategoryId || undefined
    ).subscribe({
      next: (items) => {
        this.spendingItems = items;
        this.isLoadingItems = false;
      },
      error: (error) => {
        this.spendingItems = [];
        this.isLoadingItems = false;
        this.showError('Failed to load spending items: ' + error.message);
      }
    });
  }

  /**
   * Initialize money allocations for the create form.
   */
  private initializeNewMoneyAllocations(): void {
    this.newItemMoneyAllocations = this.monies.map(money => ({
      moneyId: money.id,
      moneyName: money.name,
      isDefault: money.isDefault,
      capAmount: 0,
      omAmount: 0
    }));
  }

  /**
   * Check if user can write to the selected RC.
   *
   * @returns true if user is owner or has READ_WRITE access
   */
  get canWrite(): boolean {
    if (!this.selectedRC) {
      return false;
    }
    return this.selectedRC.isOwner || this.selectedRC.accessLevel === 'READ_WRITE';
  }

  /**
   * Filter spending items by category.
   */
  filterByCategory(categoryId: number | null): void {
    this.selectedCategoryId = categoryId;
    this.loadSpendingItems();
  }

  /**
   * Show the create form.
   */
  showCreate(): void {
    this.showCreateForm = true;
    this.resetCreateForm();
    this.initializeNewMoneyAllocations();
  }

  /**
   * Hide the create form.
   */
  cancelCreate(): void {
    this.showCreateForm = false;
    this.resetCreateForm();
  }

  /**
   * Reset the create form.
   */
  private resetCreateForm(): void {
    this.newItemName = '';
    this.newItemDescription = '';
    this.newItemVendor = '';
    this.newItemReferenceNumber = '';
    this.newItemAmount = null;
    this.newItemStatus = 'DRAFT';
    this.newItemCurrency = DEFAULT_CURRENCY;
    this.newItemExchangeRate = null;
    this.newItemCategoryId = this.categories.length > 0 ? this.categories[0].id : null;
    this.initializeNewMoneyAllocations();
  }

  /**
   * Check if the form has valid money allocation.
   */
  hasValidMoneyAllocation(): boolean {
    if (!this.newItemMoneyAllocations || this.newItemMoneyAllocations.length === 0) {
      return false;
    }
    return this.newItemMoneyAllocations.some(allocation =>
      (allocation.capAmount && allocation.capAmount > 0) ||
      (allocation.omAmount && allocation.omAmount > 0)
    );
  }

  /**
   * Create a new spending item.
   */
  createSpendingItem(): void {
    if (!this.selectedRC || !this.selectedFY || !this.newItemName.trim() || !this.newItemCategoryId) {
      return;
    }

    if (!this.hasValidMoneyAllocation()) {
      this.showError('At least one money type must have a CAP or OM amount greater than $0.00');
      return;
    }

    this.isCreating = true;
    this.clearMessages();

    const request: SpendingItemCreateRequest = {
      name: this.newItemName.trim(),
      description: this.newItemDescription.trim() || undefined,
      vendor: this.newItemVendor.trim() || undefined,
      referenceNumber: this.newItemReferenceNumber.trim() || undefined,
      amount: this.newItemAmount ?? undefined,
      status: this.newItemStatus,
      currency: this.newItemCurrency,
      exchangeRate: (this.newItemCurrency !== DEFAULT_CURRENCY && this.newItemExchangeRate !== null) ? this.newItemExchangeRate : undefined,
      categoryId: this.newItemCategoryId,
      moneyAllocations: this.newItemMoneyAllocations
    };

    this.spendingItemService.createSpendingItem(this.selectedRC.id, this.selectedFY.id, request).subscribe({
      next: () => {
        this.showSuccess('Spending item created successfully');
        this.showCreateForm = false;
        this.resetCreateForm();
        this.loadSpendingItems();
        this.isCreating = false;
      },
      error: (error) => {
        this.showError('Failed to create spending item: ' + error.message);
        this.isCreating = false;
      }
    });
  }

  /**
   * Delete a spending item.
   */
  deleteSpendingItem(item: SpendingItem): void {
    if (!this.selectedRC || !this.selectedFY) return;
    if (!confirm(`Are you sure you want to delete "${item.name}"?`)) return;

    this.spendingItemService.deleteSpendingItem(this.selectedRC.id, this.selectedFY.id, item.id).subscribe({
      next: () => {
        this.showSuccess('Spending item deleted successfully');
        this.loadSpendingItems();
      },
      error: (error) => {
        this.showError('Failed to delete spending item: ' + error.message);
      }
    });
  }

  /**
   * Get the status label for display.
   */
  getStatusLabel(status: SpendingItemStatus): string {
    return SPENDING_STATUS_INFO[status]?.label || status;
  }

  /**
   * Get the CSS class for a status badge.
   */
  getStatusClass(status: SpendingItemStatus): string {
    return `status-${SPENDING_STATUS_INFO[status]?.color || 'secondary'}`;
  }

  /**
   * Format a number as currency.
   */
  formatCurrency(value: number | null | undefined, currency: string = 'CAD'): string {
    if (value === null || value === undefined) return '$0.00';
    return new Intl.NumberFormat('en-CA', {
      style: 'currency',
      currency: currency
    }).format(value);
  }

  /**
   * Get the flag emoji for a currency code.
   *
   * @param currencyCode ISO 4217 currency code
   * @returns Flag emoji string representing the currency's country/region
   */
  getCurrencyFlag(currencyCode: string): string {
    return getCurrencyFlag(currencyCode);
  }

  /**
   * Calculate the total for a spending item from allocations.
   */
  calculateTotal(item: SpendingItem): number {
    if (!item.moneyAllocations) return 0;
    return item.moneyAllocations.reduce((sum, alloc) => 
      sum + (alloc.capAmount || 0) + (alloc.omAmount || 0), 0);
  }

  /**
   * Get a category name by ID.
   */
  getCategoryName(categoryId: number): string {
    const category = this.categories.find(c => c.id === categoryId);
    return category ? category.name : 'Unknown';
  }

  /**
   * Show error message.
   */
  private showError(message: string): void {
    this.errorMessage = message;
    this.successMessage = null;
    setTimeout(() => this.errorMessage = null, 5000);
  }

  /**
   * Show success message.
   */
  private showSuccess(message: string): void {
    this.successMessage = message;
    this.errorMessage = null;
    setTimeout(() => this.successMessage = null, 3000);
  }

  /**
   * Clear all messages.
   */
  private clearMessages(): void {
    this.errorMessage = null;
    this.successMessage = null;
  }
}
