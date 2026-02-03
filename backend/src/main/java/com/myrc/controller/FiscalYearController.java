/*
 * myRC - Fiscal Year REST Controller
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.controller;

import com.myrc.dto.ErrorResponse;
import com.myrc.dto.FiscalYearDTO;
import com.myrc.service.FiscalYearService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Fiscal Year management.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-22
 */
@RestController
@RequestMapping("/responsibility-centres/{rcId}/fiscal-years")
@Tag(name = "Fiscal Year Management", description = "APIs for managing fiscal years within responsibility centres")
public class FiscalYearController {

  private static final Logger logger = Logger.getLogger(FiscalYearController.class.getName());
  private final FiscalYearService fiscalYearService;

  public FiscalYearController(FiscalYearService fiscalYearService) {
    this.fiscalYearService = fiscalYearService;
  }

  /**
   * Get all fiscal years for a responsibility centre.
   *
   * @param rcId the responsibility centre ID
   * @param authentication the authentication principal
   * @return list of fiscal years
   */
  @GetMapping
  @Operation(summary = "Get all fiscal years for an RC",
      description = "Retrieves all fiscal years associated with a responsibility centre")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Fiscal years retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Access denied to this RC"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<List<FiscalYearDTO>> getFiscalYears(
      @PathVariable Long rcId,
      Authentication authentication) {
    // Use default user for unauthenticated access (development mode)
    String username = "default-user";
    if (authentication != null && authentication.getName() != null && !authentication.getName().isEmpty()) {
      username = authentication.getName();
    }
    logger.info("GET /responsibility-centres/" + rcId + "/fiscal-years - Fetching fiscal years for user: " + username);
    
    try {
      List<FiscalYearDTO> fiscalYears = fiscalYearService.getFiscalYearsByRCId(rcId, username);
      return ResponseEntity.ok(fiscalYears);
    } catch (IllegalArgumentException e) {
      logger.warning("Access denied for fiscal years: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (Exception e) {
      logger.severe("Failed to fetch fiscal years: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get a specific fiscal year by ID.
   *
   * @param rcId the responsibility centre ID
   * @param fyId the fiscal year ID
   * @param authentication the authentication principal
   * @return the fiscal year
   */
  @GetMapping("/{fyId}")
  @Operation(summary = "Get a specific fiscal year",
      description = "Retrieves a specific fiscal year by ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Fiscal year retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "Fiscal year not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<FiscalYearDTO> getFiscalYear(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      Authentication authentication) {
    // Use default user for unauthenticated access (development mode)
    String username = "default-user";
    if (authentication != null && authentication.getName() != null && !authentication.getName().isEmpty()) {
      username = authentication.getName();
    }
    logger.info("GET /responsibility-centres/" + rcId + "/fiscal-years/" + fyId + " - Fetching fiscal year for user: " + username);
    
    try {
      Optional<FiscalYearDTO> fyOpt = fiscalYearService.getFiscalYearById(fyId, username);
      return fyOpt.map(ResponseEntity::ok)
          .orElseGet(() -> ResponseEntity.notFound().build());
    } catch (Exception e) {
      logger.severe("Failed to fetch fiscal year: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Create a new fiscal year for a responsibility centre.
   *
   * @param rcId the responsibility centre ID
   * @param authentication the authentication principal
   * @param request the fiscal year creation request
   * @return the created fiscal year
   */
  @PostMapping
  @Operation(summary = "Create a new fiscal year",
      description = "Creates a new fiscal year for a responsibility centre")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Fiscal year created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<?> createFiscalYear(
      @PathVariable Long rcId,
      Authentication authentication,
      @RequestBody FiscalYearCreateRequest request) {
    // Use default user for unauthenticated access (development mode)
    String username = "default-user";
    if (authentication != null && authentication.getName() != null && !authentication.getName().isEmpty()) {
      username = authentication.getName();
    }
    logger.info("POST /responsibility-centres/" + rcId + "/fiscal-years - Creating fiscal year: " + request.getName() + " for user: " + username);
    
    try {
      if (request.getName() == null || request.getName().trim().isEmpty()) {
        logger.warning("Fiscal year creation failed: Name is required");
        return ResponseEntity.badRequest().body(new ErrorResponse("Fiscal year name is required"));
      }

      if (!request.isNameValidForFilename()) {
        logger.warning("Fiscal year creation failed: Name contains invalid characters");
        return ResponseEntity.badRequest().body(new ErrorResponse(
            "Fiscal year name cannot contain the following characters: < > : \" / \\ | ? *"));
      }

      FiscalYearDTO createdFY = fiscalYearService.createFiscalYear(
          rcId,
          username,
          request.getName(),
          request.getDescription() != null ? request.getDescription() : ""
      );
      return ResponseEntity.status(HttpStatus.CREATED).body(createdFY);
    } catch (IllegalArgumentException e) {
      logger.warning("Fiscal year creation failed: " + e.getMessage());
      return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    } catch (Exception e) {
      logger.severe("Fiscal year creation failed: " + e.getMessage());
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("An unexpected error occurred"));
    }
  }

  /**
   * Update an existing fiscal year.
   *
   * @param rcId the responsibility centre ID
   * @param fyId the fiscal year ID
   * @param authentication the authentication principal
   * @param request the update request
   * @return the updated fiscal year
   */
  @PutMapping("/{fyId}")
  @Operation(summary = "Update a fiscal year",
      description = "Updates an existing fiscal year")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Fiscal year updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "Fiscal year not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<?> updateFiscalYear(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      Authentication authentication,
      @RequestBody FiscalYearCreateRequest request) {
    // Use default user for unauthenticated access (development mode)
    String username = "default-user";
    if (authentication != null && authentication.getName() != null && !authentication.getName().isEmpty()) {
      username = authentication.getName();
    }
    logger.info("PUT /responsibility-centres/" + rcId + "/fiscal-years/" + fyId + " - Updating fiscal year for user: " + username);
    
    try {
      if (request.getName() != null && !request.getName().trim().isEmpty() && !request.isNameValidForFilename()) {
        logger.warning("Fiscal year update failed: Name contains invalid characters");
        return ResponseEntity.badRequest().body(new ErrorResponse(
            "Fiscal year name cannot contain the following characters: < > : \" / \\ | ? *"));
      }

      Optional<FiscalYearDTO> updatedFY = fiscalYearService.updateFiscalYear(
          fyId,
          username,
          request.getName(),
          request.getDescription()
      );
      return updatedFY.map(ResponseEntity::ok)
          .orElseGet(() -> ResponseEntity.notFound().build());
    } catch (IllegalArgumentException e) {
      logger.warning("Fiscal year update failed: " + e.getMessage());
      return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    } catch (Exception e) {
      logger.severe("Fiscal year update failed: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("An unexpected error occurred"));
    }
  }

  /**
   * Delete a fiscal year.
   *
   * @param rcId the responsibility centre ID
   * @param fyId the fiscal year ID
   * @param authentication the authentication principal
   * @return no content
   */
  @DeleteMapping("/{fyId}")
  @Operation(summary = "Delete a fiscal year",
      description = "Deletes a fiscal year")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Fiscal year deleted successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "Fiscal year not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<Void> deleteFiscalYear(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      Authentication authentication) {
    // Use default user for unauthenticated access (development mode)
    String username = "default-user";
    if (authentication != null && authentication.getName() != null && !authentication.getName().isEmpty()) {
      username = authentication.getName();
    }
    logger.info("DELETE /responsibility-centres/" + rcId + "/fiscal-years/" + fyId + " - Deleting fiscal year for user: " + username);
    
    try {
      fiscalYearService.deleteFiscalYear(fyId, username);
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException e) {
      logger.warning("Fiscal year deletion failed: " + e.getMessage());
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      logger.severe("Fiscal year deletion failed: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Update fiscal year display settings.
   *
   * @param rcId the responsibility centre ID
   * @param fyId the fiscal year ID
   * @param request the display settings request
   * @param authentication the authentication principal
   * @return the updated fiscal year
   */
  @PatchMapping("/{fyId}/display-settings")
  @Operation(summary = "Update fiscal year display settings",
      description = "Updates display settings like category filter visibility and grouping")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Display settings updated successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "404", description = "Fiscal year not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<FiscalYearDTO> updateDisplaySettings(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      @RequestBody DisplaySettingsRequest request,
      Authentication authentication) {
    String username = "default-user";
    if (authentication != null && authentication.getName() != null && !authentication.getName().isEmpty()) {
      username = authentication.getName();
    }
    logger.info("PATCH /responsibility-centres/" + rcId + "/fiscal-years/" + fyId + "/display-settings - Updating display settings for user: " + username);
    
    try {
      Optional<FiscalYearDTO> updatedFY = fiscalYearService.updateDisplaySettings(
          fyId,
          username,
          request.getShowSearchBox(),
          request.getShowCategoryFilter(),
          request.getGroupByCategory(),
          request.getOnTargetMin(),
          request.getOnTargetMax()
      );
      return updatedFY.map(ResponseEntity::ok)
          .orElseGet(() -> ResponseEntity.notFound().build());
    } catch (IllegalArgumentException e) {
      logger.warning("Display settings update failed: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (Exception e) {
      logger.severe("Display settings update failed: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Toggle the active status of a fiscal year.
   * Only the RC owner can toggle the active status.
   *
   * @param rcId the responsibility centre ID
   * @param fyId the fiscal year ID
   * @param authentication the authentication principal
   * @return the updated fiscal year
   */
  @PatchMapping("/{fyId}/toggle-active")
  @Operation(summary = "Toggle fiscal year active status",
      description = "Toggles whether a fiscal year is active or inactive. Only RC owners can toggle.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Active status toggled successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Access denied - only RC owners can toggle"),
      @ApiResponse(responseCode = "404", description = "Fiscal year not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<?> toggleActiveStatus(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      Authentication authentication) {
    String username = "default-user";
    if (authentication != null && authentication.getName() != null && !authentication.getName().isEmpty()) {
      username = authentication.getName();
    }
    logger.info("PATCH /responsibility-centres/" + rcId + "/fiscal-years/" + fyId + "/toggle-active - Toggling active status for user: " + username);
    
    try {
      Optional<FiscalYearDTO> updatedFY = fiscalYearService.toggleActiveStatus(fyId, username);
      return updatedFY.map(ResponseEntity::ok)
          .orElseGet(() -> ResponseEntity.notFound().build());
    } catch (IllegalArgumentException e) {
      String message = e.getMessage();
      logger.warning("Toggle active status failed: " + message);
      if (message != null && message.contains("owner")) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(message));
      }
      return ResponseEntity.badRequest().body(new ErrorResponse(message));
    } catch (Exception e) {
      logger.severe("Toggle active status failed: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("An unexpected error occurred"));
    }
  }

  // Request DTOs
  public static class FiscalYearCreateRequest {
    private static final String INVALID_FILENAME_CHARS = "<>:\"/\\|?*";
    private String name;
    private String description;

    public FiscalYearCreateRequest() {}

    public FiscalYearCreateRequest(String name, String description) {
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

    /**
     * Validate that the name doesn't contain characters invalid for filenames.
     * @return true if valid, false otherwise
     */
    public boolean isNameValidForFilename() {
      if (name == null || name.isBlank()) {
        return false;
      }
      for (char c : INVALID_FILENAME_CHARS.toCharArray()) {
        if (name.indexOf(c) >= 0) {
          return false;
        }
      }
      return true;
    }
  }

  public static class DisplaySettingsRequest {
    private Boolean showSearchBox;
    private Boolean showCategoryFilter;
    private Boolean groupByCategory;
    private Integer onTargetMin;
    private Integer onTargetMax;

    public DisplaySettingsRequest() {}

    public DisplaySettingsRequest(Boolean showSearchBox, Boolean showCategoryFilter, Boolean groupByCategory,
                                   Integer onTargetMin, Integer onTargetMax) {
      this.showSearchBox = showSearchBox;
      this.showCategoryFilter = showCategoryFilter;
      this.groupByCategory = groupByCategory;
      this.onTargetMin = onTargetMin;
      this.onTargetMax = onTargetMax;
    }

    public Boolean getShowSearchBox() {
      return showSearchBox;
    }

    public void setShowSearchBox(Boolean showSearchBox) {
      this.showSearchBox = showSearchBox;
    }

    public Boolean getShowCategoryFilter() {
      return showCategoryFilter;
    }

    public void setShowCategoryFilter(Boolean showCategoryFilter) {
      this.showCategoryFilter = showCategoryFilter;
    }

    public Boolean getGroupByCategory() {
      return groupByCategory;
    }

    public void setGroupByCategory(Boolean groupByCategory) {
      this.groupByCategory = groupByCategory;
    }

    public Integer getOnTargetMin() {
      return onTargetMin;
    }

    public void setOnTargetMin(Integer onTargetMin) {
      this.onTargetMin = onTargetMin;
    }

    public Integer getOnTargetMax() {
      return onTargetMax;
    }

    public void setOnTargetMax(Integer onTargetMax) {
      this.onTargetMax = onTargetMax;
    }
  }
}
