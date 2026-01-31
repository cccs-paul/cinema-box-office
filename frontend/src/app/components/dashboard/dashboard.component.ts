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
import { CategoryService } from '../../services/category.service';
import { FuzzySearchService } from '../../services/fuzzy-search.service';
import { ResponsibilityCentreDTO } from '../../models/responsibility-centre.model';
import { FiscalYear } from '../../models/fiscal-year.model';
import { FundingItem, FundingItemCreateRequest, FundingItemUpdateRequest, getSourceLabel, getSourceClass, FundingSource, MoneyAllocation } from '../../models/funding-item.model';
import { Currency, DEFAULT_CURRENCY, getCurrencyFlag } from '../../models/currency.model';
import { Money } from '../../models/money.model';
import { Category, categoryAllowsCap, categoryAllowsOm } from '../../models/category.model';

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

  // Categories
  categories: Category[] = [];
  isLoadingCategories = false;
  selectedCategoryId: number | null = null;

  // Search filter
  searchTerm = '';

  // Create Form
  showCreateForm = false;
  isCreating = false;
  newItemName = '';
  newItemDescription = '';
  newItemSource: FundingSource = 'BUSINESS_PLAN';
  newItemComments = '';
  newItemCurrency = DEFAULT_CURRENCY;
  newItemExchangeRate: number | null = null;
  newItemCategoryId: number | null = null;
  newItemMoneyAllocations: MoneyAllocation[] = [];

  // Expandable item tracking
  expandedItemId: number | null = null;

  // Edit Form
  editingItemId: number | null = null;
  isUpdating = false;
  editItemName = '';
  editItemDescription = '';
  editItemSource: FundingSource = 'BUSINESS_PLAN';
  editItemComments = '';
  editItemCurrency = DEFAULT_CURRENCY;
  editItemExchangeRate: number | null = null;
  editItemCategoryId: number | null = null;
  editItemMoneyAllocations: MoneyAllocation[] = [];

  // Messages
  errorMessage: string | null = null;
  successMessage: string | null = null;

  // Source list for dropdown
  sourceOptions: FundingSource[] = ['BUSINESS_PLAN', 'ON_RAMP', 'APPROVED_DEFICIT'];

  // Summary interfaces
  summaryByMoneyType: { moneyCode: string; moneyName: string; totalCap: number; totalOm: number; total: number }[] = [];
  summaryByCategory: { categoryName: string; totalCap: number; totalOm: number; total: number }[] = [];
  grandTotalCap = 0;
  grandTotalOm = 0;
  grandTotal = 0;

  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private router: Router,
    private rcService: ResponsibilityCentreService,
    private fyService: FiscalYearService,
    private fundingItemService: FundingItemService,
    private currencyService: CurrencyService,
    private moneyService: MoneyService,
    private categoryService: CategoryService,
    private fuzzySearchService: FuzzySearchService
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
   * RC must be loaded before FY to ensure proper data dependencies.
   */
  private loadSelectedContext(): void {
    const rcId = this.rcService.getSelectedRC();
    const fyId = this.rcService.getSelectedFY();

    if (!rcId || !fyId) {
      // Redirect to RC selection if not selected
      this.router.navigate(['/rc-selection']);
      return;
    }

    // Load RC details first, then FY within the callback to ensure proper sequencing
    this.rcService.getResponsibilityCentre(rcId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (rc) => {
          this.selectedRC = rc;
          // Load FY details after RC is loaded to ensure selectedRC is set
          this.fyService.getFiscalYear(rcId, fyId)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
              next: (fy) => {
                this.selectedFY = fy;
                this.loadFundingItems();
                this.loadMonies();
                this.loadCategories();
              },
              error: (error) => {
                console.error('Failed to load FY:', error);
                this.router.navigate(['/rc-selection']);
              }
            });
        },
        error: (error) => {
          console.error('Failed to load RC:', error);
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
   * Load categories for the selected FY.
   */
  loadCategories(): void {
    if (!this.selectedRC || !this.selectedFY) {
      return;
    }

    this.isLoadingCategories = true;
    this.categoryService.getCategoriesByFY(this.selectedRC.id, this.selectedFY.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (categories) => {
          this.categories = categories;
          this.isLoadingCategories = false;
        },
        error: (error) => {
          console.error('Failed to load categories:', error);
          this.isLoadingCategories = false;
          this.categories = [];
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
      omAmount: 0,
      isDefault: money.isDefault
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
          this.calculateSummaries();
        },
        error: (error) => {
          this.errorMessage = error.message || 'Failed to load funding items.';
          this.isLoadingItems = false;
        }
      });
  }

  /**
   * Calculate summary totals from funding items.
   */
  private calculateSummaries(): void {
    // Reset summaries
    this.summaryByMoneyType = [];
    this.summaryByCategory = [];
    this.grandTotalCap = 0;
    this.grandTotalOm = 0;
    this.grandTotal = 0;

    // Maps for aggregation
    const moneyMap = new Map<string, { moneyCode: string; moneyName: string; totalCap: number; totalOm: number }>();
    const categoryMap = new Map<string, { categoryName: string; totalCap: number; totalOm: number }>();

    for (const item of this.fundingItems) {
      // Add to grand totals
      this.grandTotalCap += item.totalCap || 0;
      this.grandTotalOm += item.totalOm || 0;

      // Aggregate by category
      const catName = item.categoryName || 'Uncategorized';
      if (!categoryMap.has(catName)) {
        categoryMap.set(catName, { categoryName: catName, totalCap: 0, totalOm: 0 });
      }
      const catEntry = categoryMap.get(catName)!;
      catEntry.totalCap += item.totalCap || 0;
      catEntry.totalOm += item.totalOm || 0;

      // Aggregate by money type
      if (item.moneyAllocations) {
        for (const allocation of item.moneyAllocations) {
          const moneyKey = allocation.moneyCode;
          if (!moneyMap.has(moneyKey)) {
            moneyMap.set(moneyKey, {
              moneyCode: allocation.moneyCode,
              moneyName: allocation.moneyName,
              totalCap: 0,
              totalOm: 0
            });
          }
          const moneyEntry = moneyMap.get(moneyKey)!;
          moneyEntry.totalCap += allocation.capAmount || 0;
          moneyEntry.totalOm += allocation.omAmount || 0;
        }
      }
    }

    // Convert maps to arrays
    this.summaryByMoneyType = Array.from(moneyMap.values()).map(entry => ({
      ...entry,
      total: entry.totalCap + entry.totalOm
    }));

    this.summaryByCategory = Array.from(categoryMap.values()).map(entry => ({
      ...entry,
      total: entry.totalCap + entry.totalOm
    }));

    this.grandTotal = this.grandTotalCap + this.grandTotalOm;
  }

  /**
   * Get sorted and filtered list of funding items.
   * Filters by selected category (if any), applies fuzzy search, and sorts alphabetically by name.
   */
  get sortedFundingItems(): FundingItem[] {
    let items = [...this.fundingItems];
    
    // Filter by category if selected
    if (this.selectedCategoryId !== null) {
      items = items.filter(item => item.categoryId === this.selectedCategoryId);
    }

    // Apply fuzzy search filter
    if (this.searchTerm.trim()) {
      items = this.fuzzySearchService.filter(
        items,
        this.searchTerm,
        (item: FundingItem) => ({
          name: item.name,
          description: item.description,
          categoryName: item.categoryName,
          source: this.getSourceLabel(item.source),
          comments: item.comments
        })
      );
    }
    
    return items.sort((a, b) =>
      a.name.localeCompare(b.name, undefined, { sensitivity: 'base' })
    );
  }

  /**
   * Clear the search filter.
   */
  clearSearch(): void {
    this.searchTerm = '';
  }

  /**
   * Filter funding items by category.
   * @param categoryId The category ID to filter by, or null for all categories
   */
  filterByCategory(categoryId: number | null): void {
    this.selectedCategoryId = categoryId;
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

    // Validate that at least one money allocation has a value > $0.00
    if (!this.hasValidMoneyAllocation()) {
      this.errorMessage = 'At least one money type must have a CAP or OM amount greater than $0.00';
      return;
    }

    this.isCreating = true;
    this.errorMessage = null;
    this.successMessage = null;

    const request: FundingItemCreateRequest = {
      name: this.newItemName.trim(),
      description: this.newItemDescription.trim(),
      source: this.newItemSource,
      comments: this.newItemComments.trim() || undefined,
      currency: this.newItemCurrency,
      exchangeRate: this.newItemCurrency !== DEFAULT_CURRENCY ? this.newItemExchangeRate || undefined : undefined,
      categoryId: this.newItemCategoryId,
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
          // Close expanded view if deleted item was expanded
          if (this.expandedItemId === item.id) {
            this.expandedItemId = null;
          }
          // Cancel edit if deleted item was being edited
          if (this.editingItemId === item.id) {
            this.editingItemId = null;
          }
          this.successMessage = `Funding Item "${item.name}" deleted successfully.`;
          setTimeout(() => this.clearSuccess(), 5000);
        },
        error: (error) => {
          this.errorMessage = error.message || 'Failed to delete funding item.';
        }
      });
  }

  /**
   * Start editing a funding item.
   */
  startEditFundingItem(item: FundingItem): void {
    this.editingItemId = item.id;
    this.editItemName = item.name;
    this.editItemDescription = item.description || '';
    this.editItemSource = item.source;
    this.editItemComments = item.comments || '';
    this.editItemCurrency = item.currency || DEFAULT_CURRENCY;
    this.editItemExchangeRate = item.exchangeRate || null;
    this.editItemCategoryId = item.categoryId || null;
    
    // Initialize edit money allocations from the item's existing allocations
    this.editItemMoneyAllocations = this.monies.map(money => {
      const existingAllocation = item.moneyAllocations?.find(a => a.moneyId === money.id);
      return {
        moneyId: money.id,
        moneyCode: money.code,
        moneyName: money.name,
        capAmount: existingAllocation?.capAmount || 0,
        omAmount: existingAllocation?.omAmount || 0,
        isDefault: money.isDefault
      };
    });

    // Expand the item to show the edit form
    this.expandedItemId = item.id;
  }

  /**
   * Cancel editing a funding item.
   */
  cancelEditFundingItem(): void {
    this.editingItemId = null;
    this.resetEditForm();
  }

  /**
   * Update a funding item.
   */
  updateFundingItem(): void {
    if (!this.selectedFY || !this.editingItemId) {
      return;
    }

    if (!this.editItemName?.trim()) {
      this.errorMessage = 'Item name is required.';
      return;
    }

    if (!this.hasValidEditMoneyAllocation()) {
      this.errorMessage = 'At least one money allocation must have a CAP or OM amount greater than $0.00.';
      return;
    }

    // Validate exchange rate for non-CAD currencies
    if (this.editItemCurrency !== 'CAD' && (!this.editItemExchangeRate || this.editItemExchangeRate <= 0)) {
      this.errorMessage = 'Exchange rate is required for non-CAD currencies.';
      return;
    }

    this.isUpdating = true;
    this.errorMessage = null;

    const updateRequest: FundingItemUpdateRequest = {
      name: this.editItemName.trim(),
      description: this.editItemDescription?.trim() || undefined,
      source: this.editItemSource,
      comments: this.editItemComments?.trim() || undefined,
      currency: this.editItemCurrency,
      exchangeRate: this.editItemCurrency !== 'CAD' ? (this.editItemExchangeRate ?? undefined) : undefined,
      categoryId: this.editItemCategoryId || undefined,
      moneyAllocations: this.editItemMoneyAllocations
        .filter(a => (a.capAmount && a.capAmount > 0) || (a.omAmount && a.omAmount > 0))
        .map(a => ({
          moneyId: a.moneyId,
          moneyCode: a.moneyCode,
          moneyName: a.moneyName,
          capAmount: a.capAmount || 0,
          omAmount: a.omAmount || 0
        }))
    };

    this.fundingItemService.updateFundingItem(this.selectedFY.id, this.editingItemId, updateRequest)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (updatedItem) => {
          // Update the item in the list
          const index = this.fundingItems.findIndex(fi => fi.id === updatedItem.id);
          if (index !== -1) {
            this.fundingItems[index] = updatedItem;
          }
          this.editingItemId = null;
          this.isUpdating = false;
          this.resetEditForm();
          this.successMessage = `Funding Item "${updatedItem.name}" updated successfully.`;
          setTimeout(() => this.clearSuccess(), 5000);
          // Recalculate summaries
          this.calculateSummaries();
        },
        error: (error) => {
          this.errorMessage = error.message || 'Failed to update funding item.';
          this.isUpdating = false;
        }
      });
  }

  /**
   * Reset the edit form.
   */
  private resetEditForm(): void {
    this.editItemName = '';
    this.editItemDescription = '';
    this.editItemSource = 'BUSINESS_PLAN';
    this.editItemComments = '';
    this.editItemCurrency = DEFAULT_CURRENCY;
    this.editItemExchangeRate = null;
    this.editItemCategoryId = null;
    this.editItemMoneyAllocations = [];
  }

  /**
   * Handle currency change in edit form.
   */
  onEditCurrencyChange(): void {
    if (this.editItemCurrency === 'CAD') {
      this.editItemExchangeRate = null;
    }
  }

  /**
   * Check if the edit form has at least one valid money allocation.
   */
  hasValidEditMoneyAllocation(): boolean {
    return this.editItemMoneyAllocations.some(a => 
      (this.editCategoryAllowsCap() && a.capAmount && a.capAmount > 0) ||
      (this.editCategoryAllowsOm() && a.omAmount && a.omAmount > 0)
    );
  }

  /**
   * Check if the selected category in edit form allows CAP allocations.
   */
  editCategoryAllowsCap(): boolean {
    if (!this.editItemCategoryId) {
      return true; // Default to allowing both
    }
    const category = this.categories.find(c => c.id === this.editItemCategoryId);
    if (!category || !category.fundingType) {
      return true;
    }
    return category.fundingType === 'BOTH' || category.fundingType === 'CAP_ONLY';
  }

  /**
   * Check if the selected category in edit form allows OM allocations.
   */
  editCategoryAllowsOm(): boolean {
    if (!this.editItemCategoryId) {
      return true; // Default to allowing both
    }
    const category = this.categories.find(c => c.id === this.editItemCategoryId);
    if (!category || !category.fundingType) {
      return true;
    }
    return category.fundingType === 'BOTH' || category.fundingType === 'OM_ONLY';
  }

  /**
   * Toggle expanded details for a funding item.
   */
  toggleItemDetails(item: FundingItem): void {
    this.expandedItemId = this.expandedItemId === item.id ? null : item.id;
  }

  /**
   * Reset the create form.
   */
  private resetForm(): void {
    this.newItemName = '';
    this.newItemDescription = '';
    this.newItemSource = 'BUSINESS_PLAN';
    this.newItemComments = '';
    this.newItemCurrency = DEFAULT_CURRENCY;
    this.newItemExchangeRate = null;
    this.newItemCategoryId = null;
    this.initializeMoneyAllocations();
  }

  /**
   * Get source display label.
   */
  getSourceLabel(source: FundingSource): string {
    return getSourceLabel(source);
  }

  /**
   * Get source CSS class.
   */
  getSourceClass(source: FundingSource): string {
    return getSourceClass(source);
  }

  /**
   * Format currency for display.
   */
  formatCurrency(amount: number | null | undefined, currencyCode?: string): string {
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
   * Get the flag emoji for a currency code.
   *
   * @param currencyCode ISO 4217 currency code
   * @returns Flag emoji string representing the currency's country/region
   */
  getCurrencyFlag(currencyCode: string): string {
    return getCurrencyFlag(currencyCode);
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
   * Check if at least one money allocation has a CAP or OM value greater than $0.00.
   * Takes into account the selected category's funding type.
   *
   * @returns true if valid allocation exists, false otherwise
   */
  hasValidMoneyAllocation(): boolean {
    if (!this.newItemMoneyAllocations || this.newItemMoneyAllocations.length === 0) {
      return false;
    }
    
    const allowsCap = this.selectedCategoryAllowsCap();
    const allowsOm = this.selectedCategoryAllowsOm();
    
    return this.newItemMoneyAllocations.some(allocation => {
      const hasValidCap = allowsCap && allocation.capAmount && allocation.capAmount > 0;
      const hasValidOm = allowsOm && allocation.omAmount && allocation.omAmount > 0;
      return hasValidCap || hasValidOm;
    });
  }

  /**
   * Get the currently selected category.
   */
  getSelectedCategory(): Category | undefined {
    if (!this.newItemCategoryId) return undefined;
    return this.categories.find(c => c.id === this.newItemCategoryId);
  }

  /**
   * Check if the selected category allows CAP amounts.
   */
  selectedCategoryAllowsCap(): boolean {
    const category = this.getSelectedCategory();
    if (!category) return true; // Default to allowing both if no category selected
    return categoryAllowsCap(category);
  }

  /**
   * Check if the selected category allows OM amounts.
   */
  selectedCategoryAllowsOm(): boolean {
    const category = this.getSelectedCategory();
    if (!category) return true; // Default to allowing both if no category selected
    return categoryAllowsOm(category);
  }

  /**
   * Get a category by ID.
   */
  getCategoryById(categoryId: number | undefined): Category | undefined {
    if (!categoryId) return undefined;
    return this.categories.find(c => c.id === categoryId);
  }

  /**
   * Check if a category allows CAP amounts.
   */
  categoryAllowsCapById(categoryId: number | null | undefined): boolean {
    const category = this.getCategoryById(categoryId ?? undefined);
    if (!category) return true;
    return categoryAllowsCap(category);
  }

  /**
   * Check if a category allows OM amounts.
   */
  categoryAllowsOmById(categoryId: number | null | undefined): boolean {
    const category = this.getCategoryById(categoryId ?? undefined);
    if (!category) return true;
    return categoryAllowsOm(category);
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
