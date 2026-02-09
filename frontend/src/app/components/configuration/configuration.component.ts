/*
 * myRC - Configuration Component
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, combineLatest } from 'rxjs';
import { takeUntil, filter, switchMap } from 'rxjs/operators';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ResponsibilityCentreService } from '../../services/responsibility-centre.service';
import { FiscalYearService } from '../../services/fiscal-year.service';
import { MoneyService } from '../../services/money.service';
import { CategoryService, CategoryCreateRequest, CategoryUpdateRequest } from '../../services/category.service';
import { Money, MoneyCreateRequest, MoneyUpdateRequest } from '../../models/money.model';
import { Category, FundingType } from '../../models/category.model';
import { ResponsibilityCentreDTO } from '../../models/responsibility-centre.model';
import { FiscalYear } from '../../models/fiscal-year.model';

/**
 * Configuration component for managing RC settings.
 * Supports Money type configuration and Spending Categories.
 * Accessible from the sidebar "Configuration" menu item.
 *
 * @author myRC Team
 * @version 1.1.0
 * @since 2026-01-24
 */
@Component({
  selector: 'app-configuration',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './configuration.component.html',
  styleUrls: ['./configuration.component.scss'],
})
export class ConfigurationComponent implements OnInit, OnDestroy {
  // Current context
  selectedRC: ResponsibilityCentreDTO | null = null;
  selectedFY: FiscalYear | null = null;
  rcId: number | null = null;
  fyId: number | null = null;

  // Configuration tabs
  activeTab: 'monies' | 'categories' | 'summary' | 'import-export' = 'monies';

  // Money management state
  monies: Money[] = [];
  isLoadingMonies = false;
  moneyError: string | null = null;

  // Money form state
  isAddingMoney = false;
  editingMoneyId: number | null = null;
  newMoney: MoneyCreateRequest = { code: '', name: '', description: '' };
  editMoney: MoneyUpdateRequest = { code: '', name: '', description: '' };

  // Category management state
  categories: Category[] = [];
  isLoadingCategories = false;
  categoryError: string | null = null;

  // Category form state
  isAddingCategory = false;
  editingCategoryId: number | null = null;
  newCategory: CategoryCreateRequest = { name: '', description: '', fundingType: 'BOTH' };
  editCategory: CategoryUpdateRequest = { name: '', description: '', fundingType: 'BOTH' };

  // Funding type options for dropdown
  fundingTypeOptions: FundingType[] = ['BOTH', 'CAP_ONLY', 'OM_ONLY'];

  // Operation state
  isSaving = false;
  isDeleting = false;
  successMessage: string | null = null;

  // On Target slider values for real-time display
  onTargetMinValue: number | null = null;
  onTargetMaxValue: number | null = null;

  // Import/Export state
  exportPath = '';
  exportFileHandle: any = null;
  isExporting = false;
  exportSuccessMessage: string | null = null;
  exportErrorMessage: string | null = null;

  private destroy$ = new Subject<void>();

  /**
   * Checks whether the fiscal year is inactive (making it read-only
   * regardless of user role).
   */
  get isInactiveFY(): boolean {
    return this.selectedFY != null && !this.selectedFY.active;
  }

  /**
   * Checks whether the current user lacks owner access to the RC.
   * Money types and categories are configuration-level settings that require
   * OWNER access. Both READ_ONLY and READ_WRITE users are restricted.
   */
  get isNotOwner(): boolean {
    return this.selectedRC?.accessLevel !== 'OWNER';
  }

  /**
   * Checks whether editing is disabled — either because the user is not
   * an owner or because the fiscal year is inactive.
   * When true, editing operations (add/edit/delete money types and categories,
   * changing on-target settings) should be disabled in the UI.
   */
  get isReadOnly(): boolean {
    return this.isInactiveFY || this.isNotOwner;
  }

