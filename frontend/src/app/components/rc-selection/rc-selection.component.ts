/*
 * Cinema Box Office - RC/FY Selection Component
 * Copyright (c) 2026 Box Office Team
 * Licensed under MIT License
 */

import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ResponsibilityCentreService } from '../../services/responsibility-centre.service';
import { ResponsibilityCentreDTO } from '../../models/responsibility-centre.model';
import { FiscalYearService } from '../../services/fiscal-year.service';
import { FiscalYearDTO } from '../../models/fiscal-year.model';

@Component({
  selector: 'app-rc-selection',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './rc-selection.component.html',
  styleUrls: ['./rc-selection.component.scss']
})
export class RCSelectionComponent implements OnInit, OnDestroy {
  // RC Selection State
  responsibilityCentres: ResponsibilityCentreDTO[] = [];
  isLoading = true;
  errorMessage: string | null = null;
  newRCName = '';
  newRCDescription = '';
  showCreateForm = false;
  isCreating = false;

  // FY Selection State
  isSelectingFY = false;
  selectedRC: ResponsibilityCentreDTO | null = null;
  fiscalYears: FiscalYearDTO[] = [];
  isLoadingFY = false;
  showCreateFYForm = false;
  isCreatingFY = false;
  newFYName = '';

  // Edit State
  editingRCId: number | null = null;
  editingFYId: number | null = null;
  editValue = '';
  editDescriptionValue = '';

  private destroy$ = new Subject<void>();

  constructor(
    private rcService: ResponsibilityCentreService,
    private fyService: FiscalYearService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadResponsibilityCentres();
  }

