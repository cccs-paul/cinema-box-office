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
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ResponsibilityCentreService } from '../../services/responsibility-centre.service';
import { ResponsibilityCentreDTO } from '../../models/responsibility-centre.model';
import { AuditService } from '../../services/audit.service';
import { AuditEvent } from '../../models/audit-event.model';
import { RCPermissionsComponent } from '../rc-permissions/rc-permissions.component';
import { TrainingItemService } from '../../services/training-item.service';
import { TravelItemService } from '../../services/travel-item.service';
import { FiscalYearService } from '../../services/fiscal-year.service';

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
  imports: [CommonModule, FormsModule, TranslateModule, RCPermissionsComponent],
  templateUrl: './rc-configuration.component.html',
  styleUrls: ['./rc-configuration.component.scss']
})
export class RCConfigurationComponent implements OnInit, OnDestroy {
  /** The RC ID from the route parameter. */
  rcId: number | null = null;

  /** The loaded RC details. */
  rc: ResponsibilityCentreDTO | null = null;

  /** The currently active tab. */
  activeTab: 'permissions' | 'audit' | 'training' | 'travel' = 'permissions';

  /** Whether the RC is loading. */
  isLoading = true;

  /** Error message to display. */
  errorMessage: string | null = null;

  /** Audit events for the RC. */
  auditEvents: AuditEvent[] = [];

  /** Whether audit events are loading. */
  auditLoading = false;

  /** Error message for audit tab. */
  auditError: string | null = null;

  /** Whether the current user is the RC owner (needed for audit access). */
  isOwner = false;

  /** The expanded audit event ID (for showing details). */
  expandedAuditId: number | null = null;

  /** Whether training items exist (prevents toggle-off). */
  hasTrainingItems = false;
  checkingTrainingItems = false;

  /** Whether travel items exist (prevents toggle-off). */
  hasTravelItems = false;
  checkingTravelItems = false;

  /** Toggle operation in progress. */
  togglingTraining = false;
  togglingTravel = false;

  /** Success message. */
  successMessage: string | null = null;

  private destroy$ = new Subject<void>();

