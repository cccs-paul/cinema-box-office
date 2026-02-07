/*
 * myRC - Money REST Controller
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-24
 * Version: 1.0.0
 *
 * Description:
 * REST Controller for Money management.
 */
package com.myrc.controller;

import com.myrc.dto.ErrorResponse;
import com.myrc.dto.MoneyDTO;
import com.myrc.service.MoneyService;
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
 * REST Controller for Money management.
 * Monies are configured at the FY level and consist of two parts: CAP and OM.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-24
 */
@RestController
@RequestMapping("/responsibility-centres/{rcId}/fiscal-years/{fyId}/monies")
@Tag(name = "Money Management", description = "APIs for managing money types within fiscal years")
public class MoneyController {

  private static final Logger logger = Logger.getLogger(MoneyController.class.getName());
  private final MoneyService moneyService;

  public MoneyController(MoneyService moneyService) {
    this.moneyService = moneyService;
  }

  /**
   * Get all monies for a fiscal year.
   *
   * @param rcId the responsibility centre ID
   * @param fyId the fiscal year ID
   * @param authentication the authentication principal
   * @return list of monies
   */
  @GetMapping
  @Operation(summary = "Get all monies for a fiscal year",
      description = "Retrieves all money types configured for a fiscal year. Each money has CAP and OM parts.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Monies retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Access denied to this RC"),
      @ApiResponse(responseCode = "404", description = "Fiscal year not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<List<MoneyDTO>> getMonies(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      Authentication authentication) {
    String username = getUsername(authentication);
    logger.info("GET /responsibility-centres/" + rcId + "/fiscal-years/" + fyId + 
        "/monies - Fetching monies for user: " + username);

    try {
      List<MoneyDTO> monies = moneyService.getMoniesByFiscalYearId(fyId, username);
      return ResponseEntity.ok(monies);
    } catch (IllegalArgumentException e) {
      logger.warning("Access denied for monies: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (Exception e) {
      logger.severe("Failed to fetch monies: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get a specific money by ID.
   *
   * @param rcId the responsibility centre ID
   * @param fyId the fiscal year ID
   * @param moneyId the money ID
   * @param authentication the authentication principal
   * @return the money
   */
  @GetMapping("/{moneyId}")
  @Operation(summary = "Get a specific money",
      description = "Retrieves a specific money type by ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Money retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "Money not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<MoneyDTO> getMoney(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      @PathVariable Long moneyId,
      Authentication authentication) {
    String username = getUsername(authentication);
    logger.info("GET /responsibility-centres/" + rcId + "/fiscal-years/" + fyId + 
        "/monies/" + moneyId + " - Fetching money for user: " + username);

    try {
      Optional<MoneyDTO> moneyOpt = moneyService.getMoneyById(moneyId, username);
      return moneyOpt.map(ResponseEntity::ok)
          .orElseGet(() -> ResponseEntity.notFound().build());
    } catch (Exception e) {
      logger.severe("Failed to fetch money: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Create a new money for a fiscal year.
   *
   * @param rcId the responsibility centre ID
   * @param fyId the fiscal year ID
   * @param authentication the authentication principal
   * @param request the money creation request
   * @return the created money
   */
  @PostMapping
  @Operation(summary = "Create a new money",
      description = "Creates a new money type for a fiscal year")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Money created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "409", description = "Money code already exists"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<?> createMoney(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      Authentication authentication,
      @RequestBody MoneyCreateRequest request) {
    String username = getUsername(authentication);
    logger.info("POST /responsibility-centres/" + rcId + "/fiscal-years/" + fyId + 
        "/monies - Creating money: " + request.getCode() + " for user: " + username);

    try {
      if (request.getCode() == null || request.getCode().trim().isEmpty()) {
        logger.warning("Money creation failed: Code is required");
        return ResponseEntity.badRequest().body(new ErrorResponse("Money code is required"));
      }
      if (request.getName() == null || request.getName().trim().isEmpty()) {
        logger.warning("Money creation failed: Name is required");
        return ResponseEntity.badRequest().body(new ErrorResponse("Money name is required"));
      }

      MoneyDTO created = moneyService.createMoney(fyId, username, 
          request.getCode(), request.getName(), request.getDescription());
      return ResponseEntity.status(HttpStatus.CREATED).body(created);
    } catch (IllegalArgumentException e) {
      String message = e.getMessage();
      if (message.contains("already exists")) {
        logger.warning("Money creation conflict: " + message);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(message));
      }
      if (message.contains("access")) {
        logger.warning("Money creation access denied: " + message);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(message));
      }
      logger.warning("Money creation failed: " + message);
      return ResponseEntity.badRequest().body(new ErrorResponse(message));
    } catch (Exception e) {
      logger.severe("Failed to create money: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("An unexpected error occurred"));
    }
  }

  /**
   * Update an existing money.
   *
   * @param rcId the responsibility centre ID
   * @param fyId the fiscal year ID
   * @param moneyId the money ID
   * @param authentication the authentication principal
   * @param request the money update request
   * @return the updated money
   */
  @PutMapping("/{moneyId}")
  @Operation(summary = "Update a money",
      description = "Updates an existing money type. Cannot change the code of the default AB money.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Money updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "404", description = "Money not found"),
      @ApiResponse(responseCode = "409", description = "Money code already exists"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<?> updateMoney(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      @PathVariable Long moneyId,
      Authentication authentication,
      @RequestBody MoneyUpdateRequest request) {
    String username = getUsername(authentication);
    logger.info("PUT /responsibility-centres/" + rcId + "/fiscal-years/" + fyId + 
        "/monies/" + moneyId + " - Updating money for user: " + username);

    try {
      MoneyDTO updated = moneyService.updateMoney(moneyId, username, 
          request.getCode(), request.getName(), request.getDescription());
      return ResponseEntity.ok(updated);
    } catch (IllegalArgumentException e) {
      String message = e.getMessage();
      if (message.contains("not found")) {
        return ResponseEntity.notFound().build();
      }
      if (message.contains("already exists")) {
        logger.warning("Money update conflict: " + message);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(message));
      }
      if (message.contains("access") || message.contains("default")) {
        logger.warning("Money update denied: " + message);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(message));
      }
      logger.warning("Money update failed: " + message);
      return ResponseEntity.badRequest().body(new ErrorResponse(message));
    } catch (Exception e) {
      logger.severe("Failed to update money: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("An unexpected error occurred"));
    }
  }

  /**
   * Delete a money.
   *
   * @param rcId the responsibility centre ID
   * @param fyId the fiscal year ID
   * @param moneyId the money ID
   * @param authentication the authentication principal
   * @return no content on success
   */
  @DeleteMapping("/{moneyId}")
  @Operation(summary = "Delete a money",
      description = "Deletes a money type. Cannot delete the default AB money.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Money deleted successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Access denied or cannot delete default money"),
      @ApiResponse(responseCode = "404", description = "Money not found"),
      @ApiResponse(responseCode = "409", description = "Money is in use and cannot be deleted"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<?> deleteMoney(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      @PathVariable Long moneyId,
      Authentication authentication) {
    String username = getUsername(authentication);
    logger.info("DELETE /responsibility-centres/" + rcId + "/fiscal-years/" + fyId + 
        "/monies/" + moneyId + " - Deleting money for user: " + username);

    try {
      moneyService.deleteMoney(moneyId, username);
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException e) {
      String message = e.getMessage();
      if (message.contains("not found")) {
        return ResponseEntity.notFound().build();
      }
      if (message.contains("in use")) {
        logger.warning("Money delete conflict: " + message);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(message));
      }
      if (message.contains("access") || message.contains("default")) {
        logger.warning("Money delete denied: " + message);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(message));
      }
      logger.warning("Money delete failed: " + message);
      return ResponseEntity.badRequest().body(new ErrorResponse(message));
    } catch (Exception e) {
      logger.severe("Failed to delete money: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("An unexpected error occurred"));
    }
  }

  /**
   * Reorder monies within a fiscal year.
   *
   * @param rcId the responsibility centre ID
   * @param fyId the fiscal year ID
   * @param authentication the authentication principal
   * @param request the reorder request containing money IDs in desired order
   * @return no content on success
   */
  @PostMapping("/reorder")
  @Operation(summary = "Reorder monies",
      description = "Reorders money types within a fiscal year")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Monies reordered successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<?> reorderMonies(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      Authentication authentication,
      @RequestBody MoneyReorderRequest request) {
    String username = getUsername(authentication);
    logger.info("POST /responsibility-centres/" + rcId + "/fiscal-years/" + fyId + 
        "/monies/reorder - Reordering monies for user: " + username);

    try {
      if (request.getMoneyIds() == null || request.getMoneyIds().isEmpty()) {
        return ResponseEntity.badRequest().body(new ErrorResponse("Money IDs are required for reordering"));
      }

      moneyService.reorderMonies(fyId, username, request.getMoneyIds());
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException e) {
      if (e.getMessage().contains("access")) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
      }
      return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    } catch (Exception e) {
      logger.severe("Failed to reorder monies: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("An unexpected error occurred"));
    }
  }

  /**
   * Get username from authentication, defaulting to "default-user" for development.
   */
  private String getUsername(Authentication authentication) {
    if (authentication != null && authentication.getName() != null && !authentication.getName().isEmpty()) {
      return authentication.getName();
    }
    return "default-user";
  }

  // Request DTOs

  /**
   * Request body for creating a money.
   */
  public static class MoneyCreateRequest {
    private String code;
    private String name;
    private String description;

    public String getCode() {
      return code;
    }

    public void setCode(String code) {
      this.code = code;
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

  /**
   * Request body for updating a money.
   */
  public static class MoneyUpdateRequest {
    private String code;
    private String name;
    private String description;

    public String getCode() {
      return code;
    }

    public void setCode(String code) {
      this.code = code;
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

  /**
   * Request body for reordering monies.
   */
  public static class MoneyReorderRequest {
    private List<Long> moneyIds;

    public List<Long> getMoneyIds() {
      return moneyIds;
    }

    public void setMoneyIds(List<Long> moneyIds) {
      this.moneyIds = moneyIds;
    }
  }
}
