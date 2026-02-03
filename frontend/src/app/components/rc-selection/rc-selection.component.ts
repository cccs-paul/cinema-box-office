/*
 * myRC - RC Selection Component
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */

import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil, filter, take } from 'rxjs/operators';
import { TranslateModule } from '@ngx-translate/core';
import { ResponsibilityCentreService } from '../../services/responsibility-centre.service';
import { ResponsibilityCentreDTO } from '../../models/responsibility-centre.model';
import { FiscalYearService } from '../../services/fiscal-year.service';
import { FiscalYear, FiscalYearCreateRequest, FiscalYearUpdateRequest } from '../../models/fiscal-year.model';
import { AuthService } from '../../services/auth.service';
import { ThemeService, Theme } from '../../services/theme.service';

/**
 * RC Selection Component
 *
 * Provides a two-panel interface for selecting Responsibility Centres and Fiscal Years.
 * Users must select both an RC and a FY before navigating to the main dashboard.
 * Includes action buttons for creating, cloning, deleting, and renaming RCs and FYs.
 *
 * @author myRC Team
 * @version 1.1.0
 * @since 2026-01-22
 */
@Component({
  selector: 'app-rc-selection',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './rc-selection.component.html',
  styleUrls: ['./rc-selection.component.scss']
})
export class RCSelectionComponent implements OnInit, OnDestroy {
  // RC State
  responsibilityCentres: ResponsibilityCentreDTO[] = [];
  isLoading = true;
  newRCName = '';
  newRCDescription = '';
  showCreateForm = false;
  showCloneForm = false;
  showRenameForm = false;
  isCreating = false;
  isCloning = false;
  isRenaming = false;
  isDeleting = false;
  selectedRCId: number | null = null;
  cloneNewName = '';
  renameNewName = '';
  renameNewDescription = '';

  // Fiscal Year State
  fiscalYears: FiscalYear[] = [];
  isFYLoading = false;
  newFYName = '';
  newFYDescription = '';
  showFYCreateForm = false;
  showFYRenameForm = false;
  isCreatingFY = false;
  isRenamingFY = false;
  isTogglingFYActive = false;
  selectedFYId: number | null = null;
  renameFYNewName = '';
  renameFYNewDescription = '';

  // Messages
  errorMessage: string | null = null;
  successMessage: string | null = null;

  // Confirmation dialog
  showDeleteConfirm = false;

  private destroy$ = new Subject<void>();

  constructor(
    private rcService: ResponsibilityCentreService,
    private fyService: FiscalYearService,
    private authService: AuthService,
    private router: Router,
    private themeService: ThemeService
  ) {}

  ngOnInit(): void {
    // Restore user's saved theme preference from localStorage
    // The login page forces light theme, so we restore user's choice here
    const savedTheme = localStorage.getItem('appTheme') as Theme || 'light';
    this.themeService.setTheme(savedTheme);

    // Wait for authentication to be checked before loading RCs
    this.authService.currentUser$
      .pipe(
        filter(user => user !== null), // Wait until user is loaded
        take(1), // Only take the first non-null user
        takeUntil(this.destroy$)
      )
      .subscribe(() => {
        this.loadResponsibilityCentres();
      });
  }

  /**
   * Get sorted list of responsibility centres (alphabetical by name, Demo at bottom).
   */
  get sortedResponsibilityCentres(): ResponsibilityCentreDTO[] {
    return [...this.responsibilityCentres].sort((a, b) => {
      // Demo RC always goes to the bottom
      if (a.name === 'Demo') return 1;
      if (b.name === 'Demo') return -1;
      return a.name.localeCompare(b.name, undefined, { sensitivity: 'base' });
    });
  }

  /**
   * Get sorted list of fiscal years (alphabetical by name).
   */
  get sortedFiscalYears(): FiscalYear[] {
    return [...this.fiscalYears].sort((a, b) =>
      a.name.localeCompare(b.name, undefined, { sensitivity: 'base' })
    );
  }

