/*
 * Cinema Box Office - Dashboard Component
 * Copyright (c) 2026 Box Office Team
 * Licensed under MIT License
 */
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { HttpClient } from '@angular/common/http';
import { User } from '../../models/user.model';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ThemeService, Theme } from '../../services/theme.service';
import { ResponsibilityCentreService } from '../../services/responsibility-centre.service';
import { FiscalYearService } from '../../services/fiscal-year.service';
import { FundingItemService } from '../../services/funding-item.service';
import { ResponsibilityCentreDTO } from '../../models/responsibility-centre.model';
import { FiscalYear } from '../../models/fiscal-year.model';
import { FundingItem, FundingItemCreateRequest, getStatusLabel, getStatusClass, FundingItemStatus } from '../../models/funding-item.model';

/**
 * Dashboard component showing funding items for the selected RC and FY.
 *
 * @author Box Office Team
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
  title = 'Cinema Box Office';
  currentUser: User | null = null;
  currentTheme: Theme = 'light';
  isLoggingOut = false;
  isUserMenuOpen = false;

  // Selected RC and FY
  selectedRC: ResponsibilityCentreDTO | null = null;
  selectedFY: FiscalYear | null = null;

  // Funding Items
  fundingItems: FundingItem[] = [];
  isLoadingItems = false;

  // Create Form
  showCreateForm = false;
  isCreating = false;
  newItemName = '';
  newItemDescription = '';
  newItemBudget: number | null = null;
  newItemStatus: FundingItemStatus = 'DRAFT';

  // Messages
  errorMessage: string | null = null;
  successMessage: string | null = null;

  // Status list for dropdown
  statusOptions: FundingItemStatus[] = ['DRAFT', 'PENDING', 'APPROVED', 'ACTIVE', 'CLOSED'];

  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private router: Router,
    private http: HttpClient,
    private themeService: ThemeService,
    private rcService: ResponsibilityCentreService,
    private fyService: FiscalYearService,
    private fundingItemService: FundingItemService
  ) {}

  ngOnInit(): void {
    // Subscribe to current user
    this.authService.currentUser$.pipe(takeUntil(this.destroy$)).subscribe((user: User | null) => {
      this.currentUser = user;
      if (!user) {
        this.router.navigate(['/login']);
      } else {
        this.loadSelectedContext();
        // Load user's theme preference from server
        this.themeService.getUserTheme(user.username).subscribe({
          next: (response) => {
            this.currentTheme = response.theme;
            this.themeService.setTheme(response.theme);
          },
          error: (error) => {
            console.error('Failed to load theme preference:', error);
          },
        });
      }
    });

    // Subscribe to theme changes
    this.themeService.currentTheme$.pipe(takeUntil(this.destroy$)).subscribe((theme: Theme) => {
      this.currentTheme = theme;
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Load the selected RC and FY context from service.
   */
  private loadSelectedContext(): void {
    const rcId = this.rcService.getSelectedRC();
    const fyId = this.rcService.getSelectedFY();

    if (!rcId || !fyId) {
      // Redirect to RC selection if not selected
      this.router.navigate(['/rc-selection']);
      return;
    }

    // Load RC details
    this.rcService.getResponsibilityCentre(rcId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (rc) => {
          this.selectedRC = rc;
        },
        error: (error) => {
          console.error('Failed to load RC:', error);
          this.router.navigate(['/rc-selection']);
        }
      });

    // Load FY details
    this.fyService.getFiscalYear(rcId, fyId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (fy) => {
          this.selectedFY = fy;
          this.loadFundingItems();
        },
        error: (error) => {
          console.error('Failed to load FY:', error);
          this.router.navigate(['/rc-selection']);
        }
      });
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
        },
        error: (error) => {
          this.errorMessage = error.message || 'Failed to load funding items.';
          this.isLoadingItems = false;
        }
      });
  }

  /**
   * Get sorted list of funding items (alphabetical by name).
   */
  get sortedFundingItems(): FundingItem[] {
    return [...this.fundingItems].sort((a, b) =>
      a.name.localeCompare(b.name, undefined, { sensitivity: 'base' })
    );
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

    this.isCreating = true;
    this.errorMessage = null;
    this.successMessage = null;

    const request: FundingItemCreateRequest = {
      name: this.newItemName.trim(),
      description: this.newItemDescription.trim(),
      budgetAmount: this.newItemBudget || undefined,
      status: this.newItemStatus
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
          this.successMessage = `Funding Item "${item.name}" deleted successfully.`;
          setTimeout(() => this.clearSuccess(), 5000);
        },
        error: (error) => {
          this.errorMessage = error.message || 'Failed to delete funding item.';
        }
      });
  }

  /**
   * Reset the create form.
   */
  private resetForm(): void {
    this.newItemName = '';
    this.newItemDescription = '';
    this.newItemBudget = null;
    this.newItemStatus = 'DRAFT';
  }

  /**
   * Get status display label.
   */
  getStatusLabel(status: FundingItemStatus): string {
    return getStatusLabel(status);
  }

  /**
   * Get status CSS class.
   */
  getStatusClass(status: FundingItemStatus): string {
    return getStatusClass(status);
  }

  /**
   * Format currency for display.
   */
  formatCurrency(amount: number | null): string {
    if (amount === null || amount === undefined) {
      return '-';
    }
    return new Intl.NumberFormat('en-CA', {
      style: 'currency',
      currency: 'CAD'
    }).format(amount);
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

  /**
   * Toggle user menu dropdown.
   */
  toggleUserMenu(): void {
    this.isUserMenuOpen = !this.isUserMenuOpen;
  }

  /**
   * Close user menu.
   */
  closeUserMenu(): void {
    this.isUserMenuOpen = false;
  }

  /**
   * Toggle between light and dark theme.
   */
  toggleTheme(): void {
    if (!this.currentUser) {
      return;
    }

    const newTheme: Theme = this.currentTheme === 'light' ? 'dark' : 'light';
    this.themeService.setTheme(newTheme);

    this.themeService.updateUserTheme(this.currentUser.username, newTheme).subscribe({
      next: () => {
        console.log(`Theme updated to ${newTheme}`);
      },
      error: (error) => {
        console.error('Failed to update theme:', error);
        this.themeService.setTheme(this.currentTheme === 'light' ? 'dark' : 'light');
      },
    });
    this.closeUserMenu();
  }

  /**
   * Get theme icon based on current theme.
   */
  getThemeIcon(): string {
    return this.currentTheme === 'light' ? '🌙' : '☀️';
  }

  /**
   * Handle user logout.
   */
  logout(): void {
    this.isLoggingOut = true;
    setTimeout(() => {
      this.authService.logout();
      this.router.navigate(['/login']);
    }, 300);
  }
}
