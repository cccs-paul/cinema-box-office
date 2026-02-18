/*
 * myRC - Travel Component
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
import { TravelItemService, TravelItemCreateRequest, TravelItemUpdateRequest } from '../../services/travel-item.service';
import { MoneyService } from '../../services/money.service';
import { CurrencyService } from '../../services/currency.service';
import { FuzzySearchService } from '../../services/fuzzy-search.service';
import { ResponsibilityCentreDTO } from '../../models/responsibility-centre.model';
import { FiscalYear } from '../../models/fiscal-year.model';
import { TravelItem, TravelMoneyAllocation, TravelItemStatus, TravelType, TravelTraveller, TravelApprovalStatus, TRAVEL_STATUS_INFO, TRAVEL_TYPE_INFO, TRAVEL_APPROVAL_STATUS_INFO } from '../../models/travel-item.model';
import { Money } from '../../models/money.model';
import { Currency, DEFAULT_CURRENCY, getCurrencyFlag } from '../../models/currency.model';

/**
 * Travel component for managing travel items within a fiscal year.
 * Travel items only use O&M money allocations.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-16
 */
@Component({
  selector: 'app-travel',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './travel.component.html',
  styleUrls: ['./travel.component.scss'],
})
export class TravelComponent implements OnInit, OnDestroy {
  currentUser: User | null = null;

  // Selected RC and FY
  selectedRC: ResponsibilityCentreDTO | null = null;
  selectedFY: FiscalYear | null = null;

  // Travel Items
  travelItems: TravelItem[] = [];
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
  selectedStatusFilter: TravelItemStatus | null = null;
  selectedTypeFilter: TravelType | null = null;

  // Summary Section
  summaryExpanded = false;

  // Create Form
  showCreateForm = false;
  isCreating = false;
  newItemName = '';
  newItemDescription = '';
  newItemEmap = '';
  newItemDestination = '';
  newItemPurpose = '';
  newItemStatus: TravelItemStatus = 'PLANNED';
  newItemTravelType: TravelType = 'DOMESTIC';
  newItemDepartureDate = '';
  newItemReturnDate = '';
  newItemMoneyAllocations: TravelMoneyAllocation[] = [];
  newItemTravellers: TravelTraveller[] = [];

  // Expandable item tracking
  expandedItemId: number | null = null;

  // Edit Form
  editingItemId: number | null = null;
  isUpdating = false;
  editItemName = '';
  editItemDescription = '';
  editItemEmap = '';
  editItemDestination = '';
  editItemPurpose = '';
  editItemStatus: TravelItemStatus = 'PLANNED';
  editItemTravelType: TravelType = 'DOMESTIC';
  editItemDepartureDate = '';
  editItemReturnDate = '';
  editItemMoneyAllocations: TravelMoneyAllocation[] = [];

  // Traveller management
  editingTravellers: TravelTraveller[] = [];
  isAddingTraveller = false;
  isSavingTraveller = false;

  // Messages
  errorMessage: string | null = null;
  successMessage: string | null = null;

  // Status and type lists for dropdowns
  statusOptions: TravelItemStatus[] = ['PLANNED', 'APPROVED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'];
  travelTypeOptions: TravelType[] = ['DOMESTIC', 'NORTH_AMERICA', 'INTERNATIONAL', 'LOCAL'];
  approvalStatusOptions: TravelApprovalStatus[] = ['PLANNED', 'TAAC_ESTIMATE_SUBMITTED', 'TAAC_ESTIMATE_APPROVED', 'TAAC_FINAL_SUBMITTED', 'TAAC_FINAL_APPROVED', 'CANCELLED'];

  // Summary data
  summaryByMoneyType: { moneyCode: string; moneyName: string; totalOm: number }[] = [];
  grandTotalOm = 0;
  grandTotalEstimated = 0;
  grandTotalActual = 0;
  totalItems = 0;
  totalTravellers = 0;

  get canWrite(): boolean {
    return this.selectedRC?.accessLevel === 'OWNER' || this.selectedRC?.accessLevel === 'READ_WRITE';
  }

  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private router: Router,
    private rcService: ResponsibilityCentreService,
    private fyService: FiscalYearService,
    private travelItemService: TravelItemService,
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
            this.loadTravelItems();
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
  // Load Travel Items
  // ============================

