/*
 * myRC - RC Permissions Component
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */

import { Component, OnInit, OnDestroy, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { Subject, Subscription } from 'rxjs';
import { takeUntil, debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { RCPermissionService, RCAccess, GrantUserAccessRequest, GrantGroupAccessRequest } from '../../services/rc-permission.service';
import { ResponsibilityCentreService } from '../../services/responsibility-centre.service';
import { ResponsibilityCentreDTO } from '../../models/responsibility-centre.model';
import { DirectorySearchService, DirectorySearchResult } from '../../services/directory-search.service';

/**
 * RC Permissions Configuration Component.
 * Allows RC owners to grant, update, and revoke access permissions.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-29
 */
@Component({
  selector: 'app-rc-permissions',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './rc-permissions.component.html',
  styleUrls: ['./rc-permissions.component.scss']
})
export class RCPermissionsComponent implements OnInit, OnDestroy {
  // RC Info
  rcId: number | null = null;
  rc: ResponsibilityCentreDTO | null = null;

  // Permissions
  permissions: RCAccess[] = [];
  isLoading = true;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  // Grant access form
  showGrantForm = false;
  grantFormType: 'USER' | 'GROUP' = 'USER';
  newPrincipalIdentifier = '';
  newPrincipalDisplayName = '';
  newAccessLevel: 'OWNER' | 'READ_WRITE' | 'READ_ONLY' = 'READ_WRITE';
  isGranting = false;

  // Autocomplete
  suggestions: DirectorySearchResult[] = [];
  showSuggestions = false;
  isSearching = false;
  activeSuggestionIndex = -1;
  private searchSubject$ = new Subject<string>();
  private searchSubscription: Subscription | null = null;

  // Edit form
  editingPermissionId: number | null = null;
  editAccessLevel: 'OWNER' | 'READ_WRITE' | 'READ_ONLY' = 'READ_WRITE';
  isSaving = false;

  // Delete confirmation
  deleteConfirmId: number | null = null;
  isDeleting = false;

  private destroy$ = new Subject<void>();

  constructor(
    private permissionService: RCPermissionService,
    private rcService: ResponsibilityCentreService,
    private directorySearchService: DirectorySearchService,
    private router: Router,
    private route: ActivatedRoute,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    // Set up autocomplete search pipeline
    this.initSearchPipeline();

    // Get RC ID from route params
    this.route.params.pipe(takeUntil(this.destroy$)).subscribe(params => {
      const id = params['rcId'];
      if (id) {
        this.rcId = parseInt(id, 10);
        this.loadRC();
      } else {
        // Try to get from the stored selection
        this.rcService.selectedRC$.pipe(takeUntil(this.destroy$)).subscribe(selectedId => {
          if (selectedId) {
            this.rcId = selectedId;
            this.loadRC();
          } else {
            this.router.navigate(['/rc-selection']);
          }
        });
      }
    });
  }

  ngOnDestroy(): void {
    if (this.searchSubscription) {
      this.searchSubscription.unsubscribe();
    }
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadRC(): void {
    if (!this.rcId) return;

    this.rcService.getResponsibilityCentre(this.rcId).subscribe({
      next: (rc) => {
        this.rc = rc;
        // Check if user is owner
        if (!rc.isOwner) {
          this.errorMessage = this.translate.instant('rcPermissions.noPermissionToManage');
        } else {
          // Only load permissions if user is owner
          this.loadPermissions();
        }
      },
      error: (error) => {
        this.errorMessage = this.translate.instant('rcPermissions.loadRCError');
        this.isLoading = false;
        console.error('Error loading RC:', error);
      }
    });
  }

  loadPermissions(): void {
    if (!this.rcId) return;

    this.isLoading = true;
    this.errorMessage = null;

    this.permissionService.getPermissionsForRC(this.rcId).subscribe({
      next: (permissions) => {
        this.permissions = permissions;
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = error.message || this.translate.instant('rcPermissions.loadPermissionsError');
        this.isLoading = false;
      }
    });
  }

  // Grant form methods
  openGrantForm(type: 'USER' | 'GROUP' = 'USER'): void {
    this.showGrantForm = true;
    this.grantFormType = type;
    this.newPrincipalIdentifier = '';
    this.newPrincipalDisplayName = '';
    this.newAccessLevel = 'READ_WRITE';
    this.closeSuggestions();
    this.clearMessages();
  }

  closeGrantForm(): void {
    this.showGrantForm = false;
    this.newPrincipalIdentifier = '';
    this.newPrincipalDisplayName = '';
    this.closeSuggestions();
  }

  /**
   * Initialize the debounced search pipeline for autocomplete.
   */
  private initSearchPipeline(): void {
    this.searchSubscription = this.searchSubject$.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(query => {
        this.isSearching = true;
        const trimmedQuery = query != null ? query.trim() : '';
        if (this.grantFormType === 'USER') {
          return this.directorySearchService.searchUsers(trimmedQuery);
        } else if (this.grantFormType === 'GROUP') {
          return this.directorySearchService.searchAllGroups(trimmedQuery);
        }
        return [];
      })
    ).subscribe(results => {
      this.suggestions = results;
      this.showSuggestions = results.length > 0;
      this.isSearching = false;
      this.activeSuggestionIndex = -1;
    });
  }

  /**
   * Called when the identifier input value changes.
   * Triggers the debounced autocomplete search.
   */
  onIdentifierInput(): void {
    this.searchSubject$.next(this.newPrincipalIdentifier);
  }

  /**
   * Select a suggestion from the autocomplete dropdown.
   */
  selectSuggestion(suggestion: DirectorySearchResult): void {
    this.newPrincipalIdentifier = suggestion.identifier;
    if (this.grantFormType !== 'USER' && suggestion.displayName) {
      this.newPrincipalDisplayName = suggestion.displayName;
    }
    this.closeSuggestions();
  }

  /**
   * Close the autocomplete suggestions dropdown.
   */
  closeSuggestions(): void {
    this.suggestions = [];
    this.showSuggestions = false;
    this.isSearching = false;
    this.activeSuggestionIndex = -1;
  }

  /**
   * Handle keyboard navigation in the autocomplete dropdown.
   * When Enter or ArrowDown is pressed with no suggestions showing,
   * triggers a browse-all search to display available entries.
   */
  onSuggestionKeydown(event: KeyboardEvent): void {
    // If suggestions are not showing, Enter or ArrowDown triggers browse-all
    if (!this.showSuggestions || this.suggestions.length === 0) {
      if (event.key === 'Enter' || event.key === 'ArrowDown') {
        event.preventDefault();
        this.triggerBrowseAll();
      }
      return;
    }

    switch (event.key) {
      case 'ArrowDown':
        event.preventDefault();
        this.activeSuggestionIndex = Math.min(
          this.activeSuggestionIndex + 1,
          this.suggestions.length - 1
        );
        break;
      case 'ArrowUp':
        event.preventDefault();
        this.activeSuggestionIndex = Math.max(this.activeSuggestionIndex - 1, -1);
        break;
      case 'Enter':
        event.preventDefault();
        if (this.activeSuggestionIndex >= 0 && this.activeSuggestionIndex < this.suggestions.length) {
          this.selectSuggestion(this.suggestions[this.activeSuggestionIndex]);
        }
        break;
      case 'Escape':
        event.preventDefault();
        this.closeSuggestions();
        break;
    }
  }

  /**
   * Trigger a browse-all search to display all available entries.
   * Called when the user presses Enter or ArrowDown with no suggestions showing.
   */
  triggerBrowseAll(): void {
    this.searchSubject$.next(this.newPrincipalIdentifier);
  }

  /**
   * Get the source label for a suggestion (translated).
   */
  getSuggestionSourceLabel(source: string): string {
    switch (source) {
      case 'APP':
        return this.translate.instant('rcPermissions.sourceApp');
      case 'LDAP':
        return this.translate.instant('rcPermissions.sourceLdap');
      default:
        return source;
    }
  }

  grantAccess(): void {
    if (!this.rcId || !this.newPrincipalIdentifier.trim()) return;

    this.isGranting = true;
    this.clearMessages();

    if (this.grantFormType === 'USER') {
      const request: GrantUserAccessRequest = {
        username: this.newPrincipalIdentifier.trim(),
        accessLevel: this.newAccessLevel
      };

      this.permissionService.grantUserAccess(this.rcId, request).subscribe({
        next: () => {
          this.showSuccessMessage(this.translate.instant('rcPermissions.accessGrantedSuccess', { principal: this.newPrincipalIdentifier }));
          this.closeGrantForm();
          this.loadPermissions();
          this.isGranting = false;
        },
        error: (error) => {
          this.errorMessage = error.message || this.translate.instant('rcPermissions.accessGrantedError');
          this.isGranting = false;
        }
      });
    } else {
      const request: GrantGroupAccessRequest = {
        principalIdentifier: this.newPrincipalIdentifier.trim(),
        principalDisplayName: this.newPrincipalDisplayName.trim() || this.newPrincipalIdentifier.trim(),
        principalType: 'GROUP',
        accessLevel: this.newAccessLevel
      };

      this.permissionService.grantGroupAccess(this.rcId, request).subscribe({
        next: () => {
          this.showSuccessMessage(this.translate.instant('rcPermissions.accessGrantedSuccess', { principal: this.newPrincipalIdentifier }));
          this.closeGrantForm();
          this.loadPermissions();
          this.isGranting = false;
        },
        error: (error) => {
          this.errorMessage = error.message || this.translate.instant('rcPermissions.accessGrantedError');
          this.isGranting = false;
        }
      });
    }
  }

  // Edit methods
  startEdit(permission: RCAccess): void {
    if (!permission.id) return; // Can't edit the original owner
    this.editingPermissionId = permission.id;
    this.editAccessLevel = permission.accessLevel;
    this.clearMessages();
  }

  cancelEdit(): void {
    this.editingPermissionId = null;
  }

  saveEdit(): void {
    if (!this.editingPermissionId) return;

    this.isSaving = true;
    this.permissionService.updatePermission(this.editingPermissionId, { accessLevel: this.editAccessLevel }).subscribe({
      next: () => {
        this.showSuccessMessage(this.translate.instant('rcPermissions.permissionUpdatedSuccess'));
        this.editingPermissionId = null;
        this.isSaving = false;
        this.loadPermissions();
      },
      error: (error) => {
        this.errorMessage = error.message || this.translate.instant('rcPermissions.permissionUpdatedError');
        this.isSaving = false;
      }
    });
  }

  // Delete methods
  confirmDelete(permission: RCAccess): void {
    if (!permission.id) return; // Can't delete the original owner
    this.deleteConfirmId = permission.id;
    this.clearMessages();
  }

  cancelDelete(): void {
    this.deleteConfirmId = null;
  }

  deletePermission(): void {
    if (!this.deleteConfirmId) return;

    this.isDeleting = true;
    this.permissionService.revokeAccess(this.deleteConfirmId).subscribe({
      next: () => {
        this.showSuccessMessage(this.translate.instant('rcPermissions.accessRevokedSuccess'));
        this.deleteConfirmId = null;
        this.isDeleting = false;
        this.loadPermissions();
      },
      error: (error) => {
        this.errorMessage = error.message || this.translate.instant('rcPermissions.accessRevokedError');
        this.deleteConfirmId = null;
        this.isDeleting = false;
      }
    });
  }

  // Helper methods
  getAccessLevelLabel(level: string): string {
    return this.permissionService.getAccessLevelLabel(level);
  }

  getPrincipalTypeLabel(type: string): string {
    return this.permissionService.getPrincipalTypeLabel(type);
  }

  getPrincipalTypeIcon(type: string): string {
    return this.permissionService.getPrincipalTypeIcon(type);
  }

  getAccessLevelIcon(level: string): string {
    return this.permissionService.getAccessLevelIcon(level);
  }

  canEditPermission(permission: RCAccess): boolean {
    // Can't edit original owner (id is null) or if user is the RC's original owner
    return permission.id !== null && 
           !(this.rc && permission.principalIdentifier === this.rc.ownerUsername);
  }

  getFormTypeLabel(): string {
    switch (this.grantFormType) {
      case 'USER':
        return 'User';
      case 'GROUP':
        return 'Group';
      default:
        return '';
    }
  }

  getIdentifierPlaceholder(): string {
    switch (this.grantFormType) {
      case 'USER':
        return 'Enter username (e.g., jsmith)';
      case 'GROUP':
        return 'Enter group name (e.g., Finance-Team)';
      default:
        return '';
    }
  }

  /**
   * Get translated form type label.
   */
  getFormTypeLabelTranslated(): string {
    switch (this.grantFormType) {
      case 'USER':
        return this.translate.instant('rcPermissions.grantUserAccess');
      case 'GROUP':
        return this.translate.instant('rcPermissions.grantGroupAccess');
      default:
        return '';
    }
  }

  /**
   * Get translated identifier placeholder.
   */
  getIdentifierPlaceholderTranslated(): string {
    switch (this.grantFormType) {
      case 'USER':
        return this.translate.instant('rcPermissions.enterUsername');
      case 'GROUP':
        return this.translate.instant('rcPermissions.enterGroupId');
      default:
        return '';
    }
  }

  /**
   * Get translated principal type label.
   */
  getPrincipalTypeLabelTranslated(type: string): string {
    switch (type) {
      case 'USER':
        return this.translate.instant('rcPermissions.user');
      case 'GROUP':
      case 'DISTRIBUTION_LIST':
        return this.translate.instant('rcPermissions.group');
      default:
        return type;
    }
  }

  /**
   * Get translated access level label.
   */
  getAccessLevelLabelTranslated(level: string): string {
    switch (level) {
      case 'OWNER':
        return this.translate.instant('rcPermissions.owner');
      case 'READ_WRITE':
        return this.translate.instant('rcPermissions.readWrite');
      case 'READ_ONLY':
        return this.translate.instant('rcPermissions.readOnly');
      default:
        return level;
    }
  }

  /**
   * Show a success message and auto-clear it after 5 seconds.
   */
  showSuccessMessage(message: string): void {
    this.successMessage = message;
    setTimeout(() => {
      if (this.successMessage === message) {
        this.successMessage = null;
      }
    }, 5000);
  }

  clearMessages(): void {
    this.errorMessage = null;
    this.successMessage = null;
  }

  goBack(): void {
    this.router.navigate(['/rc-selection']);
  }
}
