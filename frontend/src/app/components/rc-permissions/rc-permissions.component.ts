/*
 * myRC - RC Permissions Component
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */

import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { RCPermissionService, RCAccess, GrantUserAccessRequest, GrantGroupAccessRequest } from '../../services/rc-permission.service';
import { ResponsibilityCentreService } from '../../services/responsibility-centre.service';
import { ResponsibilityCentreDTO } from '../../models/responsibility-centre.model';

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
  imports: [CommonModule, FormsModule],
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
  grantFormType: 'USER' | 'GROUP' | 'DISTRIBUTION_LIST' = 'USER';
  newPrincipalIdentifier = '';
  newPrincipalDisplayName = '';
  newAccessLevel: 'OWNER' | 'READ_WRITE' | 'READ_ONLY' = 'READ_WRITE';
  isGranting = false;

  // Edit form
  editingPermissionId: number | null = null;
  editAccessLevel: 'OWNER' | 'READ_WRITE' | 'READ_ONLY' = 'READ_WRITE';

  // Delete confirmation
  deleteConfirmId: number | null = null;

  private destroy$ = new Subject<void>();

  constructor(
    private permissionService: RCPermissionService,
    private rcService: ResponsibilityCentreService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Get RC ID from route params
    this.route.params.pipe(takeUntil(this.destroy$)).subscribe(params => {
      const id = params['rcId'];
      if (id) {
        this.rcId = parseInt(id, 10);
        this.loadRC();
        this.loadPermissions();
      } else {
        // Try to get from the stored selection
        this.rcService.selectedRC$.pipe(takeUntil(this.destroy$)).subscribe(selectedId => {
          if (selectedId) {
            this.rcId = selectedId;
            this.loadRC();
            this.loadPermissions();
          } else {
            this.router.navigate(['/rc-selection']);
          }
        });
      }
    });
  }

  ngOnDestroy(): void {
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
          this.errorMessage = 'You do not have permission to manage this RC\'s permissions.';
        }
      },
      error: (error) => {
        this.errorMessage = 'Failed to load RC information.';
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
        this.errorMessage = error.message || 'Failed to load permissions.';
        this.isLoading = false;
      }
    });
  }

  // Grant form methods
  openGrantForm(type: 'USER' | 'GROUP' | 'DISTRIBUTION_LIST' = 'USER'): void {
    this.showGrantForm = true;
    this.grantFormType = type;
    this.newPrincipalIdentifier = '';
    this.newPrincipalDisplayName = '';
    this.newAccessLevel = 'READ_WRITE';
    this.clearMessages();
  }

  closeGrantForm(): void {
    this.showGrantForm = false;
    this.newPrincipalIdentifier = '';
    this.newPrincipalDisplayName = '';
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
          this.successMessage = `Access granted to ${this.newPrincipalIdentifier}`;
          this.closeGrantForm();
          this.loadPermissions();
          this.isGranting = false;
        },
        error: (error) => {
          this.errorMessage = error.message || 'Failed to grant access.';
          this.isGranting = false;
        }
      });
    } else {
      const request: GrantGroupAccessRequest = {
        principalIdentifier: this.newPrincipalIdentifier.trim(),
        principalDisplayName: this.newPrincipalDisplayName.trim() || this.newPrincipalIdentifier.trim(),
        principalType: this.grantFormType,
        accessLevel: this.newAccessLevel
      };

      this.permissionService.grantGroupAccess(this.rcId, request).subscribe({
        next: () => {
          this.successMessage = `Access granted to ${this.newPrincipalIdentifier}`;
          this.closeGrantForm();
          this.loadPermissions();
          this.isGranting = false;
        },
        error: (error) => {
          this.errorMessage = error.message || 'Failed to grant access.';
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

    this.permissionService.updatePermission(this.editingPermissionId, { accessLevel: this.editAccessLevel }).subscribe({
      next: () => {
        this.successMessage = 'Permission updated successfully.';
        this.editingPermissionId = null;
        this.loadPermissions();
      },
      error: (error) => {
        this.errorMessage = error.message || 'Failed to update permission.';
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

    this.permissionService.revokeAccess(this.deleteConfirmId).subscribe({
      next: () => {
        this.successMessage = 'Access revoked successfully.';
        this.deleteConfirmId = null;
        this.loadPermissions();
      },
      error: (error) => {
        this.errorMessage = error.message || 'Failed to revoke access.';
        this.deleteConfirmId = null;
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
        return 'Security Group';
      case 'DISTRIBUTION_LIST':
        return 'Distribution List';
      default:
        return '';
    }
  }

  getIdentifierPlaceholder(): string {
    switch (this.grantFormType) {
      case 'USER':
        return 'Enter username (e.g., jsmith)';
      case 'GROUP':
        return 'Enter LDAP group name (e.g., CN=Finance-Team,OU=Groups,DC=corp,DC=local)';
      case 'DISTRIBUTION_LIST':
        return 'Enter distribution list (e.g., finance-team@company.com)';
      default:
        return '';
    }
  }

  clearMessages(): void {
    this.errorMessage = null;
    this.successMessage = null;
  }

  goBack(): void {
    this.router.navigate(['/rc-selection']);
  }
}
