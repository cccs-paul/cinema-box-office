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
import { ResponsibilityCentreService } from '../../services/responsibility-centre.service';
import { FiscalYearService } from '../../services/fiscal-year.service';
import { MoneyService } from '../../services/money.service';
import { SpendingCategoryService, CategoryCreateRequest, CategoryUpdateRequest } from '../../services/spending-category.service';
import { Money, MoneyCreateRequest, MoneyUpdateRequest } from '../../models/money.model';
import { SpendingCategory } from '../../models/spending-category.model';
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
  imports: [CommonModule, FormsModule],
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
  activeTab: 'monies' | 'categories' | 'general' = 'monies';

  // Money management state
  monies: Money[] = [];
  isLoadingMonies = false;
  moneyError: string | null = null;

  // Money form state
  isAddingMoney = false;
  editingMoneyId: number | null = null;
  newMoney: MoneyCreateRequest = { code: '', name: '', description: '' };
  editMoney: MoneyUpdateRequest = { code: '', name: '', description: '' };

  // Spending Category management state
  categories: SpendingCategory[] = [];
  isLoadingCategories = false;
  categoryError: string | null = null;

  // Category form state
  isAddingCategory = false;
  editingCategoryId: number | null = null;
  newCategory: CategoryCreateRequest = { name: '', description: '' };
  editCategory: CategoryUpdateRequest = { name: '', description: '' };

  // Operation state
  isSaving = false;
  isDeleting = false;
  successMessage: string | null = null;

  private destroy$ = new Subject<void>();

  constructor(
    private rcService: ResponsibilityCentreService,
    private fyService: FiscalYearService,
    private moneyService: MoneyService,
    private spendingCategoryService: SpendingCategoryService
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
  setActiveTab(tab: 'monies' | 'categories' | 'general'): void {
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
  // Spending Categories
  // =====================

  /**
   * Load spending categories for the current fiscal year.
   */
  loadCategories(): void {
    if (!this.rcId || !this.fyId) {
      return;
    }

    this.isLoadingCategories = true;
    this.categoryError = null;

    this.spendingCategoryService.getCategoriesByFY(this.rcId, this.fyId).subscribe({
      next: (categories) => {
        this.categories = categories;
        this.isLoadingCategories = false;
      },
      error: (error) => {
        this.categoryError = error.message || 'Failed to load spending categories';
        this.isLoadingCategories = false;
      }
    });
  }

  /**
   * Start adding a new spending category.
   */
  startAddCategory(): void {
    this.isAddingCategory = true;
    this.newCategory = { name: '', description: '' };
    this.editingCategoryId = null;
  }

  /**
   * Cancel adding a new spending category.
   */
  cancelAddCategory(): void {
    this.isAddingCategory = false;
    this.newCategory = { name: '', description: '' };
  }

  /**
   * Save a new spending category.
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

    this.spendingCategoryService.createCategory(this.rcId, this.fyId, this.newCategory).subscribe({
      next: (created) => {
        this.categories.push(created);
        this.categories.sort((a, b) => a.displayOrder - b.displayOrder);
        this.isAddingCategory = false;
        this.newCategory = { name: '', description: '' };
        this.isSaving = false;
        this.showSuccess(`Spending category "${created.name}" created successfully`);
      },
      error: (error) => {
        this.categoryError = error.message || 'Failed to create spending category';
        this.isSaving = false;
      }
    });
  }

  /**
   * Start editing a spending category.
   */
  startEditCategory(category: SpendingCategory): void {
    this.editingCategoryId = category.id;
    this.editCategory = {
      name: category.name,
      description: category.description || ''
    };
    this.isAddingCategory = false;
  }

  /**
   * Cancel editing a spending category.
   */
  cancelEditCategory(): void {
    this.editingCategoryId = null;
    this.editCategory = { name: '', description: '' };
  }

  /**
   * Update a spending category.
   */
  updateCategory(category: SpendingCategory): void {
    if (!this.rcId || !this.fyId) {
      return;
    }

    if (!this.editCategory.name?.trim()) {
      this.categoryError = 'Name is required';
      return;
    }

    this.isSaving = true;
    this.categoryError = null;

    this.spendingCategoryService.updateCategory(this.rcId, this.fyId, category.id, this.editCategory).subscribe({
      next: (updated) => {
        const index = this.categories.findIndex(c => c.id === category.id);
        if (index !== -1) {
          this.categories[index] = updated;
        }
        this.editingCategoryId = null;
        this.editCategory = { name: '', description: '' };
        this.isSaving = false;
        this.showSuccess(`Spending category "${updated.name}" updated successfully`);
      },
      error: (error) => {
        this.categoryError = error.message || 'Failed to update spending category';
        this.isSaving = false;
      }
    });
  }

  /**
   * Delete a spending category.
   */
  deleteCategory(category: SpendingCategory): void {
    if (!this.rcId || !this.fyId) {
      return;
    }

    if (category.isDefault) {
      this.categoryError = 'Cannot delete a default spending category';
      return;
    }

    if (!confirm(`Are you sure you want to delete the category "${category.name}"?`)) {
      return;
    }

    this.isDeleting = true;
    this.categoryError = null;

    this.spendingCategoryService.deleteCategory(this.rcId, this.fyId, category.id).subscribe({
      next: () => {
        this.categories = this.categories.filter(c => c.id !== category.id);
        this.isDeleting = false;
        this.showSuccess(`Spending category "${category.name}" deleted successfully`);
      },
      error: (error) => {
        this.categoryError = error.message || 'Failed to delete spending category';
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
   * Track function for ngFor optimization.
   */
  trackByCategoryId(index: number, category: SpendingCategory): number {
    return category.id;
  }
}
