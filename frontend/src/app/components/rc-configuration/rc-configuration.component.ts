/*
 * myRC - RC Configuration Component
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-02-08
 * Version: 1.0.0
 *
 * Description:
 * RC-level configuration page with tabbed interface.
 * Currently provides a Permissions tab for managing RC access.
 * Accessible from the RC Selection page for RC owners.
 */
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ResponsibilityCentreService } from '../../services/responsibility-centre.service';
import { ResponsibilityCentreDTO } from '../../models/responsibility-centre.model';
import { RCPermissionsComponent } from '../rc-permissions/rc-permissions.component';

/**
 * RC Configuration Component.
 * Provides a tabbed interface for RC-level settings.
 * Currently includes a Permissions tab that embeds the RC Permissions component.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-08
 */
@Component({
  selector: 'app-rc-configuration',
  standalone: true,
  imports: [CommonModule, TranslateModule, RCPermissionsComponent],
  templateUrl: './rc-configuration.component.html',
  styleUrls: ['./rc-configuration.component.scss']
})
export class RCConfigurationComponent implements OnInit, OnDestroy {
  /** The RC ID from the route parameter. */
  rcId: number | null = null;

  /** The loaded RC details. */
  rc: ResponsibilityCentreDTO | null = null;

  /** The currently active tab. */
  activeTab: 'permissions' = 'permissions';

  /** Whether the RC is loading. */
  isLoading = true;

  /** Error message to display. */
  errorMessage: string | null = null;

  private destroy$ = new Subject<void>();

  constructor(
    private rcService: ResponsibilityCentreService,
    private router: Router,
    private route: ActivatedRoute,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    this.route.params.pipe(takeUntil(this.destroy$)).subscribe(params => {
      const id = params['rcId'];
      if (id) {
        this.rcId = parseInt(id, 10);
        this.loadRC();
      } else {
        this.router.navigate(['/rc-selection']);
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Load the RC details.
   */
  loadRC(): void {
    if (!this.rcId) return;

    this.isLoading = true;
    this.errorMessage = null;

    this.rcService.getResponsibilityCentre(this.rcId).subscribe({
      next: (rc) => {
        this.rc = rc;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = this.translate.instant('rcConfiguration.loadError');
        this.isLoading = false;
      }
    });
  }

  /**
   * Switch to a configuration tab.
   *
   * @param tab the tab to activate
   */
  setActiveTab(tab: 'permissions'): void {
    this.activeTab = tab;
  }

  /**
   * Navigate back to the RC Selection page.
   */
  goBack(): void {
    this.router.navigate(['/rc-selection']);
  }
}
