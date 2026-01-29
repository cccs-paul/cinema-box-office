/*
 * myRC - Procurement Component
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
import { ProcurementService, ProcurementItemCreateRequest, QuoteCreateRequest } from '../../services/procurement.service';
import { CurrencyService } from '../../services/currency.service';
import { ResponsibilityCentreDTO } from '../../models/responsibility-centre.model';
import { FiscalYear } from '../../models/fiscal-year.model';
import {
  ProcurementItem,
  ProcurementQuote,
  ProcurementQuoteFile,
  ProcurementItemStatus,
  QuoteStatus,
  PROCUREMENT_STATUS_INFO,
  QUOTE_STATUS_INFO
} from '../../models/procurement.model';
import { Currency, DEFAULT_CURRENCY, getCurrencyFlag } from '../../models/currency.model';

/**
 * Procurement component for managing procurement items, quotes, and files.
 *
 * @author myRC Team
 * @version 1.0.0
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

  // Selected Item for detail view
  selectedItem: ProcurementItem | null = null;
  quotes: ProcurementQuote[] = [];
  isLoadingQuotes = false;

  // Selected Quote for file view
  selectedQuote: ProcurementQuote | null = null;
  files: ProcurementQuoteFile[] = [];
  isLoadingFiles = false;

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

  // File Upload
  showFileUpload = false;
  isUploadingFile = false;
  selectedFile: File | null = null;
  fileDescription = '';

  // Messages
  errorMessage: string | null = null;
  successMessage: string | null = null;

  // Status options
  statusOptions: ProcurementItemStatus[] = [
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
    private currencyService: CurrencyService
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

  onSearch(): void {
    this.loadProcurementItems();
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
      exchangeRate: this.newItemCurrency !== 'CAD' ? this.newItemExchangeRate : undefined
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
      },
      error: (error) => {
        this.showError('Failed to delete procurement item: ' + error.message);
      }
    });
  }

  viewItemDetails(item: ProcurementItem): void {
    this.selectedItem = item;
    this.selectedQuote = null;
    this.files = [];
    this.loadQuotes();
  }

  closeItemDetails(): void {
    this.selectedItem = null;
    this.quotes = [];
    this.selectedQuote = null;
    this.files = [];
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
      next: () => {
        this.showSuccess('Quote added successfully');
        this.showCreateQuoteForm = false;
        this.resetCreateQuoteForm();
        this.loadQuotes();
        this.isCreatingQuote = false;
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

    const url = `/api${this.procurementService.getFileViewUrl(
      this.selectedRC.id,
      this.selectedFY.id,
      this.selectedItem.id,
      this.selectedQuote.id,
      file.id
    )}`;
    window.open(url, '_blank');
  }

  downloadFile(file: ProcurementQuoteFile): void {
    if (!this.selectedRC || !this.selectedFY || !this.selectedItem || !this.selectedQuote) return;

    const url = `/api${this.procurementService.getFileDownloadUrl(
      this.selectedRC.id,
      this.selectedFY.id,
      this.selectedItem.id,
      this.selectedQuote.id,
      file.id
    )}`;
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
