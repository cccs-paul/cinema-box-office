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
        },
        error: (error: any) => {
          this.errorMessage = 'Failed to load responsibility centres';
          this.isLoading = false;
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
          this.errorMessage = error.error?.message || 'Failed to create RC';
          this.isCreating = false;
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