  constructor(
    private rcService: ResponsibilityCentreService,
    private auditService: AuditService,
    private trainingItemService: TrainingItemService,
    private travelItemService: TravelItemService,
    private fiscalYearService: FiscalYearService,
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
        this.isOwner = rc.isOwner === true;
        this.isLoading = false;
        this.checkForExistingItems();
      },
      error: () => {
        this.errorMessage = this.translate.instant('rcConfiguration.loadError');
        this.isLoading = false;
      }
    });
  }

  /**
   * Check if training or travel items exist in any FY of this RC.
   * This is needed to prevent disabling features that have data.
   */
  private checkForExistingItems(): void {
    if (!this.rcId) return;

    this.checkingTrainingItems = true;
    this.checkingTravelItems = true;

    this.fiscalYearService.getFiscalYearsByRC(this.rcId).subscribe({
      next: (fiscalYears: any[]) => {
        if (fiscalYears.length === 0) {
          this.hasTrainingItems = false;
          this.hasTravelItems = false;
          this.checkingTrainingItems = false;
          this.checkingTravelItems = false;
          return;
        }

        let trainingChecked = 0;
        let travelChecked = 0;
        let foundTraining = false;
        let foundTravel = false;

        for (const fy of fiscalYears) {
          this.trainingItemService.getTrainingItemsByFY(this.rcId!, fy.id).subscribe({
            next: (items) => {
              if (items.length > 0) foundTraining = true;
              trainingChecked++;
              if (trainingChecked === fiscalYears.length) {
                this.hasTrainingItems = foundTraining;
                this.checkingTrainingItems = false;
              }
            },
            error: () => {
              trainingChecked++;
              if (trainingChecked === fiscalYears.length) {
                this.hasTrainingItems = foundTraining;
                this.checkingTrainingItems = false;
              }
            }
          });

          this.travelItemService.getTravelItemsByFY(this.rcId!, fy.id).subscribe({
            next: (items) => {
              if (items.length > 0) foundTravel = true;
              travelChecked++;
              if (travelChecked === fiscalYears.length) {
                this.hasTravelItems = foundTravel;
                this.checkingTravelItems = false;
              }
            },
            error: () => {
              travelChecked++;
              if (travelChecked === fiscalYears.length) {
                this.hasTravelItems = foundTravel;
                this.checkingTravelItems = false;
              }
            }
          });
        }
      },
      error: () => {
        this.checkingTrainingItems = false;
        this.checkingTravelItems = false;
      }
    });
  }

  /**
   * Switch to a configuration tab.
   *
   * @param tab the tab to activate
   */
  setActiveTab(tab: 'permissions' | 'audit' | 'training' | 'travel'): void {
    this.activeTab = tab;
    if (tab === 'audit' && this.isOwner && this.auditEvents.length === 0) {
      this.loadAuditEvents();
    }
  }

  /**
   * Toggle training enabled for this RC.
   */
  toggleTrainingEnabled(): void {
    if (!this.rcId || !this.rc || !this.isOwner) return;

    const newValue = !this.rc.trainingEnabled;

    // Prevent turning off if items exist
    if (!newValue && this.hasTrainingItems) {
      this.errorMessage = this.translate.instant('rcConfiguration.cannotDisableTraining');
      return;
    }

    this.togglingTraining = true;
    this.errorMessage = null;

    this.rcService.setTrainingEnabled(this.rcId, newValue).subscribe({
      next: (updatedRc) => {
        this.rc = updatedRc;
        this.togglingTraining = false;
        this.successMessage = newValue
          ? this.translate.instant('rcConfiguration.trainingEnabled')
          : this.translate.instant('rcConfiguration.trainingDisabled');
        this.autoClearSuccess();
      },
      error: (err) => {
        this.errorMessage = err.message || 'Failed to update training setting';
        this.togglingTraining = false;
      }
    });
  }

  /**
   * Toggle travel enabled for this RC.
   */
  toggleTravelEnabled(): void {
    if (!this.rcId || !this.rc || !this.isOwner) return;

    const newValue = !this.rc.travelEnabled;

    // Prevent turning off if items exist
    if (!newValue && this.hasTravelItems) {
      this.errorMessage = this.translate.instant('rcConfiguration.cannotDisableTravel');
      return;
    }

    this.togglingTravel = true;
    this.errorMessage = null;

    this.rcService.setTravelEnabled(this.rcId, newValue).subscribe({
      next: (updatedRc) => {
        this.rc = updatedRc;
        this.togglingTravel = false;
        this.successMessage = newValue
          ? this.translate.instant('rcConfiguration.travelEnabled')
          : this.translate.instant('rcConfiguration.travelDisabled');
        this.autoClearSuccess();
      },
      error: (err) => {
        this.errorMessage = err.message || 'Failed to update travel setting';
        this.togglingTravel = false;
      }
    });
  }

  private autoClearSuccess(): void {
    setTimeout(() => {
      this.successMessage = null;
    }, 3000);
  }

  /**
   * Load audit events for the RC.
   */
  loadAuditEvents(): void {
    if (!this.rcId) return;

    this.auditLoading = true;
    this.auditError = null;

    this.auditService.getAuditEventsForRC(this.rcId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (events) => {
          this.auditEvents = events;
          this.auditLoading = false;
        },
        error: () => {
          this.auditError = this.translate.instant('rcConfiguration.auditLoadError');
          this.auditLoading = false;
        }
      });
  }

  /**
   * Toggle the expanded state of an audit event row.
   *
   * @param eventId the audit event ID
   */
  toggleAuditDetails(eventId: number): void {
    this.expandedAuditId = this.expandedAuditId === eventId ? null : eventId;
  }

  /**
   * Navigate back to the RC Selection page.
   */
  goBack(): void {
    this.router.navigate(['/rc-selection']);
  }
}
