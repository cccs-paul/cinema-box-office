/*
 * myRC - Training Component
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/user.model';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ResponsibilityCentreService } from '../../services/responsibility-centre.service';
import { FiscalYearService } from '../../services/fiscal-year.service';
import { TrainingItemService, TrainingItemCreateRequest, TrainingItemUpdateRequest } from '../../services/training-item.service';
import { MoneyService } from '../../services/money.service';
import { CurrencyService } from '../../services/currency.service';
import { FuzzySearchService } from '../../services/fuzzy-search.service';
import { ResponsibilityCentreDTO } from '../../models/responsibility-centre.model';
import { FiscalYear } from '../../models/fiscal-year.model';
import { TrainingItem, TrainingMoneyAllocation, TrainingItemStatus, TrainingType, TRAINING_STATUS_INFO, TRAINING_TYPE_INFO } from '../../models/training-item.model';
import { Money } from '../../models/money.model';
import { Currency, DEFAULT_CURRENCY, getCurrencyFlag } from '../../models/currency.model';

/**
 * Training component for managing training items within a fiscal year.
 * Training items only use O&M money allocations.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-16
 */
@Component({
  selector: 'app-training',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './training.component.html',
  styleUrls: ['./training.component.scss'],
})
export class TrainingComponent implements OnInit, OnDestroy {
  currentUser: User | null = null;

  // Selected RC and FY
  selectedRC: ResponsibilityCentreDTO | null = null;
  selectedFY: FiscalYear | null = null;

  // Training Items
  trainingItems: TrainingItem[] = [];
  isLoadingItems = false;

  // Currencies
  currencies: Currency[] = [];
  isLoadingCurrencies = false;

  // Monies
  monies: Money[] = [];
  isLoadingMonies = false;

  // Search filter
  searchTerm = '';
  filtersExpanded = false;
  selectedStatusFilter: TrainingItemStatus | null = null;
  selectedTypeFilter: TrainingType | null = null;

  // Summary Section
  summaryExpanded = false;

  // Create Form
  showCreateForm = false;
  isCreating = false;
  newItemName = '';
  newItemDescription = '';
  newItemProvider = '';
  newItemReferenceNumber = '';
  newItemEstimatedCost: number | null = null;
  newItemActualCost: number | null = null;
  newItemStatus: TrainingItemStatus = 'PLANNED';
  newItemTrainingType: TrainingType = 'COURSE';
  newItemCurrency = DEFAULT_CURRENCY;
  newItemExchangeRate: number | null = null;
  newItemStartDate = '';
  newItemEndDate = '';
  newItemLocation = '';
  newItemEmployeeName = '';
  newItemNumberOfParticipants = 1;
  newItemMoneyAllocations: TrainingMoneyAllocation[] = [];

  // Expandable item tracking
  expandedItemId: number | null = null;

  // Edit Form
  editingItemId: number | null = null;
  isUpdating = false;
  editItemName = '';
  editItemDescription = '';
  editItemProvider = '';
  editItemReferenceNumber = '';
  editItemEstimatedCost: number | null = null;
  editItemActualCost: number | null = null;
  editItemStatus: TrainingItemStatus = 'PLANNED';
  editItemTrainingType: TrainingType = 'COURSE';
  editItemCurrency = DEFAULT_CURRENCY;
  editItemExchangeRate: number | null = null;
  editItemStartDate = '';
  editItemEndDate = '';
  editItemLocation = '';
  editItemEmployeeName = '';
  editItemNumberOfParticipants = 1;
  editItemMoneyAllocations: TrainingMoneyAllocation[] = [];

  // Messages
  errorMessage: string | null = null;
  successMessage: string | null = null;

  // Status and type lists for dropdowns
  statusOptions: TrainingItemStatus[] = ['PLANNED', 'APPROVED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'];
  trainingTypeOptions: TrainingType[] = ['COURSE', 'CONFERENCE', 'CERTIFICATION', 'WORKSHOP', 'SEMINAR', 'ONLINE', 'OTHER'];

  // Summary data
  summaryByMoneyType: { moneyCode: string; moneyName: string; totalOm: number }[] = [];
  grandTotalOm = 0;
  grandTotalEstimated = 0;
  grandTotalActual = 0;
  totalItems = 0;
  totalParticipants = 0;

  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private router: Router,
    private rcService: ResponsibilityCentreService,
    private fyService: FiscalYearService,
    private trainingItemService: TrainingItemService,
    private moneyService: MoneyService,
    private currencyService: CurrencyService,
    private fuzzySearchService: FuzzySearchService,
    private translate: TranslateService
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

  // ============================
  // Context Loading
  // ============================

