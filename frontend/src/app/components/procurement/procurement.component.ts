/*
 * myRC - Procurement Component
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/user.model';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ResponsibilityCentreService } from '../../services/responsibility-centre.service';
import { FiscalYearService } from '../../services/fiscal-year.service';
import { ProcurementService, ProcurementItemCreateRequest, QuoteCreateRequest, ToggleSpendingLinkResponse } from '../../services/procurement.service';
import { CurrencyService } from '../../services/currency.service';
import { FuzzySearchService } from '../../services/fuzzy-search.service';
import { CategoryService } from '../../services/category.service';
import { CurrencyInputDirective } from '../../directives/currency-input.directive';
import { ResponsibilityCentreDTO } from '../../models/responsibility-centre.model';
import { FiscalYear } from '../../models/fiscal-year.model';
import { Category } from '../../models/category.model';
import {
  ProcurementItem,
  ProcurementQuote,
  ProcurementQuoteFile,
  ProcurementItemStatus,
  QuoteStatus,
  TrackingStatus,
  PROCUREMENT_STATUS_INFO,
  QUOTE_STATUS_INFO,
  TRACKING_STATUS_INFO,
  ProcurementEvent,
  ProcurementEventType,
  ProcurementEventRequest,
  EVENT_TYPE_INFO
} from '../../models/procurement.model';
import { Currency, DEFAULT_CURRENCY, getCurrencyFlag } from '../../models/currency.model';

/**
 * Procurement component for managing procurement items, quotes, and files.
 *
 * @author myRC Team
 * @version 1.1.0
 * @since 2026-01-28
 */
@Component({
  selector: 'app-procurement',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule, CurrencyInputDirective],
  templateUrl: './procurement.component.html',
  styleUrls: ['./procurement.component.scss'],
})
export class ProcurementComponent implements OnInit, OnDestroy {
  currentUser: User | null = null;

  // Selected RC and FY
  selectedRC: ResponsibilityCentreDTO | null = null;
  selectedFY: FiscalYear | null = null;

  // Procurement Items
  procurementItems: ProcurementItem[] = [];
  isLoadingItems = false;
  searchTerm = '';
  filtersExpanded = true;

  // Categories
  categories: Category[] = [];
  isLoadingCategories = false;
  selectedCategoryId: number | null = null;

  // Selected Item for detail view
  selectedItem: ProcurementItem | null = null;
  quotes: ProcurementQuote[] = [];
  isLoadingQuotes = false;

  // Selected Quote for file view
  selectedQuote: ProcurementQuote | null = null;
  files: ProcurementQuoteFile[] = [];
  isLoadingFiles = false;

  // Procurement Events (Tracking)
  events: ProcurementEvent[] = [];
  isLoadingEvents = false;
  showEventsPanel = false;
  showCreateEventForm = false;
  isCreatingEvent = false;
  editingEvent: ProcurementEvent | null = null;
  newEventType: ProcurementEventType = 'NOT_STARTED';
  newEventDate: string = '';
  newEventComment: string = '';
  eventTypeOptions: ProcurementEventType[] = [
    'NOT_STARTED', 'QUOTE', 'SAM_ACKNOWLEDGEMENT_REQUESTED', 'SAM_ACKNOWLEDGEMENT_RECEIVED',
    'PACKAGE_SENT_TO_PROCUREMENT', 'ACKNOWLEDGED_BY_PROCUREMENT', 'PAUSED', 'CANCELLED',
    'CONTRACT_AWARDED', 'GOODS_RECEIVED', 'FULL_INVOICE_RECEIVED', 'PARTIAL_INVOICE_RECEIVED',
    'MONTHLY_INVOICE_RECEIVED', 'FULL_INVOICE_SIGNED', 'PARTIAL_INVOICE_SIGNED',
    'MONTHLY_INVOICE_SIGNED', 'CONTRACT_AMENDED'
  ];

  // Currencies
  currencies: Currency[] = [];
  isLoadingCurrencies = false;

  // Create Procurement Form
  showCreateItemForm = false;
  isCreatingItem = false;
  newItemPR = '';
  newItemPO = '';
  newItemName = '';
  newItemDescription = '';
  newItemVendor = '';
  newItemFinalPrice: number | null = null;
  newItemFinalPriceCurrency = DEFAULT_CURRENCY;
  newItemFinalPriceExchangeRate: number | null = null;
  newItemFinalPriceCad: number | null = null;
  newItemQuotedPrice: number | null = null;
  newItemQuotedPriceCurrency = DEFAULT_CURRENCY;
  newItemQuotedPriceExchangeRate: number | null = null;
  newItemQuotedPriceCad: number | null = null;
  newItemContractNumber = '';
  newItemContractStartDate = '';
  newItemContractEndDate = '';
  newItemProcurementCompleted = false;
  newItemProcurementCompletedDate = '';
  newItemCategoryId: number | null = null;

  // Create Quote Form
  showCreateQuoteForm = false;
  isCreatingQuote = false;
  newQuoteVendorName = '';
  newQuoteVendorContact = '';
  newQuoteReference = '';
  newQuoteAmount: number | null = null;
  newQuoteAmountCap: number | null = null;
  newQuoteAmountOm: number | null = null;
  newQuoteCurrency = DEFAULT_CURRENCY;
  newQuoteExchangeRate: number | null = null;
  newQuoteAmountCapCad: number | null = null;
  newQuoteAmountOmCad: number | null = null;
  newQuoteReceivedDate = '';
  newQuoteExpiryDate = '';
  newQuoteNotes = '';
  newQuoteFile: File | null = null;
  newQuoteFileDescription = '';

  // Edit Quote Form
  editingQuoteId: number | null = null;
  isUpdatingQuote = false;
  editQuoteVendorName = '';
  editQuoteVendorContact = '';
  editQuoteReference = '';
  editQuoteAmount: number | null = null;
  editQuoteAmountCap: number | null = null;
  editQuoteAmountOm: number | null = null;
  editQuoteCurrency = DEFAULT_CURRENCY;
  editQuoteExchangeRate: number | null = null;
  editQuoteAmountCapCad: number | null = null;
  editQuoteAmountOmCad: number | null = null;
  editQuoteReceivedDate = '';
  editQuoteExpiryDate = '';
  editQuoteNotes = '';

  // File Upload
  showFileUpload = false;
  isUploadingFile = false;
  selectedFile: File | null = null;
  fileDescription = '';

  // File Replace
  replacingFileId: number | null = null;
  replacementFile: File | null = null;
  replacementFileDescription = '';
  isReplacingFile = false;

  // File Preview
  previewingFile: ProcurementQuoteFile | null = null;
  previewUrl: any = null;
  previewTextContent: string = '';
  isLoadingPreview = false;

  // Messages
  errorMessage: string | null = null;
  successMessage: string | null = null;

