/*
 * myRC - Spending Component
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/user.model';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ResponsibilityCentreService } from '../../services/responsibility-centre.service';
import { FiscalYearService } from '../../services/fiscal-year.service';
import { SpendingItemService, SpendingItemCreateRequest, SpendingItemUpdateRequest } from '../../services/spending-item.service';
import { SpendingEventService } from '../../services/spending-event.service';
import { CategoryService } from '../../services/category.service';
import { MoneyService } from '../../services/money.service';
import { CurrencyService } from '../../services/currency.service';
import { FuzzySearchService } from '../../services/fuzzy-search.service';
import { UserPreferencesService, UserDisplayPreferences } from '../../services/user-preferences.service';
import { ResponsibilityCentreDTO } from '../../models/responsibility-centre.model';
import { FiscalYear } from '../../models/fiscal-year.model';
import { SpendingItem, SpendingMoneyAllocation, SpendingItemStatus, SPENDING_STATUS_INFO } from '../../models/spending-item.model';
import { SpendingEvent, SpendingEventType, SPENDING_EVENT_TYPE_INFO, SpendingEventRequest } from '../../models/spending-event.model';
import { Category, categoryAllowsCap, categoryAllowsOm } from '../../models/category.model';
import { Money } from '../../models/money.model';
import { Currency, DEFAULT_CURRENCY, getCurrencyFlag } from '../../models/currency.model';
import { CurrencyInputDirective } from '../../directives/currency-input.directive';
import { DateInputDirective } from '../../directives/date-input.directive';
import { EVENT_TYPE_INFO, ProcurementEventType } from '../../models/procurement.model';

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
  imports: [CommonModule, FormsModule, TranslateModule, CurrencyInputDirective, DateInputDirective],
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

  // Search filter
  searchTerm = '';
  filtersExpanded = false;

  // Summary Section
  summaryExpanded = false;

  // Create Form
  showCreateForm = false;
  isCreating = false;
  newItemName = '';
  newItemDescription = '';
  newItemVendor = '';
  newItemReferenceNumber = '';
  newItemAmount: number | null = null;
  newItemStatus: SpendingItemStatus = 'PLANNING';
  newItemCurrency = DEFAULT_CURRENCY;
  newItemExchangeRate: number | null = null;
  newItemCategoryId: number | null = null;
  newItemMoneyAllocations: SpendingMoneyAllocation[] = [];

  // Expandable item tracking
  expandedItemId: number | null = null;

  // Edit Form
  editingItemId: number | null = null;
  isUpdating = false;
  editItemName = '';
  editItemDescription = '';
  editItemVendor = '';
  editItemReferenceNumber = '';
  editItemEcoAmount: number | null = null;
  editItemStatus: SpendingItemStatus = 'PLANNING';
  editItemCurrency = DEFAULT_CURRENCY;
  editItemExchangeRate: number | null = null;
  editItemCategoryId: number | null = null;
  editItemMoneyAllocations: SpendingMoneyAllocation[] = [];

  // Messages
  errorMessage: string | null = null;
  successMessage: string | null = null;

  // Status list for dropdown
  statusOptions: SpendingItemStatus[] = ['PLANNING', 'COMMITTED', 'COMPLETED', 'CANCELLED'];

  // Summary data
  summaryByMoneyType: { moneyCode: string; moneyName: string; totalCap: number; totalOm: number; total: number }[] = [];
  summaryByCategory: { categoryName: string; totalCap: number; totalOm: number; total: number }[] = [];
  grandTotalCap = 0;
  grandTotalOm = 0;
  grandTotal = 0;

  // Category grouping interface
  groupedItems: { categoryName: string; categoryId: number | null; items: SpendingItem[] }[] = [];

  // Spending Events
  selectedItemEvents: SpendingEvent[] = [];
  selectedEventItemId: number | null = null;
  isLoadingEvents = false;
  
  // Add Event Modal
  showAddEventModal = false;
  addEventItemId: number | null = null;
  newEventType: SpendingEventType = 'PENDING';
  newEventDate = '';
  newEventComment = '';
  isCreatingEvent = false;
  
  // Edit Event
  editingEventId: number | null = null;
  editEventType: SpendingEventType = 'PENDING';
  editEventDate = '';
  editEventComment = '';
  isUpdatingEvent = false;

  // Event type options
  eventTypeOptions: SpendingEventType[] = [
    'PENDING', 'ECO_REQUESTED', 'ECO_RECEIVED', 
    'EXTERNAL_APPROVAL_REQUESTED', 'EXTERNAL_APPROVAL_RECEIVED',
    'SECTION_32_PROVIDED', 'RECEIVED_GOODS_SERVICES', 'SECTION_34_PROVIDED',
    'CREDIT_CARD_CLEARED', 'CANCELLED', 'ON_HOLD'
  ];

  // For navigation with query params (expand specific item)
  private pendingExpandItemId: number | null = null;

  // User display preferences
  displayPreferences: UserDisplayPreferences = { showSearchBox: true, showCategoryFilter: true, groupByCategory: false };

  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private rcService: ResponsibilityCentreService,
    private fyService: FiscalYearService,
    private spendingItemService: SpendingItemService,
    private spendingEventService: SpendingEventService,
    private categoryService: CategoryService,
    private moneyService: MoneyService,
    private currencyService: CurrencyService,
    private fuzzySearchService: FuzzySearchService,
    private translate: TranslateService,
    private userPreferencesService: UserPreferencesService
  ) {}

  ngOnInit(): void {
    // Subscribe to user display preferences
    this.userPreferencesService.preferences$.pipe(takeUntil(this.destroy$)).subscribe(prefs => {
      this.displayPreferences = prefs;
    });

    // Check for query params (e.g., expandItem=123)
    this.route.queryParams.pipe(takeUntil(this.destroy$)).subscribe(params => {
      if (params['expandItem']) {
        this.pendingExpandItemId = Number(params['expandItem']);
      }
    });

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
        this.calculateSummaries();

        // If we have a pending item to expand from query params, expand it and scroll to it
        if (this.pendingExpandItemId) {
          const itemToExpand = items.find(item => item.id === this.pendingExpandItemId);
          if (itemToExpand) {
            this.expandedItemId = itemToExpand.id;
            // Scroll to the item after a short delay to allow rendering
            setTimeout(() => {
              const element = document.getElementById('spending-item-' + this.pendingExpandItemId);
              if (element) {
                element.scrollIntoView({ behavior: 'smooth', block: 'center' });
              }
              this.pendingExpandItemId = null;
            }, 100);
          } else {
            this.pendingExpandItemId = null;
          }
        }
      },
      error: (error) => {
        this.spendingItems = [];
        this.isLoadingItems = false;
        this.showError('Failed to load spending items: ' + error.message);
      }
    });
  }

  /**
   * Calculate summary totals from spending items.
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
    const categoryMap = new Map<number | null, { categoryName: string; totalCap: number; totalOm: number }>();
    const uncategorizedLabel = this.getUncategorizedLabel();

    for (const item of this.spendingItems) {
      // Aggregate by category (using categoryId as key for proper grouping)
      const categoryId = item.categoryId || null;
      const catName = this.getCategoryDisplayNameById(categoryId, item.categoryName || uncategorizedLabel);
      if (!categoryMap.has(categoryId)) {
        categoryMap.set(categoryId, { categoryName: catName, totalCap: 0, totalOm: 0 });
      }
      const catEntry = categoryMap.get(categoryId)!;

      // Aggregate by money type
      if (item.moneyAllocations) {
        for (const allocation of item.moneyAllocations) {
          const moneyKey = allocation.moneyName || 'AB';
          if (!moneyMap.has(moneyKey)) {
            moneyMap.set(moneyKey, {
              moneyCode: allocation.moneyName || 'AB',
              moneyName: allocation.moneyName || 'AB',
              totalCap: 0,
              totalOm: 0
            });
          }
          const moneyEntry = moneyMap.get(moneyKey)!;
          moneyEntry.totalCap += allocation.capAmount || 0;
          moneyEntry.totalOm += allocation.omAmount || 0;

          // Also add to category totals
          catEntry.totalCap += allocation.capAmount || 0;
          catEntry.totalOm += allocation.omAmount || 0;

          // Add to grand totals
          this.grandTotalCap += allocation.capAmount || 0;
          this.grandTotalOm += allocation.omAmount || 0;
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
   * Clicking the same category again will remove the filter (toggle behavior).
   */
  filterByCategory(categoryId: number | null): void {
    // Toggle off if clicking the same category
    if (this.selectedCategoryId === categoryId) {
      this.selectedCategoryId = null;
    } else {
      this.selectedCategoryId = categoryId;
    }
    this.loadSpendingItems();
  }

  /**
   * Get sorted and filtered list of spending items.
   * Filters by selected category (if any), applies fuzzy search, and sorts alphabetically by name.
   */
  get filteredSpendingItems(): SpendingItem[] {
    let items = [...this.spendingItems];

    // Apply fuzzy search filter
    if (this.searchTerm.trim()) {
      items = this.fuzzySearchService.filter(
        items,
        this.searchTerm,
        (item: SpendingItem) => ({
          name: item.name,
          description: item.description,
          categoryName: item.categoryName,
          vendor: item.vendor,
          referenceNumber: item.referenceNumber,
          status: SPENDING_STATUS_INFO[item.status]?.label || item.status
        })
      );
    }
    
    return items.sort((a, b) =>
      a.name.localeCompare(b.name, undefined, { sensitivity: 'base' })
    );
  }

  /**
   * Get the translated "Uncategorized" label.
   */
  getUncategorizedLabel(): string {
    return this.translate.instant('common.uncategorized');
  }

  /**
   * Get the translated display name for a category.
   * Looks up the category by ID in the loaded categories array and uses
   * the translationKey for i18n. Falls back to the raw name for custom categories.
   *
   * @param categoryId the category ID to look up
   * @param fallbackName the fallback name to use if the category is not found
   * @returns the translated category name
   */
  getCategoryDisplayNameById(categoryId: number | null | undefined, fallbackName: string): string {
    if (!categoryId) return fallbackName;
    const category = this.categories.find(c => c.id === categoryId);
    if (category) {
      return this.getCategoryDisplayName(category);
    }
    return fallbackName;
  }

  /**
   * Get the display name for a category, using the translation key if available.
   * Default (system) categories use their translationKey for i18n.
   * Custom categories display their user-entered name directly.
   *
   * @param category the category to get the display name for
   * @returns the translated category name
   */
  getCategoryDisplayName(category: Category): string {
    if (category.translationKey) {
      const translated = this.translate.instant(category.translationKey);
      return translated !== category.translationKey ? translated : category.name;
    }
    return category.name;
  }

  /**
   * Track spending items by ID to prevent unnecessary re-renders.
   */
  trackByItemId(index: number, item: SpendingItem): number {
    return item.id;
  }

  /**
   * Track groups by category name to prevent unnecessary re-renders.
   */
  trackByGroupName(index: number, group: { categoryName: string; categoryId: number | null; items: SpendingItem[] }): string {
    return group.categoryName;
  }

  /**
   * Get spending items grouped by category.
   * Returns an array of category groups, each containing the category name and its items.
   * Items within each group are sorted alphabetically.
   */
  get groupedSpendingItems(): { categoryName: string; categoryId: number | null; items: SpendingItem[] }[] {
    const sortedItems = this.filteredSpendingItems;
    const groups = new Map<number | null, { categoryName: string; categoryId: number | null; items: SpendingItem[] }>();
    const uncategorizedLabel = this.getUncategorizedLabel();

    // Group items by category ID
    for (const item of sortedItems) {
      const categoryId = item.categoryId || null;
      const categoryName = this.getCategoryDisplayNameById(categoryId, item.categoryName || uncategorizedLabel);
      
      if (!groups.has(categoryId)) {
        groups.set(categoryId, { categoryName, categoryId, items: [] });
      }
      groups.get(categoryId)!.items.push(item);
    }

    // Convert to array and sort by category name (Uncategorized last)
    return Array.from(groups.values()).sort((a, b) => {
      if (a.categoryId === null) return 1;
      if (b.categoryId === null) return -1;
      return a.categoryName.localeCompare(b.categoryName, undefined, { sensitivity: 'base' });
    });
  }

  /**
   * Clear the search filter.
   */
  clearSearch(): void {
    this.searchTerm = '';
  }

  /**
   * Toggle the filters bar visibility.
   */
  toggleFilters(): void {
    this.filtersExpanded = !this.filtersExpanded;
  }

  /**
   * Toggle the summary section visibility.
   */
  toggleSummary(): void {
    this.summaryExpanded = !this.summaryExpanded;
  }

  /**
   * Handle category change in the create form.
   * Clears money allocations that don't match the new category's funding type.
   */
  onCategoryChange(): void {
    const allowsCap = this.selectedCategoryAllowsCap();
    const allowsOm = this.selectedCategoryAllowsOm();
    
    // Clear amounts that are not allowed by the new category
    this.newItemMoneyAllocations.forEach(allocation => {
      if (!allowsCap) {
        allocation.capAmount = 0;
      }
      if (!allowsOm) {
        allocation.omAmount = 0;
      }
    });
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
    this.newItemStatus = 'PLANNING';
    this.newItemCurrency = DEFAULT_CURRENCY;
    this.newItemExchangeRate = null;
    this.newItemCategoryId = this.categories.length > 0 ? this.categories[0].id : null;
    this.initializeNewMoneyAllocations();
  }

  /**
   * Check if the form has valid money allocation.
   * Takes into account the selected category's funding type.
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
        // Close expanded view if deleted item was expanded
        if (this.expandedItemId === item.id) {
          this.expandedItemId = null;
        }
        // Cancel edit if deleted item was being edited
        if (this.editingItemId === item.id) {
          this.editingItemId = null;
        }
        this.showSuccess('Spending item deleted successfully');
        this.loadSpendingItems();
      },
      error: (error) => {
        this.showError('Failed to delete spending item: ' + error.message);
      }
    });
  }

  /**
   * Start editing a spending item.
   */
  startEditSpendingItem(item: SpendingItem): void {
    this.editingItemId = item.id;
    this.editItemName = item.name;
    this.editItemDescription = item.description || '';
    this.editItemVendor = item.vendor || '';
    this.editItemReferenceNumber = item.referenceNumber || '';
    this.editItemEcoAmount = item.ecoAmount || null;
    this.editItemStatus = item.status;
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
   * Cancel editing a spending item.
   */
  cancelEditSpendingItem(): void {
    this.editingItemId = null;
    this.resetEditForm();
  }

  /**
   * Update a spending item.
   */
  updateSpendingItem(): void {
    if (!this.selectedRC || !this.selectedFY || !this.editingItemId) {
      return;
    }

    if (!this.editItemName?.trim()) {
      this.showError('Item name is required.');
      return;
    }

    if (!this.hasValidEditMoneyAllocation()) {
      this.showError('At least one money allocation must have a CAP or OM amount greater than $0.00.');
      return;
    }

    // Validate exchange rate for non-CAD currencies
    if (this.editItemCurrency !== 'CAD' && (!this.editItemExchangeRate || this.editItemExchangeRate <= 0)) {
      this.showError('Exchange rate is required for non-CAD currencies.');
      return;
    }

    this.isUpdating = true;

    const updateRequest: SpendingItemUpdateRequest = {
      name: this.editItemName.trim(),
      description: this.editItemDescription?.trim() || undefined,
      vendor: this.editItemVendor?.trim() || undefined,
      referenceNumber: this.editItemReferenceNumber?.trim() || undefined,
      ecoAmount: this.editItemEcoAmount ?? undefined,
      status: this.editItemStatus,
      currency: this.editItemCurrency,
      exchangeRate: this.editItemCurrency !== 'CAD' ? (this.editItemExchangeRate ?? undefined) : undefined,
      categoryId: this.editItemCategoryId || undefined,
      moneyAllocations: this.editItemMoneyAllocations
        .filter(a => (a.capAmount && a.capAmount > 0) || (a.omAmount && a.omAmount > 0))
        .map(a => ({
          moneyId: a.moneyId,
          moneyName: a.moneyName,
          capAmount: a.capAmount || 0,
          omAmount: a.omAmount || 0
        }))
    };

    this.spendingItemService.updateSpendingItem(this.selectedRC.id, this.selectedFY.id, this.editingItemId, updateRequest)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (updatedItem) => {
          // Update the item in the list
          const index = this.spendingItems.findIndex(si => si.id === updatedItem.id);
          if (index !== -1) {
            this.spendingItems[index] = updatedItem;
          }
          this.editingItemId = null;
          this.isUpdating = false;
          this.resetEditForm();
          this.showSuccess(`Spending Item "${updatedItem.name}" updated successfully.`);
          // Recalculate summaries
          this.calculateSummaries();
        },
        error: (error) => {
          this.showError(error.message || 'Failed to update spending item.');
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
    this.editItemVendor = '';
    this.editItemReferenceNumber = '';
    this.editItemEcoAmount = null;
    this.editItemStatus = 'PLANNING';
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
   * Toggle expanded details for a spending item.
   */
  toggleItemDetails(item: SpendingItem): void {
    this.expandedItemId = this.expandedItemId === item.id ? null : item.id;
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
  getCategoryById(categoryId: number): Category | undefined {
    return this.categories.find(c => c.id === categoryId);
  }

  /**
   * Check if a category allows CAP amounts.
   */
  categoryAllowsCapById(categoryId: number): boolean {
    const category = this.getCategoryById(categoryId);
    if (!category) return true;
    return categoryAllowsCap(category);
  }

  /**
   * Check if a category allows OM amounts.
   */
  categoryAllowsOmById(categoryId: number): boolean {
    const category = this.getCategoryById(categoryId);
    if (!category) return true;
    return categoryAllowsOm(category);
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

  /**
   * Calculate the total for a spending item in CAD.
   */
  calculateTotalCad(item: SpendingItem): number {
    const total = this.calculateTotal(item);
    if (item.currency === 'CAD' || !item.exchangeRate) {
      return total;
    }
    return total * item.exchangeRate;
  }

  /**
   * Get the procurement price in CAD (final price if available, otherwise quoted).
   */
  getProcurementPriceCad(item: SpendingItem): number {
    if (item.procurementFinalPriceCad != null) {
      return item.procurementFinalPriceCad;
    }
    if (item.procurementQuotedPriceCad != null) {
      return item.procurementQuotedPriceCad;
    }
    // Fallback to non-CAD prices if CAD versions not available
    if (item.procurementFinalPrice != null) {
      return item.procurementFinalPrice;
    }
    if (item.procurementQuotedPrice != null) {
      return item.procurementQuotedPrice;
    }
    return 0;
  }

  /**
   * Check if there's a price mismatch between spending and procurement.
   * Returns true if the spending total doesn't match the procurement price.
   */
  hasPriceMismatch(item: SpendingItem): boolean {
    // Only check for linked items with procurement prices
    if (!item.procurementItemId) return false;
    
    const procurementPriceCad = this.getProcurementPriceCad(item);
    if (procurementPriceCad === 0) return false; // No procurement price to compare
    
    const spendingTotalCad = this.calculateTotalCad(item);
    
    // Allow small tolerance for floating point comparison (0.01 CAD)
    const tolerance = 0.01;
    return Math.abs(spendingTotalCad - procurementPriceCad) > tolerance;
  }

  /**
   * Navigate to the procurement page to view the linked procurement item.
   */
  viewLinkedProcurement(): void {
    const item = this.expandedItemId ? this.spendingItems.find(i => i.id === this.expandedItemId) : null;
    if (item?.procurementItemId) {
      this.router.navigate(['/app/procurement'], { 
        queryParams: { expandItem: item.procurementItemId } 
      });
    } else {
      this.router.navigate(['/app/procurement']);
    }
  }

  // ========================================
  // Spending Event Methods
  // ========================================

  /**
   * Get the event count for a spending item.
   */
  getEventCount(item: SpendingItem): number {
    return item.eventCount || 0;
  }

  /**
   * Load events for a spending item.
   */
  loadEventsForItem(item: SpendingItem): void {
    if (!this.selectedRC || !this.selectedFY) return;
    
    this.selectedEventItemId = item.id;
    this.isLoadingEvents = true;
    
    this.spendingEventService.getEvents(
      this.selectedRC.id, this.selectedFY.id, item.id
    ).subscribe({
      next: (events) => {
        this.selectedItemEvents = events;
        this.isLoadingEvents = false;
      },
      error: (error) => {
        this.showError('Failed to load events: ' + error.message);
        this.isLoadingEvents = false;
      }
    });
  }

  /**
   * Open the inline add event form for an item.
   */
  openAddEventForm(item: SpendingItem): void {
    this.addEventItemId = item.id;
    this.newEventType = 'PENDING';
    this.newEventDate = new Date().toLocaleDateString('en-CA'); // YYYY-MM-DD format for input
    this.newEventComment = '';
  }

  /**
   * Collapse the expanded events list, returning to the collapsed summary view.
   */
  collapseEvents(): void {
    this.selectedItemEvents = [];
    this.selectedEventItemId = null;
  }

  /**
   * Alias for backwards compatibility.
   */
  openAddEventModal(item: SpendingItem): void {
    this.openAddEventForm(item);
  }

  /**
   * Close the inline add event form.
   */
  closeAddEventForm(): void {
    this.addEventItemId = null;
    this.newEventType = 'PENDING';
    this.newEventDate = '';
    this.newEventComment = '';
  }

  /**
   * Close the add event modal (alias for backwards compatibility).
   */
  closeAddEventModal(): void {
    this.closeAddEventForm();
    this.showAddEventModal = false;
  }

  /**
   * Create a new spending event.
   */
  createSpendingEvent(): void {
    if (!this.selectedRC || !this.selectedFY || !this.addEventItemId) return;
    
    this.isCreatingEvent = true;
    const itemId = this.addEventItemId; // Store the item ID before closing
    
    const request: SpendingEventRequest = {
      eventType: this.newEventType,
      eventDate: this.newEventDate,
      comment: this.newEventComment.trim() || undefined
    };
    
    this.spendingEventService.createEvent(
      this.selectedRC.id, this.selectedFY.id, itemId, request
    ).subscribe({
      next: () => {
        this.showSuccess('Event created successfully');
        this.closeAddEventModal();
        this.loadSpendingItems(); // Refresh to get updated event info
        // Reload events for the item where the event was just added
        this.selectedEventItemId = itemId;
        this.spendingEventService.getEvents(
          this.selectedRC!.id, this.selectedFY!.id, itemId
        ).subscribe({
          next: (events) => {
            this.selectedItemEvents = events;
          },
          error: () => {
            // Silently fail, the item list will still show the count
          }
        });
        this.isCreatingEvent = false;
      },
      error: (error) => {
        this.showError('Failed to create event: ' + error.message);
        this.isCreatingEvent = false;
      }
    });
  }

  /**
   * Start editing an event.
   */
  editEvent(event: SpendingEvent): void {
    this.editingEventId = event.id;
    this.editEventType = event.eventType;
    this.editEventDate = event.eventDate?.split('T')[0] || '';
    this.editEventComment = event.comment || '';
  }

  /**
   * Cancel editing an event.
   */
  cancelEditEvent(): void {
    this.editingEventId = null;
  }

  /**
   * Update an existing event.
   */
  updateSpendingEvent(): void {
    if (!this.selectedRC || !this.selectedFY || !this.editingEventId || !this.selectedEventItemId) return;
    
    this.isUpdatingEvent = true;
    const currentItemId = this.selectedEventItemId;
    
    const request: SpendingEventRequest = {
      eventType: this.editEventType,
      eventDate: this.editEventDate,
      comment: this.editEventComment.trim() || undefined
    };
    
    this.spendingEventService.updateEvent(
      this.selectedRC.id, this.selectedFY.id, currentItemId, this.editingEventId, request
    ).subscribe({
      next: () => {
        this.showSuccess('Event updated successfully');
        this.editingEventId = null;
        this.isUpdatingEvent = false;
        this.loadSpendingItems();
        // Reload events list independently
        this.spendingEventService.getEvents(
          this.selectedRC!.id, this.selectedFY!.id, currentItemId
        ).subscribe({
          next: (events) => {
            this.selectedItemEvents = events;
            this.selectedEventItemId = currentItemId;
          },
          error: () => { /* silently fail */ }
        });
      },
      error: (error) => {
        this.showError('Failed to update event: ' + error.message);
        this.isUpdatingEvent = false;
      }
    });
  }

  /**
   * Delete an event.
   */
  deleteEvent(event: SpendingEvent): void {
    if (!this.selectedRC || !this.selectedFY || !this.selectedEventItemId) return;
    
    const confirmed = confirm(this.translate.instant('common.deleteConfirm'));
    if (!confirmed) return;
    
    const currentItemId = this.selectedEventItemId;
    
    this.spendingEventService.deleteEvent(
      this.selectedRC.id, this.selectedFY.id, currentItemId, event.id
    ).subscribe({
      next: () => {
        this.showSuccess('Event deleted successfully');
        this.loadSpendingItems();
        // Reload events list independently
        this.spendingEventService.getEvents(
          this.selectedRC!.id, this.selectedFY!.id, currentItemId
        ).subscribe({
          next: (events) => {
            this.selectedItemEvents = events;
            this.selectedEventItemId = currentItemId;
          },
          error: () => { /* silently fail */ }
        });
      },
      error: (error) => {
        this.showError('Failed to delete event: ' + error.message);
      }
    });
  }

  // ========================================
  // Event Type Display Helpers
  // ========================================

  /**
   * Get label for a spending event type.
   */
  getSpendingEventLabel(eventType: string): string {
    const info = SPENDING_EVENT_TYPE_INFO[eventType as SpendingEventType];
    return info?.label || eventType;
  }

  /**
   * Get icon for a spending event type.
   */
  getSpendingEventIcon(eventType: string): string {
    const info = SPENDING_EVENT_TYPE_INFO[eventType as SpendingEventType];
    return info?.icon || '📌';
  }

  /**
   * Get CSS class for a spending event status.
   */
  getSpendingEventStatusClass(eventType: string): string {
    const info = SPENDING_EVENT_TYPE_INFO[eventType as SpendingEventType];
    return info ? `status-${info.color}` : 'status-gray';
  }

  /**
   * Get label for a procurement event type.
   */
  getEventTypeLabel(eventType: string): string {
    const info = EVENT_TYPE_INFO[eventType as ProcurementEventType];
    return info?.label || eventType;
  }

  /**
   * Get icon for a procurement event type.
   */
  getEventTypeIcon(eventType: string): string {
    const info = EVENT_TYPE_INFO[eventType as ProcurementEventType];
    return info?.icon || '📌';
  }

  /**
   * Get CSS class for a procurement tracking status.
   */
  getTrackingStatusClass(eventType: string): string {
    const info = EVENT_TYPE_INFO[eventType as ProcurementEventType];
    return info ? `status-${info.color}` : 'status-gray';
  }
}
