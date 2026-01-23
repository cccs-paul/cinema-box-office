/*
 * myRC - Responsibility Centre Management System
 * Responsibility Centre REST Controller
 *
 * Author: myRC Team
 * Date: 2026-01-21
 * Version: 1.0.0
 *
 * License: MIT
 *
 * Description:
 * REST API endpoints for responsibility centre management operations including
 * creation, reading, updating, and access management.
 */

package com.boxoffice.controller;

import com.boxoffice.dto.ResponsibilityCentreDTO;
import com.boxoffice.dto.RCAccessDTO;
import com.boxoffice.service.ResponsibilityCentreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@RestController
@RequestMapping("/responsibility-centres")
@Tag(name = "Responsibility Centre Management", description = "APIs for managing responsibility centres and access control")
public class ResponsibilityCentreController {

  private static final Logger logger = Logger.getLogger(ResponsibilityCentreController.class.getName());
  private final ResponsibilityCentreService rcService;

  public ResponsibilityCentreController(ResponsibilityCentreService rcService) {
    this.rcService = rcService;
  }

  /**
   * Get all responsibility centres for the authenticated user.
   *
   * @param authentication the authentication principal
   * @return list of responsibility centres the user owns or has access to
   */
  @GetMapping
  @Operation(summary = "Get user's responsibility centres", 
      description = "Retrieves all responsibility centres that the authenticated user owns or has access to")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<List<ResponsibilityCentreDTO>> getAllResponsibilityCentres(Authentication authentication) {
    if (authentication == null || authentication.getName() == null || authentication.getName().isEmpty()) {
      logger.info("GET /responsibility-centres - Anonymous access, returning empty list");
      return ResponseEntity.ok(List.of());
    }
    logger.info("GET /responsibility-centres - Fetching RCs for user: " + authentication.getName());
    try {
      List<ResponsibilityCentreDTO> rcs = rcService.getUserResponsibilityCentres(authentication.getName());
      return ResponseEntity.ok(rcs);
    } catch (Exception e) {
      logger.severe("Failed to fetch responsibility centres: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Create a new responsibility centre.
   *
   * @param authentication the authentication principal
   * @param request the RC creation request with name and description
   * @return created responsibility centre
   */
  @PostMapping
  @Operation(summary = "Create a new responsibility centre",
      description = "Creates a new responsibility centre owned by the authenticated user")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "RC created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ResponsibilityCentreDTO> createResponsibilityCentre(
      Authentication authentication,
      @RequestBody RCCreateRequest request) {
    // Use default user for unauthenticated access (development mode)
    String username = "default-user";
    if (authentication != null && authentication.getName() != null && !authentication.getName().isEmpty()) {
      username = authentication.getName();
      logger.info("POST /responsibility-centres - Creating RC: " + request.getName() + " for authenticated user: " + username);
    } else {
      logger.info("POST /responsibility-centres - Creating RC: " + request.getName() + " for unauthenticated user (using default)");
    }
    
    try {
      if (request.getName() == null || request.getName().trim().isEmpty()) {
        logger.warning("RC creation failed: Name is required");
        return ResponseEntity.badRequest().build();
      }

      ResponsibilityCentreDTO createdRC = rcService.createResponsibilityCentre(
          username,
          request.getName(),
          request.getDescription() != null ? request.getDescription() : ""
      );
      return ResponseEntity.status(HttpStatus.CREATED).body(createdRC);
    } catch (IllegalArgumentException e) {
      logger.warning("RC creation failed: " + e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      logger.severe("RC creation failed: " + e.getMessage());
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get a specific responsibility centre by ID.
   *
   * @param id the RC ID
   * @param authentication the authentication principal
   * @return the responsibility centre if user has access
   */
  @GetMapping("/{id}")
  @Operation(summary = "Get a responsibility centre",
      description = "Retrieves a specific responsibility centre if the user has access")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "RC retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "RC not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ResponsibilityCentreDTO> getResponsibilityCentre(
      @PathVariable Long id,
      Authentication authentication) {
    // Use default user for unauthenticated access (development mode)
    String username = "default-user";
    if (authentication != null && authentication.getName() != null && !authentication.getName().isEmpty()) {
      username = authentication.getName();
    }
    logger.info("GET /responsibility-centres/{" + id + "} - Fetching RC for user: " + username);
    try {
      Optional<ResponsibilityCentreDTO> rc = rcService.getResponsibilityCentre(id, username);
      return rc.map(ResponseEntity::ok)
          .orElseGet(() -> ResponseEntity.notFound().build());
    } catch (Exception e) {
      logger.severe("Failed to fetch responsibility centre: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Update a responsibility centre.
   *
   * @param id the RC ID
   * @param authentication the authentication principal
   * @param request the update request
   * @return updated responsibility centre
   */
  @PutMapping("/{id}")
  @Operation(summary = "Update a responsibility centre",
      description = "Updates a responsibility centre if the user is the owner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "RC updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "RC not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ResponsibilityCentreDTO> updateResponsibilityCentre(
      @PathVariable Long id,
      Authentication authentication,
      @RequestBody RCCreateRequest request) {
    // Use default user for unauthenticated access (development mode)
    String username = "default-user";
    if (authentication != null && authentication.getName() != null && !authentication.getName().isEmpty()) {
      username = authentication.getName();
    }
    logger.info("PUT /responsibility-centres/{" + id + "} - Updating RC for user: " + username);
    try {
      Optional<ResponsibilityCentreDTO> updatedRC = rcService.updateResponsibilityCentre(
          id,
          username,
          request.getName(),
          request.getDescription()
      );
      return updatedRC.map(ResponseEntity::ok)
          .orElseGet(() -> ResponseEntity.notFound().build());
    } catch (IllegalArgumentException e) {
      logger.warning("RC update failed: " + e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      logger.severe("RC update failed: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Delete a responsibility centre.
   *
   * @param id the RC ID
   * @param authentication the authentication principal
   * @return no content response
   */
  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a responsibility centre",
      description = "Deletes a responsibility centre if the user is the owner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "RC deleted successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "RC not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<Void> deleteResponsibilityCentre(
      @PathVariable Long id,
      Authentication authentication) {
    // Use default user for unauthenticated access (development mode)
    String username = "default-user";
    if (authentication != null && authentication.getName() != null && !authentication.getName().isEmpty()) {
      username = authentication.getName();
    }
    logger.info("DELETE /responsibility-centres/{" + id + "} - Deleting RC for user: " + username);
    try {
      rcService.deleteResponsibilityCentre(id, username);
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException e) {
      logger.warning("RC deletion failed: " + e.getMessage());
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      logger.severe("RC deletion failed: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Grant access to a responsibility centre.
   *
   * @param rcId the RC ID
   * @param authentication the authentication principal
   * @param request the access grant request
   * @return no content response
   */
  @PostMapping("/{rcId}/access/grant")
  @Operation(summary = "Grant access to a responsibility centre",
      description = "Grants access to a responsibility centre to another user")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Access granted successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "RC not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<?> grantAccess(
      @PathVariable Long rcId,
      Authentication authentication,
      @RequestBody RCAccessRequest request) {
    // Use default user for unauthenticated access (development mode)
    String username = "default-user";
    if (authentication != null && authentication.getName() != null && !authentication.getName().isEmpty()) {
      username = authentication.getName();
    }
    logger.info("POST /responsibility-centres/{" + rcId + "}/access/grant - Granting access to user: " + request.getUsername());
    try {
      rcService.grantAccess(rcId, username, request.getUsername(), request.getAccessLevel());
      return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
      logger.warning("Grant access failed: " + e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      logger.severe("Grant access failed: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Revoke access to a responsibility centre.
   *
   * @param rcId the RC ID
   * @param authentication the authentication principal
   * @param request the revoke request
   * @return no content response
   */
  @PostMapping("/{rcId}/access/revoke")
  @Operation(summary = "Revoke access to a responsibility centre",
      description = "Revokes a user's access to a responsibility centre")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Access revoked successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "RC not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<?> revokeAccess(
      @PathVariable Long rcId,
      Authentication authentication,
      @RequestBody RCRevokeRequest request) {
    // Use default user for unauthenticated access (development mode)
    String username = "default-user";
    if (authentication != null && authentication.getName() != null && !authentication.getName().isEmpty()) {
      username = authentication.getName();
    }
    logger.info("POST /responsibility-centres/{" + rcId + "}/access/revoke - Revoking access from user: " + request.getUsername());
    try {
      rcService.revokeAccess(rcId, username, request.getUsername());
      return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
      logger.warning("Revoke access failed: " + e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      logger.severe("Revoke access failed: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get access list for a responsibility centre.
   *
   * @param rcId the RC ID
   * @param authentication the authentication principal
   * @return list of users with access to the RC
   */
  @GetMapping("/{rcId}/access")
  @Operation(summary = "Get RC access list",
      description = "Retrieves the list of users with access to a responsibility centre")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Access list retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "RC not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<List<?>> getAccessList(
      @PathVariable Long rcId,
      Authentication authentication) {
    // Use default user for unauthenticated access (development mode)
    String username = "default-user";
    if (authentication != null && authentication.getName() != null && !authentication.getName().isEmpty()) {
      username = authentication.getName();
    }
    logger.info("GET /responsibility-centres/{" + rcId + "}/access - Fetching access list for user: " + username);
    try {
      List<?> accessList = rcService.getResponsibilityCentreAccess(rcId, username);
      return ResponseEntity.ok(accessList);
    } catch (Exception e) {
      logger.severe("Failed to fetch access list: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Clone a responsibility centre.
   *
   * @param id the RC ID to clone
   * @param authentication the authentication principal
   * @param request the clone request with new name
   * @return the cloned responsibility centre
   */
  @PostMapping("/{id}/clone")
  @Operation(summary = "Clone a responsibility centre",
      description = "Creates a copy of a responsibility centre with a new name")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "RC cloned successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "RC not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ResponsibilityCentreDTO> cloneResponsibilityCentre(
      @PathVariable Long id,
      Authentication authentication,
      @RequestBody RCCloneRequest request) {
    // Use default user for unauthenticated access (development mode)
    String username = "default-user";
    if (authentication != null && authentication.getName() != null && !authentication.getName().isEmpty()) {
      username = authentication.getName();
    }
    logger.info("POST /responsibility-centres/{" + id + "}/clone - Cloning RC with new name: " + request.getNewName() + " for user: " + username);
    try {
      if (request.getNewName() == null || request.getNewName().trim().isEmpty()) {
        logger.warning("RC clone failed: New name is required");
        return ResponseEntity.badRequest().build();
      }

      ResponsibilityCentreDTO clonedRC = rcService.cloneResponsibilityCentre(
          id,
          username,
          request.getNewName()
      );
      return ResponseEntity.status(HttpStatus.CREATED).body(clonedRC);
    } catch (IllegalArgumentException e) {
      logger.warning("RC clone failed: " + e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (IllegalAccessError e) {
      logger.warning("RC clone access denied: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (Exception e) {
      logger.severe("RC clone failed: " + e.getMessage());
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  // Request DTOs
  public static class RCCreateRequest {
    private String name;
    private String description;

    public RCCreateRequest() {}

    public RCCreateRequest(String name, String description) {
      this.name = name;
      this.description = description;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }
  }

  public static class RCAccessRequest {
    private String username;
    private String accessLevel;

    public RCAccessRequest() {}

    public RCAccessRequest(String username, String accessLevel) {
      this.username = username;
      this.accessLevel = accessLevel;
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getAccessLevel() {
      return accessLevel;
    }

    public void setAccessLevel(String accessLevel) {
      this.accessLevel = accessLevel;
    }
  }

  public static class RCRevokeRequest {
    private String username;

    public RCRevokeRequest() {}

    public RCRevokeRequest(String username) {
      this.username = username;
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }
  }

  public static class RCCloneRequest {
    private String newName;

    public RCCloneRequest() {}

    public RCCloneRequest(String newName) {
      this.newName = newName;
    }

    public String getNewName() {
      return newName;
    }

    public void setNewName(String newName) {
      this.newName = newName;
    }
  }
}
