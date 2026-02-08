/*
 * myRC - RC Permission Controller
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.controller;

import com.myrc.config.LdapSecurityConfig;
import com.myrc.dto.RCAccessDTO;
import com.myrc.model.RCAccess.AccessLevel;
import com.myrc.model.RCAccess.PrincipalType;
import com.myrc.service.RCPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.logging.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for managing RC permissions.
 * Provides endpoints for granting, updating, and revoking access to RCs.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-29
 */
@RestController
@RequestMapping("/rc-permissions")
@Tag(name = "RC Permission Management", description = "APIs for managing RC access permissions")
public class RCPermissionController {

  private static final Logger logger = Logger.getLogger(RCPermissionController.class.getName());
  private final RCPermissionService permissionService;

  public RCPermissionController(RCPermissionService permissionService) {
    this.permissionService = permissionService;
  }

  /**
   * Get all permissions for a specific RC.
   */
  @GetMapping("/rc/{rcId}")
  @Operation(summary = "Get RC permissions", 
      description = "Retrieves all permission entries for a specific RC. Only owners can view permissions.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Permissions retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Forbidden - not an owner"),
      @ApiResponse(responseCode = "404", description = "RC not found")
  })
  public ResponseEntity<?> getPermissionsForRC(
      @Parameter(description = "RC ID") @PathVariable Long rcId,
      Authentication authentication) {
    if (authentication == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    try {
      List<RCAccessDTO> permissions = permissionService.getPermissionsForRC(rcId, authentication.getName());
      return ResponseEntity.ok(permissions);
    } catch (SecurityException e) {
      logger.warning("Access denied for " + authentication.getName() + ": " + e.getMessage());
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    } catch (IllegalArgumentException e) {
      logger.warning("Bad request: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (Exception e) {
      logger.severe("Error getting permissions: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve permissions");
    }
  }

  /**
   * Grant access to a user.
   */
  @PostMapping("/rc/{rcId}/user")
  @Operation(summary = "Grant user access",
      description = "Grants access to a specific user. Only owners can grant access.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Access granted successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Forbidden - not an owner")
  })
  public ResponseEntity<?> grantUserAccess(
      @Parameter(description = "RC ID") @PathVariable Long rcId,
      @RequestBody GrantUserAccessRequest request,
      Authentication authentication) {
    if (authentication == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    try {
      AccessLevel level = AccessLevel.valueOf(request.getAccessLevel());
      RCAccessDTO access = permissionService.grantUserAccess(
          rcId, request.getUsername(), level, authentication.getName());
      return ResponseEntity.status(HttpStatus.CREATED).body(access);
    } catch (SecurityException e) {
      logger.warning("Access denied for " + authentication.getName() + ": " + e.getMessage());
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    } catch (IllegalArgumentException e) {
      logger.warning("Bad request: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    } catch (Exception e) {
      logger.severe("Error granting user access: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to grant access");
    }
  }

  /**
   * Grant access to a group or distribution list.
   */
  @PostMapping("/rc/{rcId}/group")
  @Operation(summary = "Grant group/DL access",
      description = "Grants access to a security group or distribution list. Only owners can grant access.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Access granted successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Forbidden - not an owner")
  })
  public ResponseEntity<?> grantGroupAccess(
      @Parameter(description = "RC ID") @PathVariable Long rcId,
      @RequestBody GrantGroupAccessRequest request,
      Authentication authentication) {
    if (authentication == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    try {
      PrincipalType type = PrincipalType.valueOf(request.getPrincipalType());
      AccessLevel level = AccessLevel.valueOf(request.getAccessLevel());
      RCAccessDTO access = permissionService.grantGroupAccess(
          rcId, request.getPrincipalIdentifier(), request.getPrincipalDisplayName(),
          type, level, authentication.getName());
      return ResponseEntity.status(HttpStatus.CREATED).body(access);
    } catch (SecurityException e) {
      logger.warning("Access denied for " + authentication.getName() + ": " + e.getMessage());
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    } catch (IllegalArgumentException e) {
      logger.warning("Bad request: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    } catch (Exception e) {
      logger.severe("Error granting group access: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to grant access");
    }
  }

  /**
   * Update an existing permission.
   */
  @PutMapping("/{accessId}")
  @Operation(summary = "Update permission",
      description = "Updates the access level of an existing permission. Only owners can update permissions.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Permission updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Forbidden - not an owner"),
      @ApiResponse(responseCode = "404", description = "Permission not found")
  })
  public ResponseEntity<?> updatePermission(
      @Parameter(description = "Access record ID") @PathVariable Long accessId,
      @RequestBody UpdatePermissionRequest request,
      Authentication authentication) {
    if (authentication == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    try {
      AccessLevel level = AccessLevel.valueOf(request.getAccessLevel());
      RCAccessDTO access = permissionService.updatePermission(accessId, level, authentication.getName());
      return ResponseEntity.ok(access);
    } catch (SecurityException e) {
      logger.warning("Access denied for " + authentication.getName() + ": " + e.getMessage());
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    } catch (IllegalArgumentException e) {
      logger.warning("Bad request: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    } catch (Exception e) {
      logger.severe("Error updating permission: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update permission");
    }
  }

  /**
   * Revoke access.
   */
  @DeleteMapping("/{accessId}")
  @Operation(summary = "Revoke access",
      description = "Revokes an existing permission. Only owners can revoke permissions.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Access revoked successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Forbidden - not an owner"),
      @ApiResponse(responseCode = "404", description = "Permission not found")
  })
  public ResponseEntity<?> revokeAccess(
      @Parameter(description = "Access record ID") @PathVariable Long accessId,
      Authentication authentication) {
    if (authentication == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    try {
      permissionService.revokeAccess(accessId, authentication.getName());
      return ResponseEntity.noContent().build();
    } catch (SecurityException e) {
      logger.warning("Access denied for " + authentication.getName() + ": " + e.getMessage());
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    } catch (IllegalArgumentException e) {
      logger.warning("Bad request: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    } catch (Exception e) {
      logger.severe("Error revoking access: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to revoke access");
    }
  }

  /**
   * Check if current user is an owner of the RC.
   */
  @GetMapping("/rc/{rcId}/is-owner")
  @Operation(summary = "Check if user is owner",
      description = "Checks if the current user is an owner of the specified RC.")
  public ResponseEntity<Boolean> isOwner(
      @Parameter(description = "RC ID") @PathVariable Long rcId,
      Authentication authentication) {
    if (authentication == null) {
      return ResponseEntity.ok(false);
    }

    boolean isOwner = permissionService.isOwner(rcId, authentication.getName());
    return ResponseEntity.ok(isOwner);
  }

  /**
   * Check if current user can edit content.
   */
  @GetMapping("/rc/{rcId}/can-edit")
  @Operation(summary = "Check edit permission",
      description = "Checks if the current user can edit content in the specified RC.")
  public ResponseEntity<Boolean> canEdit(
      @Parameter(description = "RC ID") @PathVariable Long rcId,
      Authentication authentication) {
    if (authentication == null) {
      return ResponseEntity.ok(false);
    }

    boolean canEdit = permissionService.canEditContent(rcId, authentication.getName(),
        LdapSecurityConfig.extractGroupDns(authentication));
    return ResponseEntity.ok(canEdit);
  }

  // Request DTOs
  public static class GrantUserAccessRequest {
    private String username;
    private String accessLevel;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAccessLevel() { return accessLevel; }
    public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }
  }

  public static class GrantGroupAccessRequest {
    private String principalIdentifier;
    private String principalDisplayName;
    private String principalType;  // GROUP or DISTRIBUTION_LIST
    private String accessLevel;

    public String getPrincipalIdentifier() { return principalIdentifier; }
    public void setPrincipalIdentifier(String principalIdentifier) { this.principalIdentifier = principalIdentifier; }
    public String getPrincipalDisplayName() { return principalDisplayName; }
    public void setPrincipalDisplayName(String principalDisplayName) { this.principalDisplayName = principalDisplayName; }
    public String getPrincipalType() { return principalType; }
    public void setPrincipalType(String principalType) { this.principalType = principalType; }
    public String getAccessLevel() { return accessLevel; }
    public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }
  }

  public static class UpdatePermissionRequest {
    private String accessLevel;

    public String getAccessLevel() { return accessLevel; }
    public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }
  }
}