  private loadSelectedContext(): void {
    const rcId = this.rcService.getSelectedRC();
    const fyId = this.rcService.getSelectedFY();

    if (!rcId || !fyId) {
      this.router.navigate(['/rc-selection']);
      return;
    }

    this.rcService.getResponsibilityCentre(rcId).subscribe({
      next: (rc) => {
        this.selectedRC = rc;
        this.fyService.getFiscalYear(rcId, fyId).subscribe({
          next: (fy) => {
            this.selectedFY = fy;
            this.loadTrainingItems();
            this.loadMonies();
          },
          error: () => this.router.navigate(['/rc-selection'])
        });
      },
      error: () => this.router.navigate(['/rc-selection'])
    });
  }

  private loadCurrencies(): void {
    this.isLoadingCurrencies = true;
    this.currencyService.getCurrencies().subscribe({
      next: (currencies: Currency[]) => {
        this.currencies = currencies;
        this.isLoadingCurrencies = false;
      },
      error: () => {
        this.currencies = [{ code: 'CAD', name: 'Canadian Dollar', symbol: '$', isDefault: true }];
        this.isLoadingCurrencies = false;
      }
    });
  }

  private loadMonies(): void {
    if (!this.selectedRC || !this.selectedFY) return;
    this.isLoadingMonies = true;
    this.moneyService.getMoniesByFiscalYear(this.selectedRC.id, this.selectedFY.id).subscribe({
      next: (monies: Money[]) => {
        this.monies = monies;
        this.isLoadingMonies = false;
      },
      error: () => {
        this.isLoadingMonies = false;
      }
    });
  }

  // ============================
  // Load Training Items
  // ============================

  loadTrainingItems(): void {
    if (!this.selectedRC || !this.selectedFY) return;

    this.isLoadingItems = true;
    this.trainingItemService.getTrainingItemsByFY(this.selectedRC.id, this.selectedFY.id).subscribe({
      next: (items) => {
        this.trainingItems = items;
        this.isLoadingItems = false;
        this.calculateSummary();
      },
      error: (error) => {
        this.errorMessage = error.message || 'Failed to load training items';
        this.isLoadingItems = false;
      }
    });
  }

  // ============================
  // Filtered Items
  // ============================

  get filteredItems(): TrainingItem[] {
    let items = this.trainingItems;

    // Status filter
    if (this.selectedStatusFilter) {
      items = items.filter(item => item.status === this.selectedStatusFilter);
    }

    // Type filter
    if (this.selectedTypeFilter) {
      items = items.filter(item => item.trainingType === this.selectedTypeFilter);
    }

    // Text search
    if (this.searchTerm.trim()) {
      items = this.fuzzySearchService.filter(
        items,
        this.searchTerm,
        (item: TrainingItem) => ({
          name: item.name,
          description: item.description,
          provider: item.provider,
          employeeName: item.employeeName,
          location: item.location,
          referenceNumber: item.referenceNumber
        })
      );
    }

    return items;
  }

  clearSearch(): void {
    this.searchTerm = '';
  }

  toggleFilters(): void {
    this.filtersExpanded = !this.filtersExpanded;
  }

  filterByStatus(status: TrainingItemStatus | null): void {
    this.selectedStatusFilter = status;
  }

  filterByType(type: TrainingType | null): void {
    this.selectedTypeFilter = type;
  }

  // ============================
  // Summary Calculations
  // ============================

  private calculateSummary(): void {
    this.grandTotalOm = 0;
    this.grandTotalEstimated = 0;
    this.grandTotalActual = 0;
    this.totalItems = this.trainingItems.length;
    this.totalParticipants = 0;

    const moneyTotals: Record<number, { moneyCode: string; moneyName: string; totalOm: number }> = {};

    for (const item of this.trainingItems) {
      this.grandTotalEstimated += (item.estimatedCostCad ?? item.estimatedCost ?? 0);
      this.grandTotalActual += (item.actualCostCad ?? item.actualCost ?? 0);
      this.totalParticipants += item.numberOfParticipants || 0;

      if (item.moneyAllocations) {
        for (const alloc of item.moneyAllocations) {
          this.grandTotalOm += alloc.omAmount || 0;
          if (!moneyTotals[alloc.moneyId]) {
            moneyTotals[alloc.moneyId] = {
              moneyCode: alloc.moneyCode || '',
              moneyName: alloc.moneyName || '',
              totalOm: 0
            };
          }
          moneyTotals[alloc.moneyId].totalOm += alloc.omAmount || 0;
        }
      }
    }

    this.summaryByMoneyType = Object.values(moneyTotals).sort((a, b) => a.moneyCode.localeCompare(b.moneyCode));
  }

  toggleSummary(): void {
    this.summaryExpanded = !this.summaryExpanded;
  }

  // ============================
  // Create Item
  // ============================

  openCreateForm(): void {
    this.showCreateForm = true;
    this.resetCreateForm();
  }

  cancelCreate(): void {
    this.showCreateForm = false;
    this.resetCreateForm();
  }

