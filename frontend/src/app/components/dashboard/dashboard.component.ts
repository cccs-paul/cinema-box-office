/*
 * Cinema Box Office - Dashboard Component
 * Copyright (c) 2026 Box Office Team
 * Licensed under MIT License
 */
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ResponsibilityCentreService } from '../../services/responsibility-centre.service';
import { FiscalYearService } from '../../services/fiscal-year.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DashboardComponent implements OnInit, OnDestroy {
  title = 'Cinema Box Office';
  selectedRCName = '';
  selectedFYName = '';
  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private rcService: ResponsibilityCentreService,
    private fyService: FiscalYearService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Check authentication
    this.authService.currentUser$.pipe(takeUntil(this.destroy$)).subscribe((user) => {
      if (!user) {
        this.router.navigate(['/login']);
      }
    });

    // Get selection details
    const rcId = this.rcService.getSelectedRC();
    const fyId = this.fyService.getSelectedFY();

    if (!rcId || !fyId) {
      this.router.navigate(['/rc-selection']);
      return;
    }

    // Fetch names for display
    this.rcService.getResponsibilityCentre(rcId).subscribe({
      next: (rc) => this.selectedRCName = rc.name,
      error: () => this.selectedRCName = 'Unknown RC'
    });

    // For now we don't have a getById for FY, so we find it in the list
    this.fyService.getAllForRc(rcId).subscribe({
      next: (fys) => {
        const fy = fys.find(f => f.id === fyId);
        this.selectedFYName = fy ? fy.name : 'Unknown FY';
      },
      error: () => this.selectedFYName = 'Unknown FY'
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  changeSelection(): void {
    this.fyService.clearSelection();
    this.router.navigate(['/rc-selection']);
  }
}
