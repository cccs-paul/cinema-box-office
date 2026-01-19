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
    if (!this.showCreateForm) {
      this.resetRCForm();
    }
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
    if (!this.showCreateFYForm) {
      this.resetFYForm();
    }
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