  loadResponsibilityCentres(): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.rcService.getAllResponsibilityCentres()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (rcs: ResponsibilityCentreDTO[]) => {
          this.responsibilityCentres = rcs;
          this.isLoading = false;
        },
        error: (error: any) => {
          this.errorMessage = 'Failed to load responsibility centres';
          this.isLoading = false;
        }
      });
  }

  onRCSelected(rc: ResponsibilityCentreDTO): void {
    this.selectedRC = rc;
    this.rcService.setSelectedRC(rc.id);
    this.loadFiscalYears(rc.id);
  }

  loadFiscalYears(rcId: number): void {
    this.isSelectingFY = true;
    this.isLoadingFY = true;
    this.errorMessage = null;
    this.fyService.getAllForRc(rcId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (fys: FiscalYearDTO[]) => {
          this.fiscalYears = fys;
          this.isLoadingFY = false;
        },
        error: (error: any) => {
          this.errorMessage = 'Failed to load fiscal years';
          this.isLoadingFY = false;
        }
      });
  }

  backToRCSelection(): void {
    this.isSelectingFY = false;
    this.selectedRC = null;
    this.fiscalYears = [];
    this.errorMessage = null;
  }

  onFYSelected(fy: FiscalYearDTO): void {
    this.fyService.setSelectedFY(fy.id);
    this.router.navigate(['/dashboard']);
  }

  toggleCreateForm(): void {
    this.showCreateForm = !this.showCreateForm;
    if (this.showCreateForm) {
      this.editingRCId = null;
    }
    if (!this.showCreateForm) {
      this.resetRCForm();
    }
  }

  startEditRC(event: Event, rc: ResponsibilityCentreDTO): void {
    event.stopPropagation();
    this.editingRCId = rc.id;
    this.newRCName = rc.name;
    this.newRCDescription = rc.description || '';
    this.showCreateForm = true;
  }

  deleteRC(event: Event, rc: ResponsibilityCentreDTO): void {
    event.stopPropagation();

    // First check if it has FYs
    this.fyService.getAllForRc(rc.id).subscribe({
      next: (fys) => {
        let message = `Are you sure you want to delete "${rc.name}"?`;
        if (fys.length > 0) {
          message = `Warning: This Responsibility Centre has ${fys.length} Fiscal Year(s). Deleting it will also delete all associated Fiscal Years. Continue?`;
        }

        if (confirm(message)) {
          this.rcService.deleteResponsibilityCentre(rc.id).subscribe({
            next: () => this.loadResponsibilityCentres(),
            error: (err) => this.errorMessage = err.error?.message || 'Failed to delete RC'
          });
        }
      },
      error: () => {
        if (confirm(`Are you sure you want to delete "${rc.name}"?`)) {
          this.rcService.deleteResponsibilityCentre(rc.id).subscribe({
            next: () => this.loadResponsibilityCentres(),
            error: (err) => this.errorMessage = err.error?.message || 'Failed to delete RC'
          });
        }
      }
    });
  }

  saveRC(): void {
    if (this.editingRCId) {
      this.updateRC();
    } else {
      this.createRC();
    }
  }

  updateRC(): void {
    if (!this.newRCName.trim() || !this.editingRCId) return;

    this.isCreating = true;
    this.rcService.updateResponsibilityCentre(
      this.editingRCId,
      this.newRCName,
      this.newRCDescription
    ).pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.loadResponsibilityCentres();
          this.resetRCForm();
          this.showCreateForm = false;
          this.isCreating = false;
          this.editingRCId = null;
        },
        error: (error: any) => {
          this.errorMessage = error.error?.message || 'Failed to update RC';
          this.isCreating = false;
        }
      });
  }

  createRC(): void {
    if (!this.newRCName.trim()) {
      this.errorMessage = 'RC name is required';
      return;
    }

    this.isCreating = true;
    this.rcService.createResponsibilityCentre(
      this.newRCName,
      this.newRCDescription
    ).pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (newRC: ResponsibilityCentreDTO) => {
          this.responsibilityCentres.push(newRC);
          this.resetRCForm();
          this.showCreateForm = false;
          this.isCreating = false;
          this.onRCSelected(newRC);
        },
        error: (error: any) => {
          this.errorMessage = error.error?.message || 'Failed to create RC';
          this.isCreating = false;
        }
      });
  }

  toggleCreateFYForm(): void {
    this.showCreateFYForm = !this.showCreateFYForm;
    if (this.showCreateFYForm) {
      this.editingFYId = null;
    }
    if (!this.showCreateFYForm) {
      this.resetFYForm();
    }
  }

  startEditFY(event: Event, fy: FiscalYearDTO): void {
    event.stopPropagation();
    this.editingFYId = fy.id;
    this.newFYName = fy.name;
    this.showCreateFYForm = true;
  }

  deleteFY(event: Event, fy: FiscalYearDTO): void {
    event.stopPropagation();
    if (confirm(`Are you sure you want to delete Fiscal Year "${fy.name}"?`)) {
      this.fyService.deleteFiscalYear(fy.id).subscribe({
        next: () => this.loadFiscalYears(fy.rcId),
        error: (err) => this.errorMessage = err.error?.message || 'Failed to delete FY'
      });
    }
  }

  saveFY(): void {
    if (this.editingFYId) {
      this.updateFY();
    } else {
      this.createFY();
    }
  }

  updateFY(): void {
    if (!this.newFYName.trim() || !this.editingFYId || !this.selectedRC) return;

    this.isCreatingFY = true;
    this.fyService.updateFiscalYear(this.editingFYId, this.newFYName)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.loadFiscalYears(this.selectedRC!.id);
          this.resetFYForm();
          this.showCreateFYForm = false;
          this.isCreatingFY = false;
          this.editingFYId = null;
        },
        error: (error: any) => {
          this.errorMessage = error.error?.message || 'Failed to update FY';
          this.isCreatingFY = false;
        }
      });
  }

  createFY(): void {
    if (!this.newFYName.trim()) {
      this.errorMessage = 'FY name is required';
      return;
    }

    if (!this.selectedRC) return;

    this.isCreatingFY = true;
    this.fyService.createFiscalYear(
      this.newFYName,
      this.selectedRC.id
    ).pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (newFY: FiscalYearDTO) => {
          this.fiscalYears.push(newFY);
          this.resetFYForm();
          this.showCreateFYForm = false;
          this.isCreatingFY = false;
          this.onFYSelected(newFY);
        },
        error: (error: any) => {
          this.errorMessage = error.error?.message || 'Failed to create FY';
          this.isCreatingFY = false;
        }
      });
  }

  private resetRCForm(): void {
    this.newRCName = '';
    this.newRCDescription = '';
    this.errorMessage = null;
  }

  private resetFYForm(): void {
    this.newFYName = '';
    this.errorMessage = null;
  }

  getAccessLevelLabel(accessLevel: string): string {
    return accessLevel === 'READ_WRITE' ? 'Read & Write' : 'Read Only';
  }

  getAccessLevelClass(accessLevel: string): string {
    return accessLevel === 'READ_WRITE' ? 'access-readwrite' : 'access-readonly';
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