  private resetCreateForm(): void {
    this.newItemName = '';
    this.newItemDescription = '';
    this.newItemProvider = '';
    this.newItemReferenceNumber = '';
    this.newItemEstimatedCost = null;
    this.newItemActualCost = null;
    this.newItemStatus = 'PLANNED';
    this.newItemTrainingType = 'COURSE';
    this.newItemCurrency = DEFAULT_CURRENCY;
    this.newItemExchangeRate = null;
    this.newItemStartDate = '';
    this.newItemEndDate = '';
    this.newItemLocation = '';
    this.newItemEmployeeName = '';
    this.newItemNumberOfParticipants = 1;
    this.newItemMoneyAllocations = [];
  }

  createItem(): void {
    if (!this.selectedRC || !this.selectedFY || !this.newItemName.trim()) return;

    this.isCreating = true;
    this.errorMessage = null;

    const request: TrainingItemCreateRequest = {
      name: this.newItemName.trim(),
      description: this.newItemDescription.trim() || undefined,
      provider: this.newItemProvider.trim() || undefined,
      referenceNumber: this.newItemReferenceNumber.trim() || undefined,
      estimatedCost: this.newItemEstimatedCost,
      actualCost: this.newItemActualCost,
      status: this.newItemStatus,
      trainingType: this.newItemTrainingType,
      currency: this.newItemCurrency,
      exchangeRate: this.newItemCurrency !== DEFAULT_CURRENCY ? this.newItemExchangeRate : null,
      startDate: this.newItemStartDate || null,
      endDate: this.newItemEndDate || null,
      location: this.newItemLocation.trim() || undefined,
      employeeName: this.newItemEmployeeName.trim() || undefined,
      numberOfParticipants: this.newItemNumberOfParticipants,
      moneyAllocations: this.newItemMoneyAllocations.length > 0 ? this.newItemMoneyAllocations : undefined
    };

    this.trainingItemService.createTrainingItem(this.selectedRC.id, this.selectedFY.id, request).subscribe({
      next: () => {
        this.successMessage = this.translate.instant('training.itemCreated');
        this.showCreateForm = false;
        this.resetCreateForm();
        this.loadTrainingItems();
        this.isCreating = false;
        this.autoClearSuccess();
      },
      error: (error) => {
        this.errorMessage = error.message || 'Failed to create training item';
        this.isCreating = false;
      }
    });
  }

  // ============================
  // Expand / Collapse Item
  // ============================

  toggleItem(itemId: number): void {
    if (this.expandedItemId === itemId) {
      this.expandedItemId = null;
      this.editingItemId = null;
    } else {
      this.expandedItemId = itemId;
      this.editingItemId = null;
    }
  }

  isExpanded(itemId: number): boolean {
    return this.expandedItemId === itemId;
  }

  // ============================
  // Edit Item
  // ============================

  startEdit(item: TrainingItem): void {
    this.editingItemId = item.id;
    this.editItemName = item.name;
    this.editItemDescription = item.description || '';
    this.editItemProvider = item.provider || '';
    this.editItemReferenceNumber = item.referenceNumber || '';
    this.editItemEstimatedCost = item.estimatedCost;
    this.editItemActualCost = item.actualCost;
    this.editItemStatus = item.status;
    this.editItemTrainingType = item.trainingType;
    this.editItemCurrency = item.currency || DEFAULT_CURRENCY;
    this.editItemExchangeRate = item.exchangeRate;
    this.editItemStartDate = item.startDate || '';
    this.editItemEndDate = item.endDate || '';
    this.editItemLocation = item.location || '';
    this.editItemEmployeeName = item.employeeName || '';
    this.editItemNumberOfParticipants = item.numberOfParticipants || 1;
    this.editItemMoneyAllocations = item.moneyAllocations ? item.moneyAllocations.map(a => ({ ...a })) : [];
  }

  cancelEdit(): void {
    this.editingItemId = null;
  }

  saveEdit(): void {
    if (!this.selectedRC || !this.selectedFY || !this.editingItemId || !this.editItemName.trim()) return;

    this.isUpdating = true;
    this.errorMessage = null;

    const request: TrainingItemUpdateRequest = {
      name: this.editItemName.trim(),
      description: this.editItemDescription.trim() || undefined,
      provider: this.editItemProvider.trim() || undefined,
      referenceNumber: this.editItemReferenceNumber.trim() || undefined,
      estimatedCost: this.editItemEstimatedCost,
      actualCost: this.editItemActualCost,
      status: this.editItemStatus,
      trainingType: this.editItemTrainingType,
      currency: this.editItemCurrency,
      exchangeRate: this.editItemCurrency !== DEFAULT_CURRENCY ? this.editItemExchangeRate : null,
      startDate: this.editItemStartDate || null,
      endDate: this.editItemEndDate || null,
      location: this.editItemLocation.trim() || undefined,
      employeeName: this.editItemEmployeeName.trim() || undefined,
      numberOfParticipants: this.editItemNumberOfParticipants,
      moneyAllocations: this.editItemMoneyAllocations.length > 0 ? this.editItemMoneyAllocations : undefined
    };

    this.trainingItemService.updateTrainingItem(this.selectedRC.id, this.selectedFY.id, this.editingItemId, request).subscribe({
      next: () => {
        this.successMessage = this.translate.instant('training.itemUpdated');
        this.editingItemId = null;
        this.loadTrainingItems();
        this.isUpdating = false;
        this.autoClearSuccess();
      },
      error: (error) => {
        this.errorMessage = error.message || 'Failed to update training item';
        this.isUpdating = false;
      }
    });
  }