  loadTravelItems(): void {
    if (!this.selectedRC || !this.selectedFY) return;

    this.isLoadingItems = true;
    this.travelItemService.getTravelItemsByFY(this.selectedRC.id, this.selectedFY.id).subscribe({
      next: (items) => {
        this.travelItems = items;
        this.isLoadingItems = false;
        this.calculateSummary();
      },
      error: (error) => {
        this.errorMessage = error.message || 'Failed to load travel items';
        this.isLoadingItems = false;
      }
    });
  }

  // ============================
  // Filtered Items
  // ============================

  get filteredItems(): TravelItem[] {
    let items = this.travelItems;

    // Status filter
    if (this.selectedStatusFilter) {
      items = items.filter(item => item.status === this.selectedStatusFilter);
    }

    // Type filter
    if (this.selectedTypeFilter) {
      items = items.filter(item => item.travelType === this.selectedTypeFilter);
    }

    // Text search
    if (this.searchTerm.trim()) {
      items = this.fuzzySearchService.filter(
        items,
        this.searchTerm,
        (item: TravelItem) => ({
          name: item.name,
          description: item.description,
          destination: item.destination,
          emap: item.emap,
          purpose: item.purpose
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

  filterByStatus(status: TravelItemStatus | null): void {
    this.selectedStatusFilter = status;
  }

  filterByType(type: TravelType | null): void {
    this.selectedTypeFilter = type;
  }

  // ============================
  // Summary Calculations
  // ============================

  private calculateSummary(): void {
    this.grandTotalOm = 0;
    this.grandTotalEstimated = 0;
    this.grandTotalActual = 0;
    this.totalItems = this.travelItems.length;
    this.totalTravellers = 0;

    const moneyTotals: Record<number, { moneyCode: string; moneyName: string; totalOm: number }> = {};

    for (const item of this.travelItems) {
      this.grandTotalEstimated += (item.estimatedCostCad ?? 0);
      this.grandTotalActual += (item.actualCostCad ?? 0);
      this.totalTravellers += item.numberOfTravellers || 0;

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
    this.newItemEmap = '';
    this.newItemDestination = '';
    this.newItemPurpose = '';
    this.newItemStatus = 'PLANNED';
    this.newItemTravelType = 'DOMESTIC';
    this.newItemDepartureDate = '';
    this.newItemReturnDate = '';
    this.newItemMoneyAllocations = [];
    this.newItemTravellers = [];
  }

  createItem(): void {
    if (!this.selectedRC || !this.selectedFY || !this.newItemName.trim()) return;

    this.isCreating = true;
    this.errorMessage = null;

    const request: TravelItemCreateRequest = {
      name: this.newItemName.trim(),
      description: this.newItemDescription.trim() || undefined,
      emap: this.newItemEmap.trim() || undefined,
      destination: this.newItemDestination.trim() || undefined,
      purpose: this.newItemPurpose.trim() || undefined,
      status: this.newItemStatus,
      travelType: this.newItemTravelType,
      departureDate: this.newItemDepartureDate || null,
      returnDate: this.newItemReturnDate || null,
      travellers: this.newItemTravellers.length > 0 ? this.newItemTravellers : undefined,
      moneyAllocations: this.newItemMoneyAllocations.length > 0 ? this.newItemMoneyAllocations : undefined
    };

    this.travelItemService.createTravelItem(this.selectedRC.id, this.selectedFY.id, request).subscribe({
      next: () => {
        this.successMessage = this.translate.instant('travel.itemCreated');
        this.showCreateForm = false;
        this.resetCreateForm();
        this.loadTravelItems();
        this.isCreating = false;
        this.autoClearSuccess();
      },
      error: (error) => {
        this.errorMessage = error.message || 'Failed to create travel item';
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

  startEdit(item: TravelItem): void {
    this.editingItemId = item.id;
    this.editItemName = item.name;
    this.editItemDescription = item.description || '';
    this.editItemEmap = item.emap || '';
    this.editItemDestination = item.destination || '';
    this.editItemPurpose = item.purpose || '';
    this.editItemStatus = item.status;
    this.editItemTravelType = item.travelType;
    this.editItemDepartureDate = item.departureDate || '';
    this.editItemReturnDate = item.returnDate || '';
    this.editItemMoneyAllocations = item.moneyAllocations ? item.moneyAllocations.map(a => ({ ...a })) : [];
    this.editingTravellers = item.travellers ? item.travellers.map(t => ({ ...t })) : [];
  }

  cancelEdit(): void {
    this.editingItemId = null;
  }

  saveEdit(): void {
    if (!this.selectedRC || !this.selectedFY || !this.editingItemId || !this.editItemName.trim()) return;

    this.isUpdating = true;
    this.errorMessage = null;

    const request: TravelItemUpdateRequest = {
      name: this.editItemName.trim(),
      description: this.editItemDescription.trim() || undefined,
      emap: this.editItemEmap.trim() || undefined,
      destination: this.editItemDestination.trim() || undefined,
      purpose: this.editItemPurpose.trim() || undefined,
      status: this.editItemStatus,
      travelType: this.editItemTravelType,
      departureDate: this.editItemDepartureDate || null,
      returnDate: this.editItemReturnDate || null,
      moneyAllocations: this.editItemMoneyAllocations.length > 0 ? this.editItemMoneyAllocations : undefined
    };

    this.travelItemService.updateTravelItem(this.selectedRC.id, this.selectedFY.id, this.editingItemId, request).subscribe({
      next: () => {
        this.successMessage = this.translate.instant('travel.itemUpdated');
        this.editingItemId = null;
        this.loadTravelItems();
        this.isUpdating = false;
        this.autoClearSuccess();
      },
      error: (error) => {
        this.errorMessage = error.message || 'Failed to update travel item';
        this.isUpdating = false;
      }
    });
  }

  // ============================
  // Delete Item
  // ============================

  deleteItem(item: TravelItem): void {
    if (!this.selectedRC || !this.selectedFY) return;

    const confirmMsg = this.translate.instant('travel.deleteConfirm');
    if (!confirm(confirmMsg)) return;

    this.travelItemService.deleteTravelItem(this.selectedRC.id, this.selectedFY.id, item.id).subscribe({
      next: () => {
        this.successMessage = this.translate.instant('travel.itemDeleted');
        this.expandedItemId = null;
        this.editingItemId = null;
        this.loadTravelItems();
        this.autoClearSuccess();
      },
      error: (error) => {
        this.errorMessage = error.message || 'Failed to delete travel item';
      }
    });
  }

  // ============================
  // Status Update
  // ============================

  updateStatus(item: TravelItem, status: TravelItemStatus): void {
    if (!this.selectedRC || !this.selectedFY) return;

    this.travelItemService.updateStatus(this.selectedRC.id, this.selectedFY.id, item.id, status).subscribe({
      next: () => {
        this.loadTravelItems();
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

  addNewAllocation(allocations: TravelMoneyAllocation[]): void {
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

  removeAllocation(allocations: TravelMoneyAllocation[], index: number): void {
    allocations.splice(index, 1);
  }

  onAllocationMoneyChange(alloc: TravelMoneyAllocation): void {
    const money = this.monies.find(m => m.id === alloc.moneyId);
    if (money) {
      alloc.moneyName = money.name;
      alloc.moneyCode = money.code;
      alloc.isDefault = money.isDefault;
    }
  }

  saveAllocations(item: TravelItem): void {
    if (!this.selectedRC || !this.selectedFY) return;

    this.travelItemService.updateMoneyAllocations(
      this.selectedRC.id,
      this.selectedFY.id,
      item.id,
      this.editItemMoneyAllocations
    ).subscribe({
      next: () => {
        this.successMessage = this.translate.instant('travel.allocationsUpdated');
        this.loadTravelItems();
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

  getStatusInfo(status: TravelItemStatus) {
    return TRAVEL_STATUS_INFO[status] || { label: status, color: 'secondary', icon: '❓' };
  }

  getTravelTypeInfo(type: TravelType) {
    return TRAVEL_TYPE_INFO[type] || { label: type, color: 'gray', icon: '📝' };
  }

  getApprovalStatusInfo(status: TravelApprovalStatus) {
    return TRAVEL_APPROVAL_STATUS_INFO[status] || { label: status, color: 'secondary', icon: '❓' };
  }

  getCurrencyFlag(code: string): string {
    return getCurrencyFlag(code);
  }

  /**
   * Get budget mismatch warnings for a travel item.
   * Warns when money allocation total doesn't match estimated or final cost totals.
   */
  getItemWarnings(item: TravelItem): string[] {
    const warnings: string[] = [];
    const allocTotal = item.moneyAllocationTotalOm ?? 0;
    const estTotal = item.estimatedCostCad ?? 0;
    const actTotal = item.actualCostCad ?? 0;

    if (allocTotal > 0 && estTotal > 0 && Math.abs(allocTotal - estTotal) > 0.01) {
      warnings.push(this.translate.instant('travel.budgetMismatchEstimated', {
        allocation: this.formatCurrency(allocTotal),
        estimated: this.formatCurrency(estTotal)
      }));
    }
    if (allocTotal > 0 && actTotal > 0 && Math.abs(allocTotal - actTotal) > 0.01) {
      warnings.push(this.translate.instant('travel.budgetMismatchActual', {
        allocation: this.formatCurrency(allocTotal),
        actual: this.formatCurrency(actTotal)
      }));
    }
    return warnings;
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
  // Traveller Management (Create Form)
  // ============================

  addNewTraveller(travellers: TravelTraveller[]): void {
    travellers.push({
      name: '',
      taac: '',
      estimatedCost: null,
      finalCost: null,
      estimatedCurrency: DEFAULT_CURRENCY,
      estimatedExchangeRate: null,
      finalCurrency: DEFAULT_CURRENCY,
      finalExchangeRate: null,
      approvalStatus: 'PLANNED'
    });
  }

  removeNewTraveller(travellers: TravelTraveller[], index: number): void {
    travellers.splice(index, 1);
  }

  // ============================
  // Traveller Management (Edit/Existing Item)
  // ============================

  addTraveller(item: TravelItem): void {
    if (!this.selectedRC || !this.selectedFY) return;
    this.isAddingTraveller = true;

    const traveller: TravelTraveller = {
      name: '',
      taac: '',
      estimatedCost: null,
      finalCost: null,
      estimatedCurrency: DEFAULT_CURRENCY,
      estimatedExchangeRate: null,
      finalCurrency: DEFAULT_CURRENCY,
      finalExchangeRate: null,
      approvalStatus: 'PLANNED'
    };

    this.travelItemService.addTraveller(this.selectedRC.id, this.selectedFY.id, item.id, traveller).subscribe({
      next: (created) => {
        this.editingTravellers.push(created);
        this.isAddingTraveller = false;
        this.loadTravelItems();
      },
      error: (error) => {
        this.errorMessage = error.message || 'Failed to add traveller';
        this.isAddingTraveller = false;
      }
    });
  }

  saveTraveller(item: TravelItem, traveller: TravelTraveller): void {
    if (!this.selectedRC || !this.selectedFY || !traveller.id) return;
    this.isSavingTraveller = true;

    this.travelItemService.updateTraveller(this.selectedRC.id, this.selectedFY.id, item.id, traveller.id, traveller).subscribe({
      next: (updated) => {
        const idx = this.editingTravellers.findIndex(t => t.id === updated.id);
        if (idx >= 0) this.editingTravellers[idx] = updated;
        this.isSavingTraveller = false;
        this.loadTravelItems();
      },
      error: (error) => {
        this.errorMessage = error.message || 'Failed to update traveller';
        this.isSavingTraveller = false;
      }
    });
  }

  deleteTraveller(item: TravelItem, traveller: TravelTraveller): void {
    if (!this.selectedRC || !this.selectedFY || !traveller.id) return;

    const confirmMsg = this.translate.instant('travel.deleteTravellerConfirm');
    if (!confirm(confirmMsg)) return;

    this.travelItemService.deleteTraveller(this.selectedRC.id, this.selectedFY.id, item.id, traveller.id).subscribe({
      next: () => {
        this.editingTravellers = this.editingTravellers.filter(t => t.id !== traveller.id);
        this.loadTravelItems();
      },
      error: (error) => {
        this.errorMessage = error.message || 'Failed to delete traveller';
      }
    });
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
