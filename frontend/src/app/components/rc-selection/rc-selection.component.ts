/*
 * Cinema Box Office - RC Selection Component
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

@Component({
  selector: 'app-rc-selection',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './rc-selection.component.html',
  styleUrls: ['./rc-selection.component.scss']
})
export class RCSelectionComponent implements OnInit, OnDestroy {
  responsibilityCentres: ResponsibilityCentreDTO[] = [];
  isLoading = true;
  errorMessage: string | null = null;
  newRCName = '';
  newRCDescription = '';
  showCreateForm = false;
  isCreating = false;
  private destroy$ = new Subject<void>();

  constructor(
    private rcService: ResponsibilityCentreService,
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
          // If empty, don't show error - the empty state UI will handle it
          this.errorMessage = null;
        },
        error: (error: any) => {
          this.isLoading = false;
          // Handle authentication errors separately
          if (error.status === 401) {
            this.errorMessage = 'Your session has expired. Please log in again.';
          } else if (error.status === 403) {
            this.errorMessage = 'You do not have permission to view responsibility centres.';
          } else if (error.status === 404) {
            // 404 means no RCs found - this is not an error, show empty state
            this.responsibilityCentres = [];
            this.errorMessage = null;
          } else {
            this.errorMessage = 'Failed to load responsibility centres. Please try again later.';
          }
        }
      });
  }

  selectRC(rc: ResponsibilityCentreDTO): void {
    this.rcService.setSelectedRC(rc.id);
    this.router.navigate(['/dashboard']);
  }

  toggleCreateForm(): void {
    this.showCreateForm = !this.showCreateForm;
    if (!this.showCreateForm) {
      this.resetForm();
    }
  }

  createRC(): void {
    if (!this.newRCName.trim()) {
      this.errorMessage = 'RC name is required';
      return;
    }

    this.isCreating = true;
    this.errorMessage = null;
    this.rcService.createResponsibilityCentre(
      this.newRCName,
      this.newRCDescription
    ).pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (newRC: ResponsibilityCentreDTO) => {
          this.responsibilityCentres.push(newRC);
          this.resetForm();
          this.showCreateForm = false;
          this.isCreating = false;
          this.selectRC(newRC);
        },
        error: (error: any) => {
          this.isCreating = false;
          // Provide more specific error messages
          if (error.status === 401) {
            this.errorMessage = 'Your session has expired. Please log in again.';
          } else if (error.status === 400) {
            this.errorMessage = error.error?.message || 'Invalid RC details. Please check your input.';
          } else if (error.status === 404) {
            this.errorMessage = 'The API endpoint is not available. Please contact support.';
          } else if (error.status === 500) {
            this.errorMessage = 'Server error occurred while creating RC. Please try again later.';
          } else {
            this.errorMessage = error.error?.message || 'Failed to create responsibility centre. Please try again.';
          }
        }
      });
  }

  private resetForm(): void {
    this.newRCName = '';
    this.newRCDescription = '';
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