  // ============================
  // Delete Item
  // ============================

  deleteItem(item: TrainingItem): void {
    if (!this.selectedRC || !this.selectedFY) return;

    const confirmMsg = this.translate.instant('training.deleteConfirm');
    if (!confirm(confirmMsg)) return;

    this.trainingItemService.deleteTrainingItem(this.selectedRC.id, this.selectedFY.id, item.id).subscribe({
      next: () => {
        this.successMessage = this.translate.instant('training.itemDeleted');
        this.expandedItemId = null;
        this.editingItemId = null;
        this.loadTrainingItems();
        this.autoClearSuccess();
      },
      error: (error) => {
        this.errorMessage = error.message || 'Failed to delete training item';
      }
    });
  }

  // ============================
  // Status Update
  // ============================

  updateStatus(item: TrainingItem, status: TrainingItemStatus): void {
    if (!this.selectedRC || !this.selectedFY) return;

    this.trainingItemService.updateStatus(this.selectedRC.id, this.selectedFY.id, item.id, status).subscribe({
      next: () => {
        this.loadTrainingItems();
        this.autoClearSuccess();
      },
      error: (error) => {
        this.errorMessage = error.message || 'Failed to update status';
      }
    });
  }

  // ============================
  // Money Allocation Management
  // ============================

  addNewAllocation(allocations: TrainingMoneyAllocation[]): void {
    if (this.monies.length === 0) return;
    const defaultMoney = this.monies.find(m => m.isDefault) || this.monies[0];
    allocations.push({
      moneyId: defaultMoney.id,
      moneyName: defaultMoney.name,
      moneyCode: defaultMoney.code,
      isDefault: defaultMoney.isDefault,
      omAmount: 0
    });
  }

  removeAllocation(allocations: TrainingMoneyAllocation[], index: number): void {
    allocations.splice(index, 1);
  }

  onAllocationMoneyChange(alloc: TrainingMoneyAllocation): void {
    const money = this.monies.find(m => m.id === alloc.moneyId);
    if (money) {
      alloc.moneyName = money.name;
      alloc.moneyCode = money.code;
      alloc.isDefault = money.isDefault;
    }
  }

  saveAllocations(item: TrainingItem): void {
    if (!this.selectedRC || !this.selectedFY) return;

    this.trainingItemService.updateMoneyAllocations(
      this.selectedRC.id,
      this.selectedFY.id,
      item.id,
      this.editItemMoneyAllocations
    ).subscribe({
      next: () => {
        this.successMessage = this.translate.instant('training.allocationsUpdated');
        this.loadTrainingItems();
        this.autoClearSuccess();
      },
      error: (error) => {
        this.errorMessage = error.message || 'Failed to update allocations';
      }
    });
  }

  // ============================
  // Display Helpers
  // ============================

  getStatusInfo(status: TrainingItemStatus) {
    return TRAINING_STATUS_INFO[status] || { label: status, color: 'secondary', icon: '❓' };
  }

  getTrainingTypeInfo(type: TrainingType) {
    return TRAINING_TYPE_INFO[type] || { label: type, color: 'gray', icon: '📝' };
  }

  getCurrencyFlag(code: string): string {
    return getCurrencyFlag(code);
  }

  formatCurrency(amount: number | null | undefined, currency?: string): string {
    if (amount === null || amount === undefined) return '—';
    const cur = currency || 'CAD';
    return new Intl.NumberFormat('en-CA', { style: 'currency', currency: cur, minimumFractionDigits: 2 }).format(amount);
  }

  formatDate(date: string | null | undefined): string {
    if (!date) return '—';
    try {
      return new Date(date).toLocaleDateString('en-CA');
    } catch {
      return date;
    }
  }

  // ============================
  // Utility
  // ============================

  private autoClearSuccess(): void {
    setTimeout(() => {
      this.successMessage = null;
    }, 5000);
  }

  dismissError(): void {
    this.errorMessage = null;
  }

  dismissSuccess(): void {
    this.successMessage = null;
  }
}
