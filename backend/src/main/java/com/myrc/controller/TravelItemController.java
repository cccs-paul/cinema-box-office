/*
 * myRC - Travel Item REST Controller
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.controller;

import com.myrc.audit.Audited;
import com.myrc.dto.ErrorResponse;
import com.myrc.dto.TravelItemDTO;
import com.myrc.dto.TravelMoneyAllocationDTO;
import com.myrc.service.TravelItemService;
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
 * REST Controller for Travel Item management.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-16
 */
@RestController
@RequestMapping("/responsibility-centres/{rcId}/fiscal-years/{fyId}/travel-items")
@Tag(name = "Travel Item Management", description = "APIs for managing travel items within fiscal years")
public class TravelItemController {

  private static final Logger logger = Logger.getLogger(TravelItemController.class.getName());
  private final TravelItemService travelItemService;

  public TravelItemController(TravelItemService travelItemService) {
    this.travelItemService = travelItemService;
  }

  @GetMapping
  @Operation(summary = "Get all travel items for a fiscal year")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Travel items retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<List<TravelItemDTO>> getTravelItems(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      Authentication authentication) {
    String username = getUsername(authentication);
    logger.info("GET /responsibility-centres/" + rcId + "/fiscal-years/" + fyId +
        "/travel-items - Fetching travel items for user: " + username);
    try {
      List<TravelItemDTO> items = travelItemService.getTravelItemsByFiscalYearId(fyId, username);
      return ResponseEntity.ok(items);
    } catch (IllegalArgumentException e) {
      logger.warning("Access denied for travel items: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (Exception e) {
      logger.severe("Failed to fetch travel items: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping("/{travelItemId}")
  @Operation(summary = "Get a specific travel item")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Travel item retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "Travel item not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<TravelItemDTO> getTravelItem(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      @PathVariable Long travelItemId,
      Authentication authentication) {
    String username = getUsername(authentication);
    try {
      Optional<TravelItemDTO> itemOpt = travelItemService.getTravelItemById(travelItemId, username);
      return itemOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    } catch (Exception e) {
      logger.severe("Failed to fetch travel item: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PostMapping
  @Audited(action = "CREATE_TRAVEL_ITEM", entityType = "TRAVEL_ITEM")
  @Operation(summary = "Create a new travel item")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Travel item created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<?> createTravelItem(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      Authentication authentication,
      @RequestBody TravelItemDTO request) {
    String username = getUsername(authentication);
    logger.info("POST /responsibility-centres/" + rcId + "/fiscal-years/" + fyId +
        "/travel-items - Creating travel item for user: " + username);
    try {
      request.setFiscalYearId(fyId);
      TravelItemDTO created = travelItemService.createTravelItem(request, username);
      return ResponseEntity.status(HttpStatus.CREATED).body(created);
    } catch (IllegalArgumentException e) {
      logger.warning("Failed to create travel item: " + e.getMessage());
      return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    } catch (Exception e) {
      logger.severe("Failed to create travel item: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorResponse("Failed to create travel item"));
    }
  }

  @PutMapping("/{travelItemId}")
  @Audited(action = "UPDATE_TRAVEL_ITEM", entityType = "TRAVEL_ITEM")
  @Operation(summary = "Update a travel item")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Travel item updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "404", description = "Travel item not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<?> updateTravelItem(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      @PathVariable Long travelItemId,
      Authentication authentication,
      @RequestBody TravelItemDTO request) {
    String username = getUsername(authentication);
    logger.info("PUT /responsibility-centres/" + rcId + "/fiscal-years/" + fyId +
        "/travel-items/" + travelItemId + " - Updating travel item for user: " + username);
    try {
      TravelItemDTO updated = travelItemService.updateTravelItem(travelItemId, request, username);
      return ResponseEntity.ok(updated);
    } catch (IllegalArgumentException e) {
      logger.warning("Failed to update travel item: " + e.getMessage());
      if (e.getMessage().contains("not found")) {
        return ResponseEntity.notFound().build();
      }
      return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    } catch (Exception e) {
      logger.severe("Failed to update travel item: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorResponse("Failed to update travel item"));
    }
  }

  @DeleteMapping("/{travelItemId}")
  @Audited(action = "DELETE_TRAVEL_ITEM", entityType = "TRAVEL_ITEM")
  @Operation(summary = "Delete a travel item")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Travel item deleted successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "404", description = "Travel item not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<?> deleteTravelItem(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      @PathVariable Long travelItemId,
      Authentication authentication) {
    String username = getUsername(authentication);
    logger.info("DELETE /responsibility-centres/" + rcId + "/fiscal-years/" + fyId +
        "/travel-items/" + travelItemId + " - Deleting travel item for user: " + username);
    try {
      travelItemService.deleteTravelItem(travelItemId, username);
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException e) {
      logger.warning("Failed to delete travel item: " + e.getMessage());
      if (e.getMessage().contains("not found")) {
        return ResponseEntity.notFound().build();
      }
      return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    } catch (Exception e) {
      logger.severe("Failed to delete travel item: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorResponse("Failed to delete travel item"));
    }
  }

  @PutMapping("/{travelItemId}/status")
  @Audited(action = "UPDATE_TRAVEL_ITEM_STATUS", entityType = "TRAVEL_ITEM")
  @Operation(summary = "Update travel item status")
  public ResponseEntity<?> updateStatus(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      @PathVariable Long travelItemId,
      Authentication authentication,
      @RequestBody StatusUpdateRequest request) {
    String username = getUsername(authentication);
    try {
      TravelItemDTO updated = travelItemService.updateTravelItemStatus(travelItemId, request.status, username);
      return ResponseEntity.ok(updated);
    } catch (IllegalArgumentException e) {
      if (e.getMessage().contains("not found")) {
        return ResponseEntity.notFound().build();
      }
      return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorResponse("Failed to update travel item status"));
    }
  }

  @GetMapping("/{travelItemId}/allocations")
  @Operation(summary = "Get money allocations for a travel item")
  public ResponseEntity<List<TravelMoneyAllocationDTO>> getAllocations(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      @PathVariable Long travelItemId,
      Authentication authentication) {
    String username = getUsername(authentication);
    try {
      List<TravelMoneyAllocationDTO> allocations = travelItemService.getMoneyAllocations(travelItemId, username);
      return ResponseEntity.ok(allocations);
    } catch (IllegalArgumentException e) {
      if (e.getMessage().contains("not found")) {
        return ResponseEntity.notFound().build();
      }
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PutMapping("/{travelItemId}/allocations")
  @Audited(action = "UPDATE_TRAVEL_ALLOCATIONS", entityType = "TRAVEL_ITEM")
  @Operation(summary = "Update money allocations for a travel item")
  public ResponseEntity<?> updateAllocations(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      @PathVariable Long travelItemId,
      Authentication authentication,
      @RequestBody List<TravelMoneyAllocationDTO> allocations) {
    String username = getUsername(authentication);
    try {
      TravelItemDTO updated = travelItemService.updateMoneyAllocations(travelItemId, allocations, username);
      return ResponseEntity.ok(updated);
    } catch (IllegalArgumentException e) {
      if (e.getMessage().contains("not found")) {
        return ResponseEntity.notFound().build();
      }
      return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorResponse("Failed to update allocations"));
    }
  }

  private String getUsername(Authentication authentication) {
    if (authentication != null && authentication.getName() != null) {
      return authentication.getName();
    }
    return "anonymous";
  }

  public static class StatusUpdateRequest {
    public String status;
  }
}