  constructor(
    private rcService: ResponsibilityCentreService,
    private fyService: FiscalYearService,
    private moneyService: MoneyService,
    private categoryService: CategoryService,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    // Subscribe to RC and FY changes
    combineLatest([this.rcService.selectedRC$, this.rcService.selectedFY$])
      .pipe(
        takeUntil(this.destroy$),
        filter(([rcId, fyId]) => rcId !== null && fyId !== null)
      )
      .subscribe(([rcId, fyId]) => {
        this.rcId = rcId;
        this.fyId = fyId;
        this.loadContext();
        this.loadMonies();
        this.loadCategories();
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Load RC and FY context information.
   */
  private loadContext(): void {
    if (this.rcId) {
      this.rcService.getResponsibilityCentre(this.rcId).subscribe({
        next: (rc) => {
          this.selectedRC = rc;
        },
        error: () => {
          this.selectedRC = null;
        }
      });
    }

    if (this.rcId && this.fyId) {
      this.fyService.getFiscalYear(this.rcId, this.fyId).subscribe({
        next: (fy) => {
          this.selectedFY = fy;
        },
        error: () => {
          this.selectedFY = null;
        }
      });
    }
  }

  /**
   * Load money types for the current fiscal year.
   */
  loadMonies(): void {
    if (!this.rcId || !this.fyId) {
      return;
    }

    this.isLoadingMonies = true;
    this.moneyError = null;

    this.moneyService.getMoniesByFiscalYear(this.rcId, this.fyId).subscribe({
      next: (monies) => {
        this.monies = monies;
        this.isLoadingMonies = false;
      },
      error: (error) => {
        this.moneyError = error.message || 'Failed to load money types';
        this.isLoadingMonies = false;
      }
    });
  }

  /**
   * Switch to a configuration tab.
   */
  setActiveTab(tab: 'monies' | 'categories' | 'summary' | 'import-export'): void {
    this.activeTab = tab;
  }

  /**
   * Start adding a new money type.
   */
  startAddMoney(): void {
    this.isAddingMoney = true;
    this.newMoney = { code: '', name: '', description: '' };
    this.editingMoneyId = null;
  }

  /**
   * Cancel adding a new money type.
   */
  cancelAddMoney(): void {
    this.isAddingMoney = false;
    this.newMoney = { code: '', name: '', description: '' };
  }

  /**
   * Save a new money type.
   */
  saveMoney(): void {
    if (!this.rcId || !this.fyId) {
      return;
    }

    if (!this.newMoney.code?.trim() || !this.newMoney.name?.trim()) {
      this.moneyError = 'Code and name are required';
      return;
    }

    this.isSaving = true;
    this.moneyError = null;

    this.moneyService.createMoney(this.rcId, this.fyId, this.newMoney).subscribe({
      next: (created) => {
        this.monies.push(created);
        this.monies.sort((a, b) => a.displayOrder - b.displayOrder || a.code.localeCompare(b.code));
        this.isAddingMoney = false;
        this.newMoney = { code: '', name: '', description: '' };
        this.isSaving = false;
        this.showSuccess(`Money type "${created.code}" created successfully`);
      },
      error: (error) => {
        this.moneyError = error.message || 'Failed to create money type';
        this.isSaving = false;
      }
    });
  }

  /**
   * Start editing a money type.
   */
  startEditMoney(money: Money): void {
    this.editingMoneyId = money.id;
    this.editMoney = {
      code: money.code,
      name: money.name,
      description: money.description || ''
    };
    this.isAddingMoney = false;
  }

  /**
   * Cancel editing a money type.
   */
  cancelEditMoney(): void {
    this.editingMoneyId = null;
    this.editMoney = { code: '', name: '', description: '' };
  }

  /**
   * Update a money type.
   */
  updateMoney(money: Money): void {
    if (!this.rcId || !this.fyId) {
      return;
    }

    if (!this.editMoney.name?.trim()) {
      this.moneyError = 'Name is required';
      return;
    }

    this.isSaving = true;
    this.moneyError = null;

    this.moneyService.updateMoney(this.rcId, this.fyId, money.id, this.editMoney).subscribe({
      next: (updated) => {
        const index = this.monies.findIndex(m => m.id === money.id);
        if (index !== -1) {
          this.monies[index] = updated;
        }
        this.editingMoneyId = null;
        this.editMoney = { code: '', name: '', description: '' };
        this.isSaving = false;
        this.showSuccess(`Money type "${updated.code}" updated successfully`);
      },
      error: (error) => {
        this.moneyError = error.message || 'Failed to update money type';
        this.isSaving = false;
      }
    });
  }

  /**
   * Delete a money type.
   */
  deleteMoney(money: Money): void {
    if (!this.rcId || !this.fyId) {
      return;
    }

    if (money.isDefault) {
      this.moneyError = 'Cannot delete the default money type (AB)';
      return;
    }

    if (money.canDelete === false) {
      this.moneyError = 'Cannot delete this money type because it has non-zero funding or spending allocations';
      return;
    }

    if (!confirm(`Are you sure you want to delete the money type "${money.code}"?`)) {
      return;
    }

    this.isDeleting = true;
    this.moneyError = null;

    this.moneyService.deleteMoney(this.rcId, this.fyId, money.id).subscribe({
      next: () => {
        this.monies = this.monies.filter(m => m.id !== money.id);
        this.isDeleting = false;
        this.showSuccess(`Money type "${money.code}" deleted successfully`);
      },
      error: (error) => {
        this.moneyError = error.message || 'Failed to delete money type';
        this.isDeleting = false;
      }
    });
  }

  /**
   * Show a success message temporarily.
   */
  private showSuccess(message: string): void {
    this.successMessage = message;
    setTimeout(() => {
      this.successMessage = null;
    }, 3000);
  }

  /**
   * Clear error message.
   */
  clearError(): void {
    this.moneyError = null;
  }

  /**
   * Track function for ngFor optimization.
   */
  trackByMoneyId(index: number, money: Money): number {
    return money.id;
  }

  // =====================
  // Categories
  // =====================

  /**
   * Load categories for the current fiscal year.
   */
  loadCategories(): void {
    if (!this.rcId || !this.fyId) {
      return;
    }

    this.isLoadingCategories = true;
    this.categoryError = null;

    this.categoryService.getCategoriesByFY(this.rcId, this.fyId).subscribe({
      next: (categories) => {
        this.categories = categories;
        this.isLoadingCategories = false;
      },
      error: (error) => {
        this.categoryError = error.message || 'Failed to load categories';
        this.isLoadingCategories = false;
      }
    });
  }

  /**
   * Start adding a new category.
   */
  startAddCategory(): void {
    this.isAddingCategory = true;
    this.newCategory = { name: '', description: '', fundingType: 'BOTH' };
    this.editingCategoryId = null;
  }

  /**
   * Cancel adding a new category.
   */
  cancelAddCategory(): void {
    this.isAddingCategory = false;
    this.newCategory = { name: '', description: '', fundingType: 'BOTH' };
  }

  /**
   * Save a new category.
   */
  saveCategory(): void {
    if (!this.rcId || !this.fyId) {
      return;
    }

    if (!this.newCategory.name?.trim()) {
      this.categoryError = 'Name is required';
      return;
    }

    this.isSaving = true;
    this.categoryError = null;

    this.categoryService.createCategory(this.rcId, this.fyId, this.newCategory).subscribe({
      next: (created) => {
        this.categories.push(created);
        this.categories.sort((a, b) => a.displayOrder - b.displayOrder);
        this.isAddingCategory = false;
        this.newCategory = { name: '', description: '' };
        this.isSaving = false;
        this.showSuccess(`Category "${created.name}" created successfully`);
      },
      error: (error) => {
        this.categoryError = error.message || 'Failed to create category';
        this.isSaving = false;
      }
    });
  }

  /**
   * Start editing a category.
   */
  startEditCategory(category: Category): void {
    if (category.isDefault) {
      this.categoryError = 'Default categories cannot be edited';
      return;
    }
    this.editingCategoryId = category.id;
    this.editCategory = {
      name: category.name,
      description: category.description || '',
      fundingType: category.fundingType || 'BOTH'
    };
    this.isAddingCategory = false;
  }

  /**
   * Cancel editing a category.
   */
  cancelEditCategory(): void {
    this.editingCategoryId = null;
    this.editCategory = { name: '', description: '', fundingType: 'BOTH' };
  }

  /**
   * Update a category.
   */
  updateCategory(category: Category): void {
    if (!this.rcId || !this.fyId) {
      return;
    }

    if (category.isDefault) {
      this.categoryError = 'Default categories cannot be updated';
      return;
    }

    if (!this.editCategory.name?.trim()) {
      this.categoryError = 'Name is required';
      return;
    }

    this.isSaving = true;
    this.categoryError = null;

    this.categoryService.updateCategory(this.rcId, this.fyId, category.id, this.editCategory).subscribe({
      next: (updated) => {
        const index = this.categories.findIndex(c => c.id === category.id);
        if (index !== -1) {
          this.categories[index] = updated;
        }
        this.editingCategoryId = null;
        this.editCategory = { name: '', description: '' };
        this.isSaving = false;
        this.showSuccess(`Category "${updated.name}" updated successfully`);
      },
      error: (error) => {
        this.categoryError = error.message || 'Failed to update category';
        this.isSaving = false;
      }
    });
  }

  /**
   * Delete a category.
   */
  deleteCategory(category: Category): void {
    if (!this.rcId || !this.fyId) {
      return;
    }

    if (category.isDefault) {
      this.categoryError = 'Cannot delete a default category';
      return;
    }

    if (category.canDelete === false) {
      this.categoryError = 'Cannot delete a category that is in use by funding, procurement, or spending items';
      return;
    }

    if (!confirm(`Are you sure you want to delete the category "${category.name}"?`)) {
      return;
    }

    this.isDeleting = true;
    this.categoryError = null;

    this.categoryService.deleteCategory(this.rcId, this.fyId, category.id).subscribe({
      next: () => {
        this.categories = this.categories.filter(c => c.id !== category.id);
        this.isDeleting = false;
        this.showSuccess(`Category "${category.name}" deleted successfully`);
      },
      error: (error) => {
        this.categoryError = error.message || 'Failed to delete category';
        this.isDeleting = false;
      }
    });
  }

  /**
   * Clear category error message.
   */
  clearCategoryError(): void {
    this.categoryError = null;
  }

  /**
   * Update On Target threshold settings for the fiscal year.
   */
  updateOnTargetSetting(setting: 'onTargetMin' | 'onTargetMax', value: string): void {
    if (!this.rcId || !this.fyId || !this.selectedFY) {
      return;
    }

    // Clear any previous error when slider is moved
    this.moneyError = null;

    const numValue = parseInt(value, 10);
    if (isNaN(numValue)) {
      return;
    }

    // Validate that min <= max
    if (setting === 'onTargetMin' && numValue > (this.selectedFY.onTargetMax ?? 2)) {
      this.moneyError = 'Minimum threshold cannot be greater than maximum threshold';
      return;
    }
    if (setting === 'onTargetMax' && numValue < (this.selectedFY.onTargetMin ?? -2)) {
      this.moneyError = 'Maximum threshold cannot be less than minimum threshold';
      return;
    }

    const request = { [setting]: numValue };
    
    this.fyService.updateDisplaySettings(this.rcId, this.fyId, request).subscribe({
      next: (updatedFY) => {
        this.selectedFY = updatedFY;
        // Reset the temp values
        this.onTargetMinValue = null;
        this.onTargetMaxValue = null;
        const settingLabel = setting === 'onTargetMin' ? 'Minimum threshold' : 'Maximum threshold';
        this.showSuccess(`${settingLabel} updated to ${numValue}%`);
      },
      error: (error) => {
        this.moneyError = error.message || 'Failed to update On Target settings';
      }
    });
  }

  /**
   * Track function for ngFor optimization.
   */
  trackByCategoryId(index: number, category: Category): number {
    return category.id;
  }

  /**
   * Get the display label for a funding type.
   */
  getFundingTypeLabel(fundingType: FundingType | string | undefined): string {
    if (!fundingType) return this.translate.instant('configuration.fundingTypeBoth');
    switch (fundingType) {
      case 'CAP_ONLY': return this.translate.instant('configuration.fundingTypeCapOnly');
      case 'OM_ONLY': return this.translate.instant('configuration.fundingTypeOmOnly');
      default: return this.translate.instant('configuration.fundingTypeBoth');
    }
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
   * Get the display description for a category, using the translation key if available.
   * Default (system) categories use their translationKey + 'Desc' suffix for i18n.
   * Custom categories display their user-entered description directly.
   *
   * @param category the category to get the description for
   * @returns the translated category description, or the raw description
   */
  getCategoryDescriptionDisplay(category: Category): string {
    if (category.translationKey) {
      const descKey = category.translationKey + 'Desc';
      const translated = this.translate.instant(descKey);
      return translated !== descKey ? translated : (category.description || '');
    }
    return category.description || '';
  }

  /**
   * Select export destination using browser file picker.
   * Uses the File System Access API to allow user to choose destination.
   */
  async selectExportDestination(): Promise<void> {
    // Generate default filename with export, date, and RC_FY format
    const rcName = this.selectedRC?.name || 'RC';
    const fyName = this.selectedFY?.name || 'FY';
    const today = new Date();
    const dateStr = today.toISOString().split('T')[0]; // yyyy-mm-dd format
    const defaultFilename = `${rcName}_${fyName}_export_${dateStr}.csv`;

    // Check if File System Access API is available
    if ('showSaveFilePicker' in window) {
      try {
        const options = {
          suggestedName: defaultFilename,
          types: [
            {
              description: 'CSV Files',
              accept: {
                'text/csv': ['.csv']
              }
            }
          ]
        };
        
        const handle = await (window as any).showSaveFilePicker(options);
        this.exportFileHandle = handle;
        this.exportPath = handle.name;
        this.exportErrorMessage = null;
      } catch (err: any) {
        // User cancelled the picker or error occurred
        if (err.name !== 'AbortError') {
          this.exportErrorMessage = 'Failed to select export destination';
        }
      }
    } else {
      // Fallback for browsers without File System Access API
      this.exportPath = defaultFilename;
      this.exportFileHandle = null;
    }
  }

  /**
   * Check if export destination is valid.
   */
  isExportValid(): boolean {
    return this.exportPath.trim().length > 0;
  }

  /**
   * Export Funding, Procurement, and Spending items to CSV.
   */
  exportToCSV(): void {
    if (!this.rcId || !this.fyId) {
      this.exportErrorMessage = 'Please select a Responsibility Centre and Fiscal Year';
      return;
    }

    if (!this.isExportValid()) {
      this.exportErrorMessage = 'Please select an export destination first';
      return;
    }

    this.isExporting = true;
    this.exportSuccessMessage = null;
    this.exportErrorMessage = null;

    // Fetch all data for export
    Promise.all([
      this.fetchFundingItems(),
      this.fetchProcurementItems(),
      this.fetchSpendingItems()
    ]).then(async ([fundingItems, procurementItems, spendingItems]) => {
      const csvContent = this.generateCSVContent(fundingItems, procurementItems, spendingItems);
      await this.downloadCSV(csvContent);
      this.exportSuccessMessage = 'Export completed successfully!';
      this.isExporting = false;
    }).catch(error => {
      this.exportErrorMessage = `Export failed: ${error.message || 'Unknown error'}`;
      this.isExporting = false;
    });
  }

  /**
   * Fetch funding items for export.
   */
  private fetchFundingItems(): Promise<any[]> {
    return fetch(`/api/fiscal-years/${this.fyId}/funding-items`)
      .then(response => {
        if (!response.ok) throw new Error('Failed to fetch funding items');
        return response.json();
      });
  }

  /**
   * Fetch procurement items for export.
   */
  private fetchProcurementItems(): Promise<any[]> {
    return fetch(`/api/responsibility-centres/${this.rcId}/fiscal-years/${this.fyId}/procurement-items`)
      .then(response => {
        if (!response.ok) throw new Error('Failed to fetch procurement items');
        return response.json();
      });
  }

  /**
   * Fetch spending items for export.
   */
  private fetchSpendingItems(): Promise<any[]> {
    return fetch(`/api/responsibility-centres/${this.rcId}/fiscal-years/${this.fyId}/spending-items`)
      .then(response => {
        if (!response.ok) throw new Error('Failed to fetch spending items');
        return response.json();
      });
  }

  /**
   * Generate CSV content from all items.
   */
  private generateCSVContent(fundingItems: any[], procurementItems: any[], spendingItems: any[]): string {
    const lines: string[] = [];

    // Funding Items Section
    lines.push('=== FUNDING ITEMS ===');
    lines.push('Type,ID,Name,Description,Source,Amount,Currency,Exchange Rate,Category,Money Allocations,Status,Created Date');
    for (const item of fundingItems) {
      const allocations = item.moneyAllocations?.map((a: any) => `${a.moneyCode || a.money?.code}:${a.amount}`).join(';') || '';
      lines.push(this.escapeCSVRow([
        'FUNDING',
        item.id,
        item.name,
        item.description || '',
        item.source || '',
        item.amount || 0,
        item.currency || 'USD',
        item.exchangeRate || 1,
        item.category?.name || '',
        allocations,
        item.status || '',
        item.createdAt || ''
      ]));
    }

    lines.push('');

    // Procurement Items Section
    lines.push('=== PROCUREMENT ITEMS ===');
    lines.push('Type,ID,PR Number,PO Number,Name,Description,Vendor,Status,Amount,Currency,Exchange Rate,Category,Created Date');
    for (const item of procurementItems) {
      lines.push(this.escapeCSVRow([
        'PROCUREMENT',
        item.id,
        item.purchaseRequisition || '',
        item.purchaseOrder || '',
        item.name,
        item.description || '',
        item.vendor || '',
        item.status || '',
        item.amount || 0,
        item.currency || 'USD',
        item.exchangeRate || 1,
        item.category?.name || '',
        item.createdAt || ''
      ]));
    }

    lines.push('');

    // Spending Items Section
    lines.push('=== SPENDING ITEMS ===');
    lines.push('Type,ID,Name,Description,Vendor,Reference Number,Amount,Currency,Exchange Rate,Category,Money Allocations,Status,Created Date');
    for (const item of spendingItems) {
      const allocations = item.moneyAllocations?.map((a: any) => `${a.moneyCode || a.money?.code}:${a.amount}`).join(';') || '';
      lines.push(this.escapeCSVRow([
        'SPENDING',
        item.id,
        item.name,
        item.description || '',
        item.vendor || '',
        item.referenceNumber || '',
        item.amount || 0,
        item.currency || 'USD',
        item.exchangeRate || 1,
        item.category?.name || '',
        allocations,
        item.status || '',
        item.createdAt || ''
      ]));
    }

    return lines.join('\n');
  }

  /**
   * Escape a row of values for CSV format.
   */
  private escapeCSVRow(values: any[]): string {
    return values.map(value => {
      const strValue = String(value ?? '');
      // Escape double quotes and wrap in quotes if contains comma, newline, or quotes
      if (strValue.includes(',') || strValue.includes('\n') || strValue.includes('"')) {
        return '"' + strValue.replace(/"/g, '""') + '"';
      }
      return strValue;
    }).join(',');
  }

  /**
   * Download the CSV content as a file.
   */
  private async downloadCSV(content: string): Promise<void> {
    // If we have a file handle from File System Access API, use it
    if (this.exportFileHandle) {
      try {
        const writable = await this.exportFileHandle.createWritable();
        await writable.write(content);
        await writable.close();
        return;
      } catch (err) {
        // Fall through to the standard download approach
        console.warn('Failed to write using file handle, falling back to download:', err);
      }
    }

    // Standard download approach
    const blob = new Blob([content], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    
    // Generate filename with export and date
    const rcName = this.selectedRC?.name || 'RC';
    const fyName = this.selectedFY?.name || 'FY';
    const today = new Date();
    const dateStr = today.toISOString().split('T')[0]; // yyyy-mm-dd format
    const filename = this.exportPath || `${rcName}_${fyName}_export_${dateStr}.csv`;
    
    link.setAttribute('href', url);
    link.setAttribute('download', filename);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  }
}
