/*
 * myRC - Training Item REST Controller
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */
package com.myrc.controller;

import com.myrc.audit.Audited;
import com.myrc.dto.ErrorResponse;
import com.myrc.dto.TrainingItemDTO;
import com.myrc.dto.TrainingMoneyAllocationDTO;
import com.myrc.dto.TrainingParticipantDTO;
import com.myrc.service.TrainingItemService;
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
 * REST Controller for Training Item management.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-02-16
 */
@RestController
@RequestMapping("/responsibility-centres/{rcId}/fiscal-years/{fyId}/training-items")
@Tag(name = "Training Item Management", description = "APIs for managing training items within fiscal years")
public class TrainingItemController {

  private static final Logger logger = Logger.getLogger(TrainingItemController.class.getName());
  private final TrainingItemService trainingItemService;

  public TrainingItemController(TrainingItemService trainingItemService) {
    this.trainingItemService = trainingItemService;
  }

  @GetMapping
  @Operation(summary = "Get all training items for a fiscal year")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Training items retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<List<TrainingItemDTO>> getTrainingItems(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      Authentication authentication) {
    String username = getUsername(authentication);
    logger.info("GET /responsibility-centres/" + rcId + "/fiscal-years/" + fyId +
        "/training-items - Fetching training items for user: " + username);
    try {
      List<TrainingItemDTO> items = trainingItemService.getTrainingItemsByFiscalYearId(fyId, username);
      return ResponseEntity.ok(items);
    } catch (IllegalArgumentException e) {
      logger.warning("Access denied for training items: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (Exception e) {
      logger.severe("Failed to fetch training items: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping("/{trainingItemId}")
  @Operation(summary = "Get a specific training item")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Training item retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "Training item not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<TrainingItemDTO> getTrainingItem(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      @PathVariable Long trainingItemId,
      Authentication authentication) {
    String username = getUsername(authentication);
    try {
      Optional<TrainingItemDTO> itemOpt = trainingItemService.getTrainingItemById(trainingItemId, username);
      return itemOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    } catch (Exception e) {
      logger.severe("Failed to fetch training item: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PostMapping
  @Audited(action = "CREATE_TRAINING_ITEM", entityType = "TRAINING_ITEM")
  @Operation(summary = "Create a new training item")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Training item created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<?> createTrainingItem(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      Authentication authentication,
      @RequestBody TrainingItemDTO request) {
    String username = getUsername(authentication);
    logger.info("POST /responsibility-centres/" + rcId + "/fiscal-years/" + fyId +
        "/training-items - Creating training item for user: " + username);
    try {
      request.setFiscalYearId(fyId);
      TrainingItemDTO created = trainingItemService.createTrainingItem(request, username);
      return ResponseEntity.status(HttpStatus.CREATED).body(created);
    } catch (IllegalArgumentException e) {
      logger.warning("Failed to create training item: " + e.getMessage());
      return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    } catch (Exception e) {
      logger.severe("Failed to create training item: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorResponse("Failed to create training item"));
    }
  }

  @PutMapping("/{trainingItemId}")
  @Audited(action = "UPDATE_TRAINING_ITEM", entityType = "TRAINING_ITEM")
  @Operation(summary = "Update a training item")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Training item updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "404", description = "Training item not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<?> updateTrainingItem(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      @PathVariable Long trainingItemId,
      Authentication authentication,
      @RequestBody TrainingItemDTO request) {
    String username = getUsername(authentication);
    logger.info("PUT /responsibility-centres/" + rcId + "/fiscal-years/" + fyId +
        "/training-items/" + trainingItemId + " - Updating training item for user: " + username);
    try {
      TrainingItemDTO updated = trainingItemService.updateTrainingItem(trainingItemId, request, username);
      return ResponseEntity.ok(updated);
    } catch (IllegalArgumentException e) {
      logger.warning("Failed to update training item: " + e.getMessage());
      if (e.getMessage().contains("not found")) {
        return ResponseEntity.notFound().build();
      }
      return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    } catch (Exception e) {
      logger.severe("Failed to update training item: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorResponse("Failed to update training item"));
    }
  }

  @DeleteMapping("/{trainingItemId}")
  @Audited(action = "DELETE_TRAINING_ITEM", entityType = "TRAINING_ITEM")
  @Operation(summary = "Delete a training item")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Training item deleted successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "404", description = "Training item not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<?> deleteTrainingItem(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      @PathVariable Long trainingItemId,
      Authentication authentication) {
    String username = getUsername(authentication);
    logger.info("DELETE /responsibility-centres/" + rcId + "/fiscal-years/" + fyId +
        "/training-items/" + trainingItemId + " - Deleting training item for user: " + username);
    try {
      trainingItemService.deleteTrainingItem(trainingItemId, username);
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException e) {
      logger.warning("Failed to delete training item: " + e.getMessage());
      if (e.getMessage().contains("not found")) {
        return ResponseEntity.notFound().build();
      }
      return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    } catch (Exception e) {
      logger.severe("Failed to delete training item: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorResponse("Failed to delete training item"));
    }
  }

  @PutMapping("/{trainingItemId}/status")
  @Audited(action = "UPDATE_TRAINING_ITEM_STATUS", entityType = "TRAINING_ITEM")
  @Operation(summary = "Update training item status")
  public ResponseEntity<?> updateStatus(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      @PathVariable Long trainingItemId,
      Authentication authentication,
      @RequestBody StatusUpdateRequest request) {
    String username = getUsername(authentication);
    try {
      TrainingItemDTO updated = trainingItemService.updateTrainingItemStatus(trainingItemId, request.status, username);
      return ResponseEntity.ok(updated);
    } catch (IllegalArgumentException e) {
      if (e.getMessage().contains("not found")) {
        return ResponseEntity.notFound().build();
      }
      return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorResponse("Failed to update training item status"));
    }
  }

  @GetMapping("/{trainingItemId}/allocations")
  @Operation(summary = "Get money allocations for a training item")
  public ResponseEntity<List<TrainingMoneyAllocationDTO>> getAllocations(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      @PathVariable Long trainingItemId,
      Authentication authentication) {
    String username = getUsername(authentication);
    try {
      List<TrainingMoneyAllocationDTO> allocations = trainingItemService.getMoneyAllocations(trainingItemId, username);
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

  @PutMapping("/{trainingItemId}/allocations")
  @Audited(action = "UPDATE_TRAINING_ALLOCATIONS", entityType = "TRAINING_ITEM")
  @Operation(summary = "Update money allocations for a training item")
  public ResponseEntity<?> updateAllocations(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      @PathVariable Long trainingItemId,
      Authentication authentication,
      @RequestBody List<TrainingMoneyAllocationDTO> allocations) {
    String username = getUsername(authentication);
    try {
      TrainingItemDTO updated = trainingItemService.updateMoneyAllocations(trainingItemId, allocations, username);
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

  // ========== Participant endpoints ==========

  @GetMapping("/{trainingItemId}/participants")
  @Operation(summary = "Get participants for a training item")
  public ResponseEntity<List<TrainingParticipantDTO>> getParticipants(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      @PathVariable Long trainingItemId,
      Authentication authentication) {
    String username = getUsername(authentication);
    try {
      List<TrainingParticipantDTO> participants = trainingItemService.getParticipants(trainingItemId, username);
      return ResponseEntity.ok(participants);
    } catch (IllegalArgumentException e) {
      if (e.getMessage().contains("not found")) {
        return ResponseEntity.notFound().build();
      }
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PostMapping("/{trainingItemId}/participants")
  @Audited(action = "ADD_TRAINING_PARTICIPANT", entityType = "TRAINING_ITEM")
  @Operation(summary = "Add a participant to a training item")
  public ResponseEntity<?> addParticipant(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      @PathVariable Long trainingItemId,
      Authentication authentication,
      @RequestBody TrainingParticipantDTO participantDTO) {
    String username = getUsername(authentication);
    try {
      TrainingParticipantDTO created = trainingItemService.addParticipant(trainingItemId, participantDTO, username);
      return ResponseEntity.status(HttpStatus.CREATED).body(created);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorResponse("Failed to add participant"));
    }
  }

  @PutMapping("/{trainingItemId}/participants/{participantId}")
  @Audited(action = "UPDATE_TRAINING_PARTICIPANT", entityType = "TRAINING_ITEM")
  @Operation(summary = "Update a participant in a training item")
  public ResponseEntity<?> updateParticipant(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      @PathVariable Long trainingItemId,
      @PathVariable Long participantId,
      Authentication authentication,
      @RequestBody TrainingParticipantDTO participantDTO) {
    String username = getUsername(authentication);
    try {
      TrainingParticipantDTO updated = trainingItemService.updateParticipant(trainingItemId, participantId, participantDTO, username);
      return ResponseEntity.ok(updated);
    } catch (IllegalArgumentException e) {
      if (e.getMessage().contains("not found")) {
        return ResponseEntity.notFound().build();
      }
      return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorResponse("Failed to update participant"));
    }
  }

  @DeleteMapping("/{trainingItemId}/participants/{participantId}")
  @Audited(action = "DELETE_TRAINING_PARTICIPANT", entityType = "TRAINING_ITEM")
  @Operation(summary = "Delete a participant from a training item")
  public ResponseEntity<?> deleteParticipant(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      @PathVariable Long trainingItemId,
      @PathVariable Long participantId,
      Authentication authentication) {
    String username = getUsername(authentication);
    try {
      trainingItemService.deleteParticipant(trainingItemId, participantId, username);
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException e) {
      if (e.getMessage().contains("not found")) {
        return ResponseEntity.notFound().build();
      }
      return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorResponse("Failed to delete participant"));
    }
  }

  public static class StatusUpdateRequest {
    public String status;
  }
}