  // Edit Procurement Item Form
  editingItemId: number | null = null;
  isUpdatingItem = false;
  editItemPR = '';
  editItemPO = '';
  editItemName = '';
  editItemDescription = '';
  editItemVendor = '';
  editItemFinalPrice: number | null = null;
  editItemFinalPriceCurrency = DEFAULT_CURRENCY;
  editItemFinalPriceExchangeRate: number | null = null;
  editItemFinalPriceCad: number | null = null;
  editItemQuotedPrice: number | null = null;
  editItemQuotedPriceCurrency = DEFAULT_CURRENCY;
  editItemQuotedPriceExchangeRate: number | null = null;
  editItemQuotedPriceCad: number | null = null;
  editItemContractNumber = '';
  editItemContractStartDate = '';
  editItemContractEndDate = '';
  editItemProcurementCompleted = false;
  editItemProcurementCompletedDate = '';
  editItemCategoryId: number | null = null;
  editItemTrackingStatus: TrackingStatus = 'ON_TRACK';

  // Status options - these are the new status values tracked via procurement events
  statusOptions: ProcurementItemStatus[] = [
    'DRAFT', 'PENDING_QUOTES', 'QUOTES_RECEIVED', 'UNDER_REVIEW',
    'APPROVED', 'PO_ISSUED', 'COMPLETED', 'CANCELLED'
  ];

  // Procurement statuses for dropdown
  procurementStatuses: ProcurementItemStatus[] = [
    'DRAFT', 'PENDING_QUOTES', 'QUOTES_RECEIVED', 'UNDER_REVIEW',
    'APPROVED', 'PO_ISSUED', 'COMPLETED', 'CANCELLED'
  ];

  private destroy$ = new Subject<void>();
  private pendingExpandItemId: number | null = null;

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private rcService: ResponsibilityCentreService,
    private fyService: FiscalYearService,
    private procurementService: ProcurementService,
    private currencyService: CurrencyService,
    private fuzzySearchService: FuzzySearchService,
    private categoryService: CategoryService,
    private sanitizer: DomSanitizer,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    this.loadCurrencies();