  /**
   * Check if the selected RC allows write access.
   */
  get selectedRCCanWrite(): boolean {
    if (this.selectedRCId === null) {
      return false;
    }
    const rc = this.responsibilityCentres.find(r => r.id === this.selectedRCId);
    return rc ? (rc.isOwner || rc.accessLevel === 'READ_WRITE') : false;
  }

  /**
   * Check if the selected RC is owned by the current user.
   */
  get selectedRCIsOwner(): boolean {
    if (this.selectedRCId === null) {
      return false;
    }
    const rc = this.responsibilityCentres.find(r => r.id === this.selectedRCId);
    return rc ? rc.isOwner : false;
  }

  /**
   * Get the selected RC.
   */
  get selectedRC(): ResponsibilityCentreDTO | null {
    if (this.selectedRCId === null) {
      return null;
    }
    return this.responsibilityCentres.find(r => r.id === this.selectedRCId) || null;
  }

  /**
   * Get the selected FY.
   */
  get selectedFY(): FiscalYear | null {
    if (this.selectedFYId === null) {
      return null;
    }
    return this.fiscalYears.find(f => f.id === this.selectedFYId) || null;
  }

  /**
   * Load all responsibility centres the user has access to.
   */
  loadResponsibilityCentres(): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.rcService.getAllResponsibilityCentres()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (rcs: ResponsibilityCentreDTO[]) => {
          this.responsibilityCentres = rcs;
          this.isLoading = false;
          this.errorMessage = null;
        },
        error: (error: unknown) => {
          this.isLoading = false;
          const httpError = error as { status?: number };
          if (httpError.status === 401) {
            this.errorMessage = 'Your session has expired. Please log in again.';
          } else if (httpError.status === 403) {
            this.errorMessage = 'You do not have permission to view responsibility centres.';
          } else if (httpError.status === 404) {
            this.responsibilityCentres = [];
            this.errorMessage = null;
          } else {
            this.errorMessage = 'Failed to load responsibility centres. Please try again later.';
          }
        }
      });
  }

  /**
   * Load fiscal years for the selected RC.
   */
  loadFiscalYears(rcId: number): void {
    this.isFYLoading = true;
    this.fiscalYears = [];
    this.selectedFYId = null;

    this.fyService.getFiscalYearsByRC(rcId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (fys: FiscalYear[]) => {
          this.fiscalYears = fys;
          this.isFYLoading = false;
        },
        error: (error: Error) => {
          this.isFYLoading = false;
          this.errorMessage = error.message || 'Failed to load fiscal years.';
        }
      });
  }

  /**
   * Select an RC without navigating to dashboard.
   */
  selectRCWithoutNavigate(rc: ResponsibilityCentreDTO): void {
    if (this.selectedRCId === rc.id) {
      return; // Already selected
    }
    this.selectedRCId = rc.id;
    this.selectedFYId = null;
    this.closeAllForms();
    this.loadFiscalYears(rc.id);
  }

  /**
   * Select a fiscal year.
   */
  selectFY(fy: FiscalYear): void {
    this.selectedFYId = fy.id;
    this.closeFYForms();
  }

  /**
   * Navigate to dashboard with selected RC and FY.
   */
  navigateToDashboard(): void {
    if (this.selectedRCId === null || this.selectedFYId === null) {
      return;
    }
    this.rcService.setSelectedRC(this.selectedRCId);
    this.rcService.setSelectedFY(this.selectedFYId);
    this.router.navigate(['/app/dashboard']);
  }

  // =========== RC Actions ===========

  /**
   * Toggle the create RC form visibility.
   */
  toggleCreateForm(): void {
    const wasOpen = this.showCreateForm;
    this.closeAllForms();
    this.showCreateForm = !wasOpen;
    if (!this.showCreateForm) {
      this.resetRCForm();
    }
  }

  /**
   * Toggle the clone RC form visibility.
   */
  toggleCloneForm(): void {
    if (this.selectedRCId === null) return;
    this.closeAllForms();
    this.showCloneForm = true;
    const rc = this.selectedRC;
    this.cloneNewName = rc ? rc.name + ' (Copy)' : '';
  }

  /**
   * Toggle the rename RC form visibility.
   */
  toggleRenameForm(): void {
    if (this.selectedRCId === null || !this.selectedRCIsOwner) return;
    this.closeAllForms();
    this.showRenameForm = true;
    const rc = this.selectedRC;
    if (rc) {
      this.renameNewName = rc.name;
      this.renameNewDescription = rc.description || '';
    }
  }

  /**
   * Show delete confirmation dialog.
   */
  confirmDelete(): void {
    if (this.selectedRCId === null || !this.selectedRCIsOwner) return;
    this.showDeleteConfirm = true;
  }

  /**
   * Cancel delete operation.
   */
  cancelDelete(): void {
    this.showDeleteConfirm = false;
  }

  /**
   * Navigate to the RC Permissions page.
   */
  managePermissions(): void {
    if (this.selectedRCId === null || !this.selectedRCIsOwner) return;
    this.router.navigate(['/rc-permissions', this.selectedRCId]);
  }

  /**
   * Close all RC forms.
   */
  closeAllForms(): void {
    this.showCreateForm = false;
    this.showCloneForm = false;
    this.showRenameForm = false;
    this.showDeleteConfirm = false;
    this.closeFYForms();
  }

  /**
   * Create a new responsibility centre.
   */
  createRC(): void {
    if (!this.newRCName.trim()) {
      this.errorMessage = 'RC name is required';
      return;
    }

    this.isCreating = true;
    this.errorMessage = null;
    this.successMessage = null;

    this.rcService.createResponsibilityCentre(
      this.newRCName.trim(),
      this.newRCDescription.trim()
    ).pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (newRC: ResponsibilityCentreDTO) => {
          this.responsibilityCentres.push(newRC);
          this.resetRCForm();
          this.showCreateForm = false;
          this.isCreating = false;
          this.successMessage = `Responsibility Centre "${newRC.name}" created successfully.`;
          // Select the newly created RC
          this.selectRCWithoutNavigate(newRC);
          // Auto-clear success message after 5 seconds
          setTimeout(() => this.clearSuccess(), 5000);
        },
        error: (error: unknown) => {
          this.isCreating = false;
          const err = error as Error;
          this.errorMessage = err.message || 'Failed to create responsibility centre. Please try again.';
        }
      });
  }

  /**
   * Clone the selected responsibility centre.
   */
  cloneRC(): void {
    if (this.selectedRCId === null || !this.cloneNewName.trim()) {
      this.errorMessage = 'New name is required for cloning';
      return;
    }

    this.isCloning = true;
    this.errorMessage = null;
    this.successMessage = null;

    this.rcService.cloneResponsibilityCentre(this.selectedRCId, this.cloneNewName.trim())
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (clonedRC: ResponsibilityCentreDTO) => {
          this.responsibilityCentres.push(clonedRC);
          this.showCloneForm = false;
          this.isCloning = false;
          this.cloneNewName = '';
          this.successMessage = `Responsibility Centre "${clonedRC.name}" cloned successfully.`;
          // Select the newly cloned RC
          this.selectRCWithoutNavigate(clonedRC);
          setTimeout(() => this.clearSuccess(), 5000);
        },
        error: (error: unknown) => {
          this.isCloning = false;
          const httpError = error as { status?: number; error?: { message?: string } };
          if (httpError.status === 400) {
            this.errorMessage = `A Responsibility Centre named "${this.cloneNewName}" already exists. Please choose a different name.`;
          } else if (httpError.status === 403) {
            this.errorMessage = 'You do not have permission to clone this RC.';
          } else {
            this.errorMessage = 'Failed to clone responsibility centre. Please try again.';
          }
        }
      });
  }

  /**
   * Rename the selected responsibility centre.
   */
  renameRC(): void {
    if (this.selectedRCId === null || !this.renameNewName.trim()) {
      this.errorMessage = 'Name is required';
      return;
    }

    this.isRenaming = true;
    this.errorMessage = null;
    this.successMessage = null;

    this.rcService.updateResponsibilityCentre(
      this.selectedRCId,
      this.renameNewName.trim(),
      this.renameNewDescription.trim()
    ).pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (updatedRC: ResponsibilityCentreDTO) => {
          // Update the RC in the list
          const index = this.responsibilityCentres.findIndex(r => r.id === updatedRC.id);
          if (index !== -1) {
            this.responsibilityCentres[index] = updatedRC;
          }
          this.showRenameForm = false;
          this.isRenaming = false;
          this.successMessage = `Responsibility Centre renamed to "${updatedRC.name}" successfully.`;
          setTimeout(() => this.clearSuccess(), 5000);
        },
        error: (error: unknown) => {
          this.isRenaming = false;
          const err = error as Error;
          this.errorMessage = err.message || 'Failed to rename responsibility centre. Please try again.';
        }
      });
  }

  /**
   * Delete the selected responsibility centre.
   */
  deleteRC(): void {
    if (this.selectedRCId === null) return;

    this.isDeleting = true;
    this.errorMessage = null;
    this.successMessage = null;
    const deletingRCName = this.selectedRC?.name || 'Unknown';

    this.rcService.deleteResponsibilityCentre(this.selectedRCId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          // Remove the RC from the list
          this.responsibilityCentres = this.responsibilityCentres.filter(r => r.id !== this.selectedRCId);
          this.selectedRCId = null;
          this.selectedFYId = null;
          this.fiscalYears = [];
          this.showDeleteConfirm = false;
          this.isDeleting = false;
          this.successMessage = `Responsibility Centre "${deletingRCName}" deleted successfully.`;
          setTimeout(() => this.clearSuccess(), 5000);
        },
        error: (error: unknown) => {
          this.isDeleting = false;
          this.showDeleteConfirm = false;
          const httpError = error as { status?: number };
          if (httpError.status === 403) {
            this.errorMessage = 'Only the owner can delete this RC.';
          } else if (httpError.status === 404) {
            this.errorMessage = 'Responsibility Centre not found.';
          } else {
            this.errorMessage = 'Failed to delete responsibility centre. Please try again.';
          }
        }
      });
  }

  // =========== Fiscal Year Actions ===========

  /**
   * Toggle the create FY form visibility.
   */
  toggleFYCreateForm(): void {
    const wasOpen = this.showFYCreateForm;
    this.closeFYForms();
    this.showFYCreateForm = !wasOpen;
    if (!this.showFYCreateForm) {
      this.resetFYForm();
    }
  }

  /**
   * Toggle the rename FY form visibility.
   */
  toggleFYRenameForm(): void {
    if (this.selectedFYId === null || !this.selectedRCCanWrite) return;
    this.closeFYForms();
    this.showFYRenameForm = true;
    const fy = this.selectedFY;
    if (fy) {
      this.renameFYNewName = fy.name;
      this.renameFYNewDescription = fy.description || '';
    }
  }

  /**
   * Close all FY forms.
   */
  closeFYForms(): void {
    this.showFYCreateForm = false;
    this.showFYRenameForm = false;
  }

  /**
   * Create a new fiscal year.
   */
  createFY(): void {
    if (!this.isValidFYForm()) {
      this.errorMessage = 'Please fill in all required fields';
      return;
    }

    if (this.selectedRCId === null) {
      this.errorMessage = 'No responsibility centre selected';
      return;
    }

    this.isCreatingFY = true;
    this.errorMessage = null;
    this.successMessage = null;

    const request: FiscalYearCreateRequest = {
      name: this.newFYName.trim(),
      description: this.newFYDescription.trim()
    };

    this.fyService.createFiscalYear(this.selectedRCId, request)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (newFY: FiscalYear) => {
          this.fiscalYears.push(newFY);
          this.resetFYForm();
          this.showFYCreateForm = false;
          this.isCreatingFY = false;
          this.successMessage = `Fiscal Year "${newFY.name}" created successfully.`;
          // Select the newly created FY
          this.selectFY(newFY);
          setTimeout(() => this.clearSuccess(), 5000);
        },
        error: (error: Error) => {
          this.isCreatingFY = false;
          if (error.message.toLowerCase().includes('already exists')) {
            this.errorMessage = `A Fiscal Year named "${this.newFYName}" already exists for this RC. Please choose a different name.`;
          } else {
            this.errorMessage = error.message || 'Failed to create fiscal year. Please try again.';
          }
        }
      });
  }

  /**
   * Rename the selected fiscal year.
   */
  renameFY(): void {
    if (this.selectedRCId === null || this.selectedFYId === null || !this.renameFYNewName.trim()) {
      this.errorMessage = 'Name is required';
      return;
    }

    this.isRenamingFY = true;
    this.errorMessage = null;
    this.successMessage = null;

    const request: FiscalYearUpdateRequest = {
      name: this.renameFYNewName.trim(),
      description: this.renameFYNewDescription.trim()
    };

    this.fyService.updateFiscalYear(this.selectedRCId, this.selectedFYId, request)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (updatedFY: FiscalYear) => {
          // Update the FY in the list
          const index = this.fiscalYears.findIndex(f => f.id === updatedFY.id);
          if (index !== -1) {
            this.fiscalYears[index] = updatedFY;
          }
          this.showFYRenameForm = false;
          this.isRenamingFY = false;
          this.successMessage = `Fiscal Year renamed to "${updatedFY.name}" successfully.`;
          setTimeout(() => this.clearSuccess(), 5000);
        },
        error: (error: Error) => {
          this.isRenamingFY = false;
          if (error.message.toLowerCase().includes('already exists')) {
            this.errorMessage = `A Fiscal Year named "${this.renameFYNewName}" already exists for this RC.`;
          } else {
            this.errorMessage = error.message || 'Failed to rename fiscal year. Please try again.';
          }
        }
      });
  }

  /**
   * Validate the fiscal year form.
   */
  isValidFYForm(): boolean {
    return !!this.newFYName.trim();
  }

  /**
   * Toggle the active status of the selected FY.
   * Only RC owners can toggle.
   */
  toggleFYActiveStatus(): void {
    if (this.selectedRCId === null || this.selectedFYId === null || !this.selectedRCIsOwner) {
      return;
    }

    this.isTogglingFYActive = true;
    this.errorMessage = null;
    this.successMessage = null;

    this.fyService.toggleActiveStatus(this.selectedRCId, this.selectedFYId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (updatedFY: FiscalYear) => {
          // Update the FY in the list
          const index = this.fiscalYears.findIndex(f => f.id === updatedFY.id);
          if (index !== -1) {
            this.fiscalYears[index] = updatedFY;
          }
          this.isTogglingFYActive = false;
          const statusText = updatedFY.active ? 'activated' : 'deactivated';
          this.successMessage = `Fiscal Year "${updatedFY.name}" ${statusText} successfully.`;
          setTimeout(() => this.clearSuccess(), 5000);
        },
        error: (error: Error) => {
          this.isTogglingFYActive = false;
          this.errorMessage = error.message || 'Failed to toggle fiscal year active status.';
        }
      });
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
   * Reset the RC creation form.
   */
  private resetRCForm(): void {
    this.newRCName = '';
    this.newRCDescription = '';
  }

  /**
   * Reset the FY creation form.
   */
  private resetFYForm(): void {
    this.newFYName = '';
    this.newFYDescription = '';
  }

  /**
   * Get access level label for display.
   */
  getAccessLevelLabel(accessLevel: string): string {
    switch (accessLevel) {
      case 'OWNER':
        return 'Owned';
      case 'READ_WRITE':
        return 'Read & Write';
      case 'READ_ONLY':
      default:
        return 'Read Only';
    }
  }

  /**
   * Get CSS class for access level badge.
   */
  getAccessLevelClass(accessLevel: string): string {
    switch (accessLevel) {
      case 'OWNER':
        return 'access-owner';
      case 'READ_WRITE':
        return 'access-readwrite';
      case 'READ_ONLY':
      default:
        return 'access-readonly';
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
