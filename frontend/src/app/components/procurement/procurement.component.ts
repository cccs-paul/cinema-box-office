/*
 * myRC - Procurement Component
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/user.model';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ResponsibilityCentreService } from '../../services/responsibility-centre.service';
import { FiscalYearService } from '../../services/fiscal-year.service';
import { ProcurementService, ProcurementItemCreateRequest, QuoteCreateRequest } from '../../services/procurement.service';
import { CurrencyService } from '../../services/currency.service';
import { FuzzySearchService } from '../../services/fuzzy-search.service';
import { CategoryService } from '../../services/category.service';
import { ResponsibilityCentreDTO } from '../../models/responsibility-centre.model';
import { FiscalYear } from '../../models/fiscal-year.model';
import { Category } from '../../models/category.model';
import {
  ProcurementItem,
  ProcurementQuote,
  ProcurementQuoteFile,
  ProcurementItemStatus,
  QuoteStatus,
  PROCUREMENT_STATUS_INFO,
  QUOTE_STATUS_INFO,
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
  imports: [CommonModule, FormsModule],
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
  selectedStatusFilter: ProcurementItemStatus | null = null;
  searchTerm = '';

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
  newEventType: ProcurementEventType = 'NOTE_ADDED';
  newEventDate: string = '';
  newEventComment: string = '';
  eventTypeOptions: ProcurementEventType[] = [
    'NOTE_ADDED', 'STATUS_CHANGE', 'QUOTE_RECEIVED', 'QUOTE_SELECTED',
    'QUOTE_REJECTED', 'PO_ISSUED', 'DELIVERED', 'INVOICED',
    'PAYMENT_MADE', 'COMPLETED', 'CANCELLED', 'OTHER'
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
  newItemCurrency = DEFAULT_CURRENCY;
  newItemExchangeRate: number | null = null;
  newItemPreferredVendor = '';
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
  newQuoteCurrency = DEFAULT_CURRENCY;
  newQuoteReceivedDate = '';
  newQuoteExpiryDate = '';
  newQuoteNotes = '';
  newQuoteFile: File | null = null;
  newQuoteFileDescription = '';

  // File Upload
  showFileUpload = false;
  isUploadingFile = false;
  selectedFile: File | null = null;
  fileDescription = '';

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
  editItemCurrency = DEFAULT_CURRENCY;
  editItemExchangeRate: number | null = null;
  editItemPreferredVendor = '';
  editItemContractNumber = '';
  editItemContractStartDate = '';
  editItemContractEndDate = '';
  editItemProcurementCompleted = false;
  editItemProcurementCompletedDate = '';
  editItemCategoryId: number | null = null;
  editItemStatus: ProcurementItemStatus = 'DRAFT';

  // Status options
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

  constructor(
    private authService: AuthService,
    private router: Router,
    private rcService: ResponsibilityCentreService,
    private fyService: FiscalYearService,
    private procurementService: ProcurementService,
    private currencyService: CurrencyService,
    private fuzzySearchService: FuzzySearchService,
    private categoryService: CategoryService,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit(): void {
    this.loadCurrencies();

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
    const status = this.selectedStatusFilter || undefined;
    const search = this.searchTerm.trim() || undefined;

    this.procurementService.getProcurementItems(
      this.selectedRC.id,
      this.selectedFY.id,
      status,
      search
    ).subscribe({
      next: (items) => {
        this.procurementItems = items;
        this.isLoadingItems = false;
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
          preferredVendor: item.preferredVendor,
          contractNumber: item.contractNumber,
          categoryName: item.categoryName,
          status: PROCUREMENT_STATUS_INFO[item.status]?.label || item.status
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
   * Clear the search filter.
   */
  clearSearch(): void {
    this.searchTerm = '';
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

  // ==========================
  // Procurement Item Methods
  // ==========================

  filterByStatus(status: ProcurementItemStatus | null): void {
    this.selectedStatusFilter = status;
    this.loadProcurementItems();
  }

  /**
   * Filter procurement items by category.
   */
  filterByCategory(categoryId: number | null): void {
    this.selectedCategoryId = categoryId;
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
    this.newItemCurrency = DEFAULT_CURRENCY;
    this.newItemExchangeRate = null;
    this.newItemPreferredVendor = '';
    this.newItemContractNumber = '';
    this.newItemContractStartDate = '';
    this.newItemContractEndDate = '';
    this.newItemProcurementCompleted = false;
    this.newItemProcurementCompletedDate = '';
    this.newItemCategoryId = null;
  }

  createProcurementItem(): void {
    if (!this.selectedRC || !this.selectedFY || !this.newItemPR.trim() || !this.newItemName.trim()) {
      return;
    }

    this.isCreatingItem = true;
    this.clearMessages();

    const request: ProcurementItemCreateRequest = {
      purchaseRequisition: this.newItemPR.trim(),
      purchaseOrder: this.newItemPO.trim() || undefined,
      name: this.newItemName.trim(),
      description: this.newItemDescription.trim() || undefined,
      currency: this.newItemCurrency,
      exchangeRate: this.newItemCurrency !== 'CAD' ? this.newItemExchangeRate : undefined,
      preferredVendor: this.newItemPreferredVendor.trim() || undefined,
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
    this.editItemPR = item.purchaseRequisition;
    this.editItemPO = item.purchaseOrder || '';
    this.editItemName = item.name;
    this.editItemDescription = item.description || '';
    this.editItemCurrency = item.currency || DEFAULT_CURRENCY;
    this.editItemExchangeRate = item.exchangeRate || null;
    this.editItemPreferredVendor = item.preferredVendor || '';
    this.editItemContractNumber = item.contractNumber || '';
    this.editItemContractStartDate = item.contractStartDate ? item.contractStartDate.split('T')[0] : '';
    this.editItemContractEndDate = item.contractEndDate ? item.contractEndDate.split('T')[0] : '';
    this.editItemProcurementCompleted = item.procurementCompleted || false;
    this.editItemProcurementCompletedDate = item.procurementCompletedDate ? item.procurementCompletedDate.split('T')[0] : '';
    this.editItemCategoryId = item.categoryId || null;
    this.editItemStatus = item.status;
    
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
    this.editItemCurrency = DEFAULT_CURRENCY;
    this.editItemExchangeRate = null;
    this.editItemPreferredVendor = '';
    this.editItemContractNumber = '';
    this.editItemContractStartDate = '';
    this.editItemContractEndDate = '';
    this.editItemProcurementCompleted = false;
    this.editItemProcurementCompletedDate = '';
    this.editItemCategoryId = null;
    this.editItemStatus = 'DRAFT';
  }

  /**
   * Update a procurement item.
   */
  updateProcurementItem(): void {
    if (!this.selectedRC || !this.selectedFY || !this.editingItemId) {
      return;
    }

    if (!this.editItemPR?.trim()) {
      this.showError('Purchase Requisition (PR) is required.');
      return;
    }

    if (!this.editItemName?.trim()) {
      this.showError('Item name is required.');
      return;
    }

    this.isUpdatingItem = true;

    const updateRequest: Partial<ProcurementItemCreateRequest> = {
      purchaseRequisition: this.editItemPR.trim(),
      purchaseOrder: this.editItemPO?.trim() || undefined,
      name: this.editItemName.trim(),
      description: this.editItemDescription?.trim() || undefined,
      currency: this.editItemCurrency,
      exchangeRate: this.editItemCurrency !== 'CAD' ? this.editItemExchangeRate : undefined,
      preferredVendor: this.editItemPreferredVendor?.trim() || undefined,
      contractNumber: this.editItemContractNumber?.trim() || undefined,
      contractStartDate: this.editItemContractStartDate || undefined,
      contractEndDate: this.editItemContractEndDate || undefined,
      procurementCompleted: this.editItemProcurementCompleted,
      procurementCompletedDate: this.editItemProcurementCompleted && this.editItemProcurementCompletedDate 
        ? this.editItemProcurementCompletedDate 
        : undefined,
      categoryId: this.editItemCategoryId || undefined,
      status: this.editItemStatus
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
    this.newQuoteCurrency = DEFAULT_CURRENCY;
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
      currency: this.newQuoteCurrency,
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
    this.newEventType = 'NOTE_ADDED';
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

  // ==========================
  // Helper Methods
  // ==========================

  getStatusLabel(status: ProcurementItemStatus): string {
    return PROCUREMENT_STATUS_INFO[status]?.label || status;
  }

  getStatusClass(status: ProcurementItemStatus): string {
    return `status-${PROCUREMENT_STATUS_INFO[status]?.color || 'gray'}`;
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
}