    // Check for expandItem query parameter
    this.route.queryParams.pipe(takeUntil(this.destroy$)).subscribe(params => {
      if (params['expandItem']) {
        this.pendingExpandItemId = +params['expandItem'];
      }
    });

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
            this.loadCategories();
            this.loadProcurementItems();
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
        this.currencies = [
          { code: 'CAD', name: 'Canadian Dollar', symbol: '$', isDefault: true }
        ];
        this.isLoadingCurrencies = false;
      }
    });
  }

  /**
   * Load categories from the API.
   */
  private loadCategories(): void {
    if (!this.selectedRC || !this.selectedFY) return;

    this.isLoadingCategories = true;
    this.categoryService.getCategoriesByFY(this.selectedRC.id, this.selectedFY.id).subscribe({
      next: (categories) => {
        this.categories = categories;
        this.isLoadingCategories = false;
      },
      error: () => {
        this.categories = [];
        this.isLoadingCategories = false;
      }
    });
  }

  /**
   * Load procurement items for the current FY.
   */
  loadProcurementItems(): void {
    if (!this.selectedRC || !this.selectedFY) return;

    this.isLoadingItems = true;
    const search = this.searchTerm.trim() || undefined;

    this.procurementService.getProcurementItems(
      this.selectedRC.id,
      this.selectedFY.id,
      undefined, // status filter removed - now using client-side fuzzy search
      search
    ).subscribe({
      next: (items) => {
        this.procurementItems = items;
        this.isLoadingItems = false;
        
        // Handle pending expand item from query params
        if (this.pendingExpandItemId) {
          const itemToExpand = items.find(i => i.id === this.pendingExpandItemId);
          if (itemToExpand) {
            setTimeout(() => {
              this.viewItemDetails(itemToExpand);
              // Scroll the item into view
              const element = document.getElementById(`procurement-item-${itemToExpand.id}`);
              if (element) {
                element.scrollIntoView({ behavior: 'smooth', block: 'center' });
              }
            }, 100);
          }
          this.pendingExpandItemId = null;
          // Clear the query param
          this.router.navigate([], { queryParams: {}, replaceUrl: true });
        }
      },
      error: (error) => {
        this.procurementItems = [];
        this.isLoadingItems = false;
        this.showError('Failed to load procurement items: ' + error.message);
      }
    });
  }

  /**
   * Get sorted and filtered list of procurement items.
   * Uses fuzzy search to filter items by PR, PO, name, description, vendor, or contract number.
   */
  get filteredProcurementItems(): ProcurementItem[] {
    let items = [...this.procurementItems];

    // Apply client-side fuzzy search filter (supplements backend search)
    if (this.searchTerm.trim()) {
      items = this.fuzzySearchService.filter(
        items,
        this.searchTerm,
        (item: ProcurementItem) => ({
          purchaseRequisition: item.purchaseRequisition,
          purchaseOrder: item.purchaseOrder,
          name: item.name,
          description: item.description,
          vendor: item.vendor,
          contractNumber: item.contractNumber,
          categoryName: item.categoryName,
          status: item.currentStatus ? (PROCUREMENT_STATUS_INFO[item.currentStatus]?.label || item.currentStatus) : ''
        })
      );
    }

    // Apply category filter
    if (this.selectedCategoryId !== null) {
      items = items.filter(item => item.categoryId === this.selectedCategoryId);
    }
    
    return items;
  }

  /**
   * Get the translated "Uncategorized" label.
   */
  getUncategorizedLabel(): string {
    return this.translate.instant('common.uncategorized');
  }

  /**
   * Get procurement items grouped by category.
   * Returns an array of category groups, each containing the category name and its items.
   */
  get groupedProcurementItems(): { categoryName: string; categoryId: number | null; items: ProcurementItem[] }[] {
    const filteredItems = this.filteredProcurementItems;
    const groups = new Map<string, { categoryName: string; categoryId: number | null; items: ProcurementItem[] }>();
    const uncategorizedLabel = this.getUncategorizedLabel();

    // Group items by category
    for (const item of filteredItems) {
      const categoryName = item.categoryName || uncategorizedLabel;
      const categoryId = item.categoryId || null;
      
      if (!groups.has(categoryName)) {
        groups.set(categoryName, { categoryName, categoryId, items: [] });
      }
      groups.get(categoryName)!.items.push(item);
    }

    // Convert to array and sort by category name (Uncategorized last)
    return Array.from(groups.values()).sort((a, b) => {
      if (a.categoryId === null) return 1;
      if (b.categoryId === null) return -1;
      return a.categoryName.localeCompare(b.categoryName, undefined, { sensitivity: 'base' });
    });
  }

  /**
   * TrackBy function for procurement items to optimize rendering.
   */
  trackByItemId(index: number, item: ProcurementItem): number {
    return item.id;
  }

  /**
   * TrackBy function for category groups to optimize rendering.
   */
  trackByGroupName(index: number, group: { categoryName: string; categoryId: number | null; items: ProcurementItem[] }): string {
    return group.categoryName;
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
   * Check if user can write to the selected RC.
   */
  get canWrite(): boolean {
    if (!this.selectedRC) {
      return false;
    }
    return this.selectedRC.isOwner || this.selectedRC.accessLevel === 'READ_WRITE';
  }

  /**
   * Check if the selected item's category allows CAP amounts.
   */
  get selectedItemAllowsCap(): boolean {
    if (!this.selectedItem || !this.selectedItem.categoryId) {
      return true; // Default to allowing CAP if no category
    }
    const category = this.categories.find(c => c.id === this.selectedItem!.categoryId);
    return category ? category.allowsCap : true;
  }

  /**
   * Check if the selected item's category allows OM amounts.
   */
  get selectedItemAllowsOm(): boolean {
    if (!this.selectedItem || !this.selectedItem.categoryId) {
      return true; // Default to allowing OM if no category
    }
    const category = this.categories.find(c => c.id === this.selectedItem!.categoryId);
    return category ? category.allowsOm : true;
  }

  // ==========================
  // Procurement Item Methods
  // ==========================

  /**
   * Filter procurement items by category.
   * Clicking the same category again will remove the filter (toggle behavior).
   */
  filterByCategory(categoryId: number | null): void {
    // Toggle off if clicking the same category
    if (this.selectedCategoryId === categoryId) {
      this.selectedCategoryId = null;
    } else {
      this.selectedCategoryId = categoryId;
    }
  }

  onSearch(): void {
    // With fuzzy search, we do client-side filtering
    // Backend search is still available but less necessary
  }

  showCreateItem(): void {
    this.showCreateItemForm = true;
    this.resetCreateItemForm();
  }

  cancelCreateItem(): void {
    this.showCreateItemForm = false;
    this.resetCreateItemForm();
  }

  private resetCreateItemForm(): void {
    this.newItemPR = '';
    this.newItemPO = '';
    this.newItemName = '';
    this.newItemDescription = '';
    this.newItemVendor = '';
    this.newItemFinalPrice = null;
    this.newItemFinalPriceCurrency = DEFAULT_CURRENCY;
    this.newItemFinalPriceExchangeRate = null;
    this.newItemFinalPriceCad = null;
    this.newItemQuotedPrice = null;
    this.newItemQuotedPriceCurrency = DEFAULT_CURRENCY;
    this.newItemQuotedPriceExchangeRate = null;
    this.newItemQuotedPriceCad = null;
    this.newItemContractNumber = '';
    this.newItemContractStartDate = '';
    this.newItemContractEndDate = '';
    this.newItemProcurementCompleted = false;
    this.newItemProcurementCompletedDate = '';
    this.newItemCategoryId = null;
  }

  createProcurementItem(): void {
    if (!this.selectedRC || !this.selectedFY || !this.newItemName.trim()) {
      return;
    }

    this.isCreatingItem = true;
    this.clearMessages();

    const request: ProcurementItemCreateRequest = {
      purchaseRequisition: this.newItemPR.trim() || undefined,
      purchaseOrder: this.newItemPO.trim() || undefined,
      name: this.newItemName.trim(),
      description: this.newItemDescription.trim() || undefined,
      vendor: this.newItemVendor.trim() || undefined,
      finalPrice: this.newItemFinalPrice,
      finalPriceCurrency: this.newItemFinalPriceCurrency,
      finalPriceExchangeRate: this.newItemFinalPriceCurrency !== 'CAD' ? this.newItemFinalPriceExchangeRate : undefined,
      finalPriceCad: this.newItemFinalPriceCurrency !== 'CAD' ? this.newItemFinalPriceCad : undefined,
      quotedPrice: this.newItemQuotedPrice,
      quotedPriceCurrency: this.newItemQuotedPriceCurrency,
      quotedPriceExchangeRate: this.newItemQuotedPriceCurrency !== 'CAD' ? this.newItemQuotedPriceExchangeRate : undefined,
      quotedPriceCad: this.newItemQuotedPriceCurrency !== 'CAD' ? this.newItemQuotedPriceCad : undefined,
      contractNumber: this.newItemContractNumber.trim() || undefined,
      contractStartDate: this.newItemContractStartDate || undefined,
      contractEndDate: this.newItemContractEndDate || undefined,
      procurementCompleted: this.newItemProcurementCompleted,
      procurementCompletedDate: this.newItemProcurementCompletedDate || undefined,
      categoryId: this.newItemCategoryId
    };

    this.procurementService.createProcurementItem(this.selectedRC.id, this.selectedFY.id, request).subscribe({
      next: () => {
        this.showSuccess('Procurement item created successfully');
        this.showCreateItemForm = false;
        this.resetCreateItemForm();
        this.loadProcurementItems();
        this.isCreatingItem = false;
      },
      error: (error) => {
        this.showError('Failed to create procurement item: ' + error.message);
        this.isCreatingItem = false;
      }
    });
  }

  deleteProcurementItem(item: ProcurementItem): void {
    if (!this.selectedRC || !this.selectedFY) return;
    if (!confirm(`Are you sure you want to delete "${item.name}" (PR: ${item.purchaseRequisition})?`)) return;

    this.procurementService.deleteProcurementItem(this.selectedRC.id, this.selectedFY.id, item.id).subscribe({
      next: () => {
        this.showSuccess('Procurement item deleted successfully');
        this.loadProcurementItems();
        if (this.selectedItem?.id === item.id) {
          this.selectedItem = null;
          this.quotes = [];
        }
        // Cancel edit if deleted item was being edited
        if (this.editingItemId === item.id) {
          this.editingItemId = null;
        }
      },
      error: (error) => {
        this.showError('Failed to delete procurement item: ' + error.message);
      }
    });
  }

  /**
   * Start editing a procurement item.
   */
  startEditProcurementItem(item: ProcurementItem): void {
    this.editingItemId = item.id;
    this.editItemPR = item.purchaseRequisition || '';
    this.editItemPO = item.purchaseOrder || '';
    this.editItemName = item.name;
    this.editItemDescription = item.description || '';
    this.editItemVendor = item.vendor || '';
    // Use explicit null checks for numeric fields to preserve 0 values
    this.editItemFinalPrice = item.finalPrice !== undefined && item.finalPrice !== null ? item.finalPrice : null;
    this.editItemFinalPriceCurrency = item.finalPriceCurrency || DEFAULT_CURRENCY;
    this.editItemFinalPriceExchangeRate = item.finalPriceExchangeRate !== undefined && item.finalPriceExchangeRate !== null ? item.finalPriceExchangeRate : null;
    this.editItemFinalPriceCad = item.finalPriceCad !== undefined && item.finalPriceCad !== null ? item.finalPriceCad : null;
    this.editItemQuotedPrice = item.quotedPrice !== undefined && item.quotedPrice !== null ? item.quotedPrice : null;
    this.editItemQuotedPriceCurrency = item.quotedPriceCurrency || DEFAULT_CURRENCY;
    this.editItemQuotedPriceExchangeRate = item.quotedPriceExchangeRate !== undefined && item.quotedPriceExchangeRate !== null ? item.quotedPriceExchangeRate : null;
    this.editItemQuotedPriceCad = item.quotedPriceCad !== undefined && item.quotedPriceCad !== null ? item.quotedPriceCad : null;
    this.editItemContractNumber = item.contractNumber || '';
    this.editItemContractStartDate = item.contractStartDate ? item.contractStartDate.split('T')[0] : '';
    this.editItemContractEndDate = item.contractEndDate ? item.contractEndDate.split('T')[0] : '';
    this.editItemProcurementCompleted = item.procurementCompleted || false;
    this.editItemProcurementCompletedDate = item.procurementCompletedDate ? item.procurementCompletedDate.split('T')[0] : '';
    this.editItemCategoryId = item.categoryId !== undefined && item.categoryId !== null ? item.categoryId : null;
    this.editItemTrackingStatus = (item.trackingStatus as TrackingStatus) || 'ON_TRACK';
    
    // Expand the item to show the edit form
    this.selectedItem = item;
  }

  /**
   * Cancel editing a procurement item.
   */
  cancelEditProcurementItem(): void {
    this.editingItemId = null;
    this.resetEditItemForm();
  }

  /**
   * Reset the edit item form.
   */
  private resetEditItemForm(): void {
    this.editItemPR = '';
    this.editItemPO = '';
    this.editItemName = '';
    this.editItemDescription = '';
    this.editItemVendor = '';
    this.editItemFinalPrice = null;
    this.editItemFinalPriceCurrency = DEFAULT_CURRENCY;
    this.editItemFinalPriceExchangeRate = null;
    this.editItemFinalPriceCad = null;
    this.editItemQuotedPrice = null;
    this.editItemQuotedPriceCurrency = DEFAULT_CURRENCY;
    this.editItemQuotedPriceExchangeRate = null;
    this.editItemQuotedPriceCad = null;
    this.editItemContractNumber = '';
    this.editItemContractStartDate = '';
    this.editItemContractEndDate = '';
    this.editItemProcurementCompleted = false;
    this.editItemProcurementCompletedDate = '';
    this.editItemCategoryId = null;
    this.editItemTrackingStatus = 'ON_TRACK';
  }

  /**
   * Update a procurement item.
   */
  updateProcurementItem(): void {
    if (!this.selectedRC || !this.selectedFY || !this.editingItemId) {
      return;
    }

    if (!this.editItemName?.trim()) {
      this.showError('Item name is required.');
      return;
    }

    this.isUpdatingItem = true;

    const updateRequest: Partial<ProcurementItemCreateRequest> = {
      purchaseRequisition: this.editItemPR?.trim() || undefined,
      purchaseOrder: this.editItemPO?.trim() || undefined,
      name: this.editItemName.trim(),
      description: this.editItemDescription?.trim() || undefined,
      vendor: this.editItemVendor?.trim() || undefined,
      finalPrice: this.editItemFinalPrice,
      finalPriceCurrency: this.editItemFinalPriceCurrency,
      finalPriceExchangeRate: this.editItemFinalPriceCurrency !== 'CAD' ? this.editItemFinalPriceExchangeRate : undefined,
      finalPriceCad: this.editItemFinalPriceCurrency !== 'CAD' ? this.editItemFinalPriceCad : undefined,
      quotedPrice: this.editItemQuotedPrice,
      quotedPriceCurrency: this.editItemQuotedPriceCurrency,
      quotedPriceExchangeRate: this.editItemQuotedPriceCurrency !== 'CAD' ? this.editItemQuotedPriceExchangeRate : undefined,
      quotedPriceCad: this.editItemQuotedPriceCurrency !== 'CAD' ? this.editItemQuotedPriceCad : undefined,
      contractNumber: this.editItemContractNumber?.trim() || undefined,
      contractStartDate: this.editItemContractStartDate || undefined,
      contractEndDate: this.editItemContractEndDate || undefined,
      procurementCompleted: this.editItemProcurementCompleted,
      procurementCompletedDate: this.editItemProcurementCompleted && this.editItemProcurementCompletedDate 
        ? this.editItemProcurementCompletedDate 
        : undefined,
      categoryId: this.editItemCategoryId || undefined,
      trackingStatus: this.editItemTrackingStatus
    };

    this.procurementService.updateProcurementItem(this.selectedRC.id, this.selectedFY.id, this.editingItemId, updateRequest)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (updatedItem) => {
          // Update the item in the list
          const index = this.procurementItems.findIndex(pi => pi.id === updatedItem.id);
          if (index !== -1) {
            this.procurementItems[index] = updatedItem;
          }
          // Update selectedItem if it's the one being edited
          if (this.selectedItem?.id === updatedItem.id) {
            this.selectedItem = updatedItem;
          }
          this.editingItemId = null;
          this.isUpdatingItem = false;
          this.resetEditItemForm();
          this.showSuccess(`Procurement Item "${updatedItem.name}" updated successfully.`);
        },
        error: (error) => {
          this.showError(error.message || 'Failed to update procurement item.');
          this.isUpdatingItem = false;
        }
      });
  }

  /**
   * Update the tracking status for a procurement item.
   */
  updateTrackingStatus(item: ProcurementItem, status: TrackingStatus): void {
    if (!this.selectedRC || !this.selectedFY || !this.canWrite) {
      return;
    }

    // Prevent unnecessary updates
    const currentStatus = item.trackingStatus || 'ON_TRACK';
    if (currentStatus === status) {
      return;
    }

    this.clearMessages();

    const updateRequest = {
      trackingStatus: status
    };

    this.procurementService.updateProcurementItem(this.selectedRC.id, this.selectedFY.id, item.id, updateRequest)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (updatedItem) => {
          // Update the item in the list
          const index = this.procurementItems.findIndex(pi => pi.id === updatedItem.id);
          if (index !== -1) {
            this.procurementItems[index] = updatedItem;
          }
          // Update selectedItem if it's the one being updated
          if (this.selectedItem?.id === updatedItem.id) {
            this.selectedItem = updatedItem;
          }
          this.showSuccess(this.translate.instant('procurement.trackingStatusUpdated', { status: this.getTrackingStatusLabel(status) }));
        },
        error: (error) => {
          this.showError(error.message || this.translate.instant('procurement.trackingStatusError'));
        }
      });
  }

  /**
   * Toggle item details - expand if collapsed, collapse if expanded
   * @param item - The procurement item to toggle
   */
  toggleItemDetails(item: ProcurementItem): void {
    if (this.selectedItem?.id === item.id) {
      // Collapse if same item clicked
      this.closeItemDetails();
    } else {
      // Expand new item
      this.viewItemDetails(item);
    }
  }

  viewItemDetails(item: ProcurementItem): void {
    this.selectedItem = item;
    this.selectedQuote = null;
    this.files = [];
    this.events = [];
    this.showEventsPanel = false;
    this.showCreateEventForm = false;
    this.editingEvent = null;
    this.loadQuotes();
    // Load events in background
    this.loadEvents();
  }

  closeItemDetails(): void {
    this.selectedItem = null;
    this.quotes = [];
    this.selectedQuote = null;
    this.files = [];
    this.events = [];
    this.showEventsPanel = false;
    this.showCreateEventForm = false;
    this.editingEvent = null;
  }

  // ==========================
  // Quote Methods
  // ==========================

  loadQuotes(): void {
    if (!this.selectedRC || !this.selectedFY || !this.selectedItem) return;

    this.isLoadingQuotes = true;
    this.procurementService.getQuotes(
      this.selectedRC.id,
      this.selectedFY.id,
      this.selectedItem.id
    ).subscribe({
      next: (quotes) => {
        this.quotes = quotes;
        this.isLoadingQuotes = false;
      },
      error: (error) => {
        this.quotes = [];
        this.isLoadingQuotes = false;
        this.showError('Failed to load quotes: ' + error.message);
      }
    });
  }

  showCreateQuote(): void {
    this.showCreateQuoteForm = true;
    this.resetCreateQuoteForm();
  }

  cancelCreateQuote(): void {
    this.showCreateQuoteForm = false;
    this.resetCreateQuoteForm();
  }

  private resetCreateQuoteForm(): void {
    this.newQuoteVendorName = '';
    this.newQuoteVendorContact = '';
    this.newQuoteReference = '';
    this.newQuoteAmount = null;
    this.newQuoteAmountCap = null;
    this.newQuoteAmountOm = null;
    this.newQuoteCurrency = DEFAULT_CURRENCY;
    this.newQuoteExchangeRate = null;
    this.newQuoteAmountCapCad = null;
    this.newQuoteAmountOmCad = null;
    this.newQuoteReceivedDate = '';
    this.newQuoteExpiryDate = '';
    this.newQuoteNotes = '';
    this.newQuoteFile = null;
    this.newQuoteFileDescription = '';
  }

  /**
   * Handle file selection for quote attachment.
   */
  onQuoteFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.newQuoteFile = input.files[0];
    }
  }

  /**
   * Remove the selected quote file.
   */
  removeQuoteFile(): void {
    this.newQuoteFile = null;
    this.newQuoteFileDescription = '';
  }

  /**
   * Format file size for display.
   */
  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  createQuote(): void {
    if (!this.selectedRC || !this.selectedFY || !this.selectedItem || !this.newQuoteVendorName.trim()) {
      return;
    }

    this.isCreatingQuote = true;
    this.clearMessages();

    const request: QuoteCreateRequest = {
      vendorName: this.newQuoteVendorName.trim(),
      vendorContact: this.newQuoteVendorContact.trim() || undefined,
      quoteReference: this.newQuoteReference.trim() || undefined,
      amount: this.newQuoteAmount ?? undefined,
      amountCap: this.newQuoteAmountCap ?? undefined,
      amountOm: this.newQuoteAmountOm ?? undefined,
      currency: this.newQuoteCurrency,
      exchangeRate: this.newQuoteCurrency !== 'CAD' ? this.newQuoteExchangeRate ?? undefined : undefined,
      amountCapCad: this.newQuoteCurrency !== 'CAD' ? this.newQuoteAmountCapCad ?? undefined : undefined,
      amountOmCad: this.newQuoteCurrency !== 'CAD' ? this.newQuoteAmountOmCad ?? undefined : undefined,
      receivedDate: this.newQuoteReceivedDate || undefined,
      expiryDate: this.newQuoteExpiryDate || undefined,
      notes: this.newQuoteNotes.trim() || undefined
    };

    this.procurementService.createQuote(
      this.selectedRC.id,
      this.selectedFY.id,
      this.selectedItem.id,
      request
    ).subscribe({
      next: (quote) => {
        // If a file was attached, upload it to the newly created quote
        if (this.newQuoteFile) {
          this.procurementService.uploadFile(
            this.selectedRC!.id,
            this.selectedFY!.id,
            this.selectedItem!.id,
            quote.id,
            this.newQuoteFile,
            this.newQuoteFileDescription.trim() || undefined
          ).subscribe({
            next: () => {
              this.showSuccess('Quote added with file attachment');
              this.showCreateQuoteForm = false;
              this.resetCreateQuoteForm();
              this.loadQuotes();
              this.isCreatingQuote = false;
            },
            error: (fileError) => {
              this.showSuccess('Quote added, but file upload failed: ' + fileError.message);
              this.showCreateQuoteForm = false;
              this.resetCreateQuoteForm();
              this.loadQuotes();
              this.isCreatingQuote = false;
            }
          });
        } else {
          this.showSuccess('Quote added successfully');
          this.showCreateQuoteForm = false;
          this.resetCreateQuoteForm();
          this.loadQuotes();
          this.isCreatingQuote = false;
        }
      },
      error: (error) => {
        this.showError('Failed to add quote: ' + error.message);
        this.isCreatingQuote = false;
      }
    });
  }

  deleteQuote(quote: ProcurementQuote): void {
    if (!this.selectedRC || !this.selectedFY || !this.selectedItem) return;
    if (!confirm(`Are you sure you want to delete the quote from "${quote.vendorName}"?`)) return;

    this.procurementService.deleteQuote(
      this.selectedRC.id,
      this.selectedFY.id,
      this.selectedItem.id,
      quote.id
    ).subscribe({
      next: () => {
        this.showSuccess('Quote deleted successfully');
        this.loadQuotes();
        if (this.selectedQuote?.id === quote.id) {
          this.selectedQuote = null;
          this.files = [];
        }
      },
      error: (error) => {
        this.showError('Failed to delete quote: ' + error.message);
      }
    });
  }

  /**
   * Start editing a quote.
   */
  startEditQuote(quote: ProcurementQuote): void {
    this.editingQuoteId = quote.id;
    this.editQuoteVendorName = quote.vendorName || '';
    this.editQuoteVendorContact = quote.vendorContact || '';
    this.editQuoteReference = quote.quoteReference || '';
    this.editQuoteAmount = quote.amount ?? null;
    this.editQuoteAmountCap = quote.amountCap ?? null;
    this.editQuoteAmountOm = quote.amountOm ?? null;
    this.editQuoteCurrency = quote.currency || DEFAULT_CURRENCY;
    this.editQuoteExchangeRate = quote.exchangeRate ?? null;
    this.editQuoteAmountCapCad = quote.amountCapCad ?? null;
    this.editQuoteAmountOmCad = quote.amountOmCad ?? null;
    this.editQuoteReceivedDate = quote.receivedDate ? quote.receivedDate.split('T')[0] : '';
    this.editQuoteExpiryDate = quote.expiryDate ? quote.expiryDate.split('T')[0] : '';
    this.editQuoteNotes = quote.notes || '';
    this.showCreateQuoteForm = false;
  }

  /**
   * Cancel editing a quote.
   */
  cancelEditQuote(): void {
    this.editingQuoteId = null;
    this.resetEditQuoteForm();
  }

  /**
   * Reset the edit quote form fields.
   */
  private resetEditQuoteForm(): void {
    this.editQuoteVendorName = '';
    this.editQuoteVendorContact = '';
    this.editQuoteReference = '';
    this.editQuoteAmount = null;
    this.editQuoteAmountCap = null;
    this.editQuoteAmountOm = null;
    this.editQuoteCurrency = DEFAULT_CURRENCY;
    this.editQuoteExchangeRate = null;
    this.editQuoteAmountCapCad = null;
    this.editQuoteAmountOmCad = null;
    this.editQuoteReceivedDate = '';
    this.editQuoteExpiryDate = '';
    this.editQuoteNotes = '';
  }

  /**
   * Save the edited quote.
   */
  updateQuote(): void {
    if (!this.selectedRC || !this.selectedFY || !this.selectedItem || !this.editingQuoteId || !this.editQuoteVendorName.trim()) {
      return;
    }

    this.isUpdatingQuote = true;
    this.clearMessages();

    const request: QuoteCreateRequest = {
      vendorName: this.editQuoteVendorName.trim(),
      vendorContact: this.editQuoteVendorContact.trim() || undefined,
      quoteReference: this.editQuoteReference.trim() || undefined,
      amount: this.editQuoteAmount ?? undefined,
      amountCap: this.editQuoteAmountCap ?? undefined,
      amountOm: this.editQuoteAmountOm ?? undefined,
      currency: this.editQuoteCurrency,
      exchangeRate: this.editQuoteCurrency !== 'CAD' ? this.editQuoteExchangeRate ?? undefined : undefined,
      amountCapCad: this.editQuoteCurrency !== 'CAD' ? this.editQuoteAmountCapCad ?? undefined : undefined,
      amountOmCad: this.editQuoteCurrency !== 'CAD' ? this.editQuoteAmountOmCad ?? undefined : undefined,
      receivedDate: this.editQuoteReceivedDate || undefined,
      expiryDate: this.editQuoteExpiryDate || undefined,
      notes: this.editQuoteNotes.trim() || undefined
    };

    this.procurementService.updateQuote(
      this.selectedRC.id,
      this.selectedFY.id,
      this.selectedItem.id,
      this.editingQuoteId,
      request
    ).subscribe({
      next: () => {
        this.showSuccess('Quote updated successfully');
        this.editingQuoteId = null;
        this.resetEditQuoteForm();
        this.loadQuotes();
        this.isUpdatingQuote = false;
      },
      error: (error) => {
        this.showError('Failed to update quote: ' + error.message);
        this.isUpdatingQuote = false;
      }
    });
  }

  selectQuote(quote: ProcurementQuote): void {
    if (!this.selectedRC || !this.selectedFY || !this.selectedItem) return;
    if (!confirm(`Select the quote from "${quote.vendorName}"? This will reject other quotes.`)) return;

    this.procurementService.selectQuote(
      this.selectedRC.id,
      this.selectedFY.id,
      this.selectedItem.id,
      quote.id
    ).subscribe({
      next: () => {
        this.showSuccess('Quote selected successfully');
        this.loadQuotes();
      },
      error: (error) => {
        this.showError('Failed to select quote: ' + error.message);
      }
    });
  }

  viewQuoteFiles(quote: ProcurementQuote): void {
    this.selectedQuote = quote;
    this.loadFiles();
  }

  closeQuoteFiles(): void {
    this.selectedQuote = null;
    this.files = [];
    this.showFileUpload = false;
  }

  // ==========================
  // File Methods
  // ==========================

  loadFiles(): void {
    if (!this.selectedRC || !this.selectedFY || !this.selectedItem || !this.selectedQuote) return;

    this.isLoadingFiles = true;
    this.procurementService.getFiles(
      this.selectedRC.id,
      this.selectedFY.id,
      this.selectedItem.id,
      this.selectedQuote.id
    ).subscribe({
      next: (files) => {
        this.files = files;
        this.isLoadingFiles = false;
      },
      error: (error) => {
        this.files = [];
        this.isLoadingFiles = false;
        this.showError('Failed to load files: ' + error.message);
      }
    });
  }

  showFileUploadForm(): void {
    this.showFileUpload = true;
    this.selectedFile = null;
    this.fileDescription = '';
  }

  cancelFileUpload(): void {
    this.showFileUpload = false;
    this.selectedFile = null;
    this.fileDescription = '';
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
    }
  }

  uploadFile(): void {
    if (!this.selectedRC || !this.selectedFY || !this.selectedItem || !this.selectedQuote || !this.selectedFile) {
      return;
    }

    this.isUploadingFile = true;
    this.clearMessages();

    this.procurementService.uploadFile(
      this.selectedRC.id,
      this.selectedFY.id,
      this.selectedItem.id,
      this.selectedQuote.id,
      this.selectedFile,
      this.fileDescription.trim() || undefined
    ).subscribe({
      next: () => {
        this.showSuccess('File uploaded successfully');
        this.showFileUpload = false;
        this.selectedFile = null;
        this.fileDescription = '';
        this.loadFiles();
        this.isUploadingFile = false;
      },
      error: (error) => {
        this.showError('Failed to upload file: ' + error.message);
        this.isUploadingFile = false;
      }
    });
  }

  deleteFile(file: ProcurementQuoteFile): void {
    if (!this.selectedRC || !this.selectedFY || !this.selectedItem || !this.selectedQuote) return;
    if (!confirm(`Are you sure you want to delete "${file.fileName}"?`)) return;

    this.procurementService.deleteFile(
      this.selectedRC.id,
      this.selectedFY.id,
      this.selectedItem.id,
      this.selectedQuote.id,
      file.id
    ).subscribe({
      next: () => {
        this.showSuccess('File deleted successfully');
        this.loadFiles();
      },
      error: (error) => {
        this.showError('Failed to delete file: ' + error.message);
      }
    });
  }

  /**
   * Start the file replacement process.
   */
  startReplaceFile(file: ProcurementQuoteFile): void {
    this.replacingFileId = file.id;
    this.replacementFile = null;
    this.replacementFileDescription = file.description || '';
  }

  /**
   * Cancel file replacement.
   */
  cancelReplaceFile(): void {
    this.replacingFileId = null;
    this.replacementFile = null;
    this.replacementFileDescription = '';
  }

  /**
   * Handle replacement file selection.
   */
  onReplacementFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.replacementFile = input.files[0];
    }
  }

  /**
   * Replace an existing file.
   */
  replaceFile(): void {
    if (!this.selectedRC || !this.selectedFY || !this.selectedItem || !this.selectedQuote || !this.replacingFileId || !this.replacementFile) {
      return;
    }

    this.isReplacingFile = true;
    this.clearMessages();

    this.procurementService.replaceFile(
      this.selectedRC.id,
      this.selectedFY.id,
      this.selectedItem.id,
      this.selectedQuote.id,
      this.replacingFileId,
      this.replacementFile,
      this.replacementFileDescription.trim() || undefined
    ).subscribe({
      next: () => {
        this.showSuccess('File replaced successfully');
        this.replacingFileId = null;
        this.replacementFile = null;
        this.replacementFileDescription = '';
        this.loadFiles();
        this.isReplacingFile = false;
      },
      error: (error) => {
        this.showError('Failed to replace file: ' + error.message);
        this.isReplacingFile = false;
      }
    });
  }

  viewFile(file: ProcurementQuoteFile): void {
    if (!this.selectedRC || !this.selectedFY || !this.selectedItem || !this.selectedQuote) return;

    const url = this.procurementService.getFileViewUrl(
      this.selectedRC.id,
      this.selectedFY.id,
      this.selectedItem.id,
      this.selectedQuote.id,
      file.id
    );
    window.open(url, '_blank');
  }

  downloadFile(file: ProcurementQuoteFile): void {
    if (!this.selectedRC || !this.selectedFY || !this.selectedItem || !this.selectedQuote) return;

    const url = this.procurementService.getFileDownloadUrl(
      this.selectedRC.id,
      this.selectedFY.id,
      this.selectedItem.id,
      this.selectedQuote.id,
      file.id
    );
    window.open(url, '_blank');
  }

  getFileIcon(contentType: string): string {
    if (contentType.startsWith('image/')) return '🖼️';
    if (contentType === 'application/pdf') return '📄';
    if (contentType.includes('word')) return '📝';
    if (contentType.includes('excel') || contentType.includes('spreadsheet')) return '📊';
    if (contentType.startsWith('text/')) return '📃';
    return '📎';
  }

  isPreviewable(contentType: string): boolean {
    return contentType === 'application/pdf' ||
           contentType.startsWith('image/') ||
           contentType.startsWith('text/');
  }

  previewFile(file: ProcurementQuoteFile): void {
    if (!this.selectedRC || !this.selectedFY || !this.selectedItem || !this.selectedQuote) return;

    this.previewingFile = file;
    this.isLoadingPreview = true;
    this.previewUrl = null;
    this.previewTextContent = '';

    const url = this.procurementService.getFileViewUrl(
      this.selectedRC.id,
      this.selectedFY.id,
      this.selectedItem.id,
      this.selectedQuote.id,
      file.id
    );

    // For PDFs and images, we can use the URL directly in an iframe/img
    if (file.contentType === 'application/pdf' || file.contentType.startsWith('image/')) {
      this.previewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(url);
      this.isLoadingPreview = false;
    } else if (file.contentType.startsWith('text/')) {
      // For text files, fetch the content
      fetch(url, { credentials: 'include' })
        .then(response => response.text())
        .then(text => {
          this.previewTextContent = text;
          this.previewUrl = true; // Just to trigger display
          this.isLoadingPreview = false;
        })
        .catch(() => {
          this.showError('Failed to load file preview');
          this.isLoadingPreview = false;
          this.previewingFile = null;
        });
    } else {
      this.isLoadingPreview = false;
    }
  }

  closePreview(): void {
    this.previewingFile = null;
    this.previewUrl = null;
    this.previewTextContent = '';
    this.isLoadingPreview = false;
  }

  // ==========================
  // Procurement Event Methods
  // ==========================

  /**
   * Toggle the events panel visibility.
   */
  toggleEventsPanel(): void {
    this.showEventsPanel = !this.showEventsPanel;
    if (this.showEventsPanel && this.events.length === 0) {
      this.loadEvents();
    }
  }

  /**
   * Load events for the selected procurement item.
   */
  loadEvents(): void {
    if (!this.selectedRC || !this.selectedFY || !this.selectedItem) return;

    this.isLoadingEvents = true;
    this.procurementService.getEvents(
      this.selectedRC.id,
      this.selectedFY.id,
      this.selectedItem.id
    ).subscribe({
      next: (events) => {
        this.events = events;
        this.isLoadingEvents = false;
      },
      error: (error) => {
        this.events = [];
        this.isLoadingEvents = false;
        this.showError('Failed to load events: ' + error.message);
      }
    });
  }

  /**
   * Show the create event form.
   */
  showCreateEvent(): void {
    this.showCreateEventForm = true;
    this.editingEvent = null;
    this.resetEventForm();
    // Set default date to today
    this.newEventDate = new Date().toISOString().split('T')[0];
  }

  /**
   * Cancel creating/editing an event.
   */
  cancelEventForm(): void {
    this.showCreateEventForm = false;
    this.editingEvent = null;
    this.resetEventForm();
  }

  /**
   * Reset the event form.
   */
  private resetEventForm(): void {
    this.newEventType = 'NOT_STARTED';
    this.newEventDate = new Date().toISOString().split('T')[0];
    this.newEventComment = '';
  }

  /**
   * Create a new procurement event.
   */
  createEvent(): void {
    if (!this.selectedRC || !this.selectedFY || !this.selectedItem) return;
    if (!this.newEventDate.trim()) {
      this.showError('Event date is required');
      return;
    }

    this.isCreatingEvent = true;
    this.clearMessages();

    const request: ProcurementEventRequest = {
      eventType: this.newEventType,
      eventDate: this.newEventDate,
      comment: this.newEventComment.trim() || undefined
    };

    this.procurementService.createEvent(
      this.selectedRC.id,
      this.selectedFY.id,
      this.selectedItem.id,
      request
    ).subscribe({
      next: () => {
        this.showSuccess('Event created successfully');
        this.showCreateEventForm = false;
        this.resetEventForm();
        this.loadEvents();
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
  editEvent(event: ProcurementEvent): void {
    this.editingEvent = event;
    this.showCreateEventForm = true;
    this.newEventType = event.eventType;
    this.newEventDate = event.eventDate;
    this.newEventComment = event.comment || '';
  }

  /**
   * Update an existing event.
   */
  updateEvent(): void {
    if (!this.selectedRC || !this.selectedFY || !this.selectedItem || !this.editingEvent) return;
    if (!this.newEventDate.trim()) {
      this.showError('Event date is required');
      return;
    }

    this.isCreatingEvent = true;
    this.clearMessages();

    const request: ProcurementEventRequest = {
      eventType: this.newEventType,
      eventDate: this.newEventDate,
      comment: this.newEventComment.trim() || undefined
    };

    this.procurementService.updateEvent(
      this.selectedRC.id,
      this.selectedFY.id,
      this.selectedItem.id,
      this.editingEvent.id,
      request
    ).subscribe({
      next: () => {
        this.showSuccess('Event updated successfully');
        this.showCreateEventForm = false;
        this.editingEvent = null;
        this.resetEventForm();
        this.loadEvents();
        this.isCreatingEvent = false;
      },
      error: (error) => {
        this.showError('Failed to update event: ' + error.message);
        this.isCreatingEvent = false;
      }
    });
  }

  /**
   * Delete a procurement event.
   */
  deleteEvent(event: ProcurementEvent): void {
    if (!this.selectedRC || !this.selectedFY || !this.selectedItem) return;
    if (!confirm(`Are you sure you want to delete this event?`)) return;

    this.procurementService.deleteEvent(
      this.selectedRC.id,
      this.selectedFY.id,
      this.selectedItem.id,
      event.id
    ).subscribe({
      next: () => {
        this.showSuccess('Event deleted successfully');
        this.loadEvents();
        if (this.editingEvent?.id === event.id) {
          this.editingEvent = null;
          this.showCreateEventForm = false;
          this.resetEventForm();
        }
      },
      error: (error) => {
        this.showError('Failed to delete event: ' + error.message);
      }
    });
  }

  /**
   * Get the label for an event type.
   */
  getEventTypeLabel(eventType: ProcurementEventType): string {
    return EVENT_TYPE_INFO[eventType]?.label || eventType;
  }

  /**
   * Get the description for an event type.
   */
  getEventTypeDescription(eventType: ProcurementEventType): string {
    return EVENT_TYPE_INFO[eventType]?.description || '';
  }

  /**
   * Get the icon for an event type.
   */
  getEventTypeIcon(eventType: ProcurementEventType): string {
    return EVENT_TYPE_INFO[eventType]?.icon || '📌';
  }

  /**
   * Get the color class for an event type.
   */
  getEventTypeClass(eventType: ProcurementEventType): string {
    return `event-${EVENT_TYPE_INFO[eventType]?.color || 'gray'}`;
  }

  /**
   * Get the most recent tracking event (first in the sorted list).
   * Returns null if there are no events.
   */
  get mostRecentEvent(): ProcurementEvent | null {
    return this.events.length > 0 ? this.events[0] : null;
  }

  // ==========================
  // Helper Methods
  // ==========================

  /**
   * Get the tracking status label.
   */
  getTrackingStatusLabel(status: TrackingStatus | string | undefined): string {
    if (!status) return TRACKING_STATUS_INFO['ON_TRACK'].label;
    return TRACKING_STATUS_INFO[status as TrackingStatus]?.label || status;
  }

  /**
   * Get the tracking status CSS class.
   */
  getTrackingStatusClass(status: TrackingStatus | string | undefined): string {
    if (!status) return 'status-green';
    return `status-${TRACKING_STATUS_INFO[status as TrackingStatus]?.color || 'green'}`;
  }

  /**
   * Get the tracking status icon.
   */
  getTrackingStatusIcon(status: TrackingStatus | string | undefined): string {
    if (!status) return TRACKING_STATUS_INFO['ON_TRACK'].icon;
    return TRACKING_STATUS_INFO[status as TrackingStatus]?.icon || '✅';
  }

  /**
   * Get the available tracking status options.
   */
  trackingStatusOptions: TrackingStatus[] = ['ON_TRACK', 'AT_RISK', 'CANCELLED'];

  getStatusLabel(status: ProcurementItemStatus): string {
    return PROCUREMENT_STATUS_INFO[status]?.label || status;
  }

  getStatusClass(status: ProcurementItemStatus): string {
    return `status-${PROCUREMENT_STATUS_INFO[status]?.color || 'gray'}`;
  }

  /**
   * Get the status label from a string status (for event newStatus).
   */
  getEventStatusLabel(status: string | undefined): string {
    if (!status) return '';
    return PROCUREMENT_STATUS_INFO[status as ProcurementItemStatus]?.label || status;
  }

  /**
   * Get the status class from a string status (for event newStatus).
   */
  getEventStatusClass(status: string | undefined): string {
    if (!status) return '';
    return `status-${PROCUREMENT_STATUS_INFO[status as ProcurementItemStatus]?.color || 'gray'}`;
  }

  getQuoteStatusLabel(status: QuoteStatus): string {
    return QUOTE_STATUS_INFO[status]?.label || status;
  }

  getQuoteStatusClass(status: QuoteStatus): string {
    return `status-${QUOTE_STATUS_INFO[status]?.color || 'gray'}`;
  }

  formatCurrency(value: number | null | undefined, currency: string = 'CAD'): string {
    if (value === null || value === undefined) return '-';
    return new Intl.NumberFormat('en-CA', {
      style: 'currency',
      currency: currency
    }).format(value);
  }

  getCurrencyFlag(currencyCode: string): string {
    return getCurrencyFlag(currencyCode);
  }

  private showError(message: string): void {
    this.errorMessage = message;
    this.successMessage = null;
    setTimeout(() => this.errorMessage = null, 5000);
  }

  private showSuccess(message: string): void {
    this.successMessage = message;
    this.errorMessage = null;
    setTimeout(() => this.successMessage = null, 3000);
  }

  private clearMessages(): void {
    this.errorMessage = null;
    this.successMessage = null;
  }

  /**
   * Navigate to the spending page to view linked spending items.
   */
  viewLinkedSpendingItems(): void {
    if (!this.selectedItem || !this.selectedItem.linkedSpendingItemIds || this.selectedItem.linkedSpendingItemIds.length === 0) {
      return;
    }
    // Navigate with the first linked spending item ID to expand
    this.router.navigate(['/app/spending'], {
      queryParams: { expandItem: this.selectedItem.linkedSpendingItemIds[0] }
    });
  }

  /**
   * Check if the selected item has linked spending items.
   */
  hasLinkedSpendingItems(): boolean {
    return this.selectedItem?.linkedSpendingItemIds != null && this.selectedItem.linkedSpendingItemIds.length > 0;
  }

  // State for spending link toggle
  isTogglingSpendingLink = false;
  showSpendingLinkWarning = false;
  spendingLinkWarningMessage: string | null = null;

  /**
   * Toggle the spending link for the selected procurement item.
   * Creates a spending item if none exists, or unlinks if one exists.
   */
  toggleSpendingLink(forceUnlink = false): void {
    if (!this.selectedRC || !this.selectedFY || !this.selectedItem) {
      return;
    }

    this.isTogglingSpendingLink = true;
    this.clearMessages();

    this.procurementService.toggleSpendingLink(
      this.selectedRC.id,
      this.selectedFY.id,
      this.selectedItem.id,
      forceUnlink
    ).subscribe({
      next: (response: ToggleSpendingLinkResponse) => {
        this.isTogglingSpendingLink = false;
        
        if (response.hasWarning && !forceUnlink) {
          // Show confirmation dialog
          this.showSpendingLinkWarning = true;
          this.spendingLinkWarningMessage = response.warningMessage;
        } else {
          // Update the selected item with new data
          this.selectedItem = response.procurementItem;
          const index = this.procurementItems.findIndex(i => i.id === response.procurementItem.id);
          if (index !== -1) {
            this.procurementItems[index] = response.procurementItem;
          }
          
          const actionKey = response.spendingLinked 
            ? 'procurement.spendingLinkCreated' 
            : 'procurement.spendingLinkRemoved';
          this.showSuccess(this.translate.instant(actionKey));
          this.showSpendingLinkWarning = false;
          this.spendingLinkWarningMessage = null;
        }
      },
      error: (err) => {
        this.isTogglingSpendingLink = false;
        
        // Check if this is a conflict (warning) response
        if (err.status === 409 && err.error?.hasWarning) {
          this.showSpendingLinkWarning = true;
          this.spendingLinkWarningMessage = err.error.warningMessage;
        } else {
          this.showError(err.message || this.translate.instant('PROCUREMENT.SPENDING_LINK_ERROR'));
        }
      }
    });
  }

  /**
   * Confirm unlinking the spending item when there's a modification warning.
   */
  confirmUnlinkSpending(): void {
    this.showSpendingLinkWarning = false;
    this.toggleSpendingLink(true);
  }

  /**
   * Cancel the spending link unlink operation.
   */
  cancelUnlinkSpending(): void {
    this.showSpendingLinkWarning = false;
    this.spendingLinkWarningMessage = null;
  }
}
