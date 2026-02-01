/*
 * myRC - Sidebar Component
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { Subject, combineLatest } from 'rxjs';
import { takeUntil, switchMap, filter } from 'rxjs/operators';
import { ResponsibilityCentreService } from '../../services/responsibility-centre.service';
import { FiscalYearService } from '../../services/fiscal-year.service';
import { ResponsibilityCentreDTO } from '../../models/responsibility-centre.model';
import { FiscalYear } from '../../models/fiscal-year.model';

/**
 * Sidebar navigation component for authenticated pages.
 * Provides main navigation menu for the application.
 * Displays currently selected RC and FY context.
 *
 * @author myRC Team
 * @version 1.1.0
 * @since 2026-01-21
 */
@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, TranslateModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss'],
})
export class SidebarComponent implements OnInit, OnDestroy {
  menuItems = [
    {
      labelKey: 'sidebar.funding',
      icon: '📊',
      route: '/app/dashboard',
      badge: null,
    },
    {
      labelKey: 'sidebar.procurement',
      icon: '📦',
      route: '/app/procurement',
      badge: null,
    },
    {
      labelKey: 'sidebar.spending',
      icon: '💰',
      route: '/app/spending',
      badge: null,
    },
    {
      labelKey: 'sidebar.insights',
      icon: '📈',
      route: '/app/insights',
      badge: null,
    },
    {
      labelKey: 'sidebar.summary',
      icon: '📋',
      route: '/app/summary',
      badge: null,
    },
  ];

  bottomMenuItems = [
    {
      labelKey: 'sidebar.configuration',
      icon: '⚙️',
      route: '/app/configuration',
      badge: null,
    },
    {
      labelKey: 'sidebar.developerTools',
      icon: '🛠️',
      route: '/app/developer-tools',
      badge: null,
    },
  ];

  selectedRC: ResponsibilityCentreDTO | null = null;
  selectedFY: FiscalYear | null = null;
  private destroy$ = new Subject<void>();

  constructor(
    private rcService: ResponsibilityCentreService,
    private fyService: FiscalYearService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Subscribe to RC changes
    this.rcService.selectedRC$
      .pipe(
        takeUntil(this.destroy$),
        filter((rcId): rcId is number => rcId !== null),
        switchMap(rcId => this.rcService.getResponsibilityCentre(rcId))
      )
      .subscribe({
        next: (rc) => {
          this.selectedRC = rc;
        },
        error: () => {
          this.selectedRC = null;
        }
      });

    // Subscribe to combined RC and FY changes for FY loading
    combineLatest([this.rcService.selectedRC$, this.rcService.selectedFY$])
      .pipe(
        takeUntil(this.destroy$),
        filter(([rcId, fyId]) => rcId !== null && fyId !== null)
      )
      .subscribe(([rcId, fyId]) => {
        if (rcId && fyId) {
          this.fyService.getFiscalYear(rcId, fyId).subscribe({
            next: (fy) => {
              this.selectedFY = fy;
            },
            error: () => {
              this.selectedFY = null;
            }
          });
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Navigate to RC selection page.
   */
  navigateToRCSelection(): void {
    this.router.navigate(['/rc-selection']);
  }

  /**
   * Navigate to configuration page.
   */
  navigateToConfiguration(): void {
    this.router.navigate(['/app/configuration']);
  }
}
