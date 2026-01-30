/*
 * myRC - Spending Category REST Controller
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-26
 * Version: 1.0.0
 *
 * Description:
 * REST Controller for Spending Category management.
 */
package com.boxoffice.controller;

import com.boxoffice.dto.ErrorResponse;
import com.boxoffice.dto.SpendingCategoryDTO;
import com.boxoffice.service.SpendingCategoryService;
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
 * REST Controller for Spending Category management.
 * Categories are used to group spending items within a fiscal year.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-26
 */
@RestController
@RequestMapping("/responsibility-centres/{rcId}/fiscal-years/{fyId}/spending-categories")
@Tag(name = "Spending Category Management", description = "APIs for managing spending categories within fiscal years")
public class SpendingCategoryController {

  private static final Logger logger = Logger.getLogger(SpendingCategoryController.class.getName());
  private final SpendingCategoryService categoryService;

  public SpendingCategoryController(SpendingCategoryService categoryService) {
    this.categoryService = categoryService;
  }

  /**
   * Get all spending categories for a fiscal year.
   *
   * @param rcId the responsibility centre ID
   * @param fyId the fiscal year ID
   * @param authentication the authentication principal
   * @return list of spending categories
   */
  @GetMapping
  @Operation(summary = "Get all spending categories for a fiscal year",
      description = "Retrieves all spending categories configured for a fiscal year.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Access denied to this RC"),
      @ApiResponse(responseCode = "404", description = "Fiscal year not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<List<SpendingCategoryDTO>> getCategories(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      Authentication authentication) {
    String username = getUsername(authentication);
    logger.info("GET /responsibility-centres/" + rcId + "/fiscal-years/" + fyId + 
        "/spending-categories - Fetching categories for user: " + username);

    try {
      List<SpendingCategoryDTO> categories = categoryService.getCategoriesByFiscalYearId(fyId, username);
      return ResponseEntity.ok(categories);
    } catch (IllegalArgumentException e) {
      logger.warning("Access denied for categories: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (Exception e) {
      logger.severe("Failed to fetch categories: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get a specific spending category by ID.
   *
   * @param rcId the responsibility centre ID
   * @param fyId the fiscal year ID
   * @param categoryId the category ID
   * @param authentication the authentication principal
   * @return the spending category
   */
  @GetMapping("/{categoryId}")
  @Operation(summary = "Get a specific spending category",
      description = "Retrieves a specific spending category by ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Category retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "Category not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<SpendingCategoryDTO> getCategory(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      @PathVariable Long categoryId,
      Authentication authentication) {
    String username = getUsername(authentication);
    logger.info("GET /responsibility-centres/" + rcId + "/fiscal-years/" + fyId + 
        "/spending-categories/" + categoryId + " - Fetching category for user: " + username);

    try {
      Optional<SpendingCategoryDTO> categoryOpt = categoryService.getCategoryById(categoryId, username);
      return categoryOpt.map(ResponseEntity::ok)
          .orElseGet(() -> ResponseEntity.notFound().build());
    } catch (Exception e) {
      logger.severe("Failed to fetch category: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Create a new spending category for a fiscal year.
   *
   * @param rcId the responsibility centre ID
   * @param fyId the fiscal year ID
   * @param authentication the authentication principal
   * @param request the category creation request
   * @return the created spending category
   */
  @PostMapping
  @Operation(summary = "Create a new spending category",
      description = "Creates a new spending category for a fiscal year")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Category created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "409", description = "Category name already exists"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<?> createCategory(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      Authentication authentication,
      @RequestBody CategoryCreateRequest request) {
    String username = getUsername(authentication);
    logger.info("POST /responsibility-centres/" + rcId + "/fiscal-years/" + fyId + 
        "/spending-categories - Creating category for user: " + username);

    try {
      SpendingCategoryDTO created = categoryService.createCategory(
          fyId, username, request.name, request.description);
      return ResponseEntity.status(HttpStatus.CREATED).body(created);
    } catch (IllegalArgumentException e) {
      logger.warning("Failed to create category: " + e.getMessage());
      if (e.getMessage().contains("already exists")) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(e.getMessage()));
      }
      return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    } catch (Exception e) {
      logger.severe("Failed to create category: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorResponse("Failed to create category"));
    }
  }

  /**
   * Update an existing spending category.
   *
   * @param rcId the responsibility centre ID
   * @param fyId the fiscal year ID
   * @param categoryId the category ID
   * @param authentication the authentication principal
   * @param request the category update request
   * @return the updated spending category
   */
  @PutMapping("/{categoryId}")
  @Operation(summary = "Update a spending category",
      description = "Updates an existing spending category")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Category updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Access denied or cannot modify default category"),
      @ApiResponse(responseCode = "404", description = "Category not found"),
      @ApiResponse(responseCode = "409", description = "Category name already exists"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<?> updateCategory(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      @PathVariable Long categoryId,
      Authentication authentication,
      @RequestBody CategoryUpdateRequest request) {
    String username = getUsername(authentication);
    logger.info("PUT /responsibility-centres/" + rcId + "/fiscal-years/" + fyId + 
        "/spending-categories/" + categoryId + " - Updating category for user: " + username);

    try {
      SpendingCategoryDTO updated = categoryService.updateCategory(
          categoryId, username, request.name, request.description);
      return ResponseEntity.ok(updated);
    } catch (IllegalArgumentException e) {
      logger.warning("Failed to update category: " + e.getMessage());
      if (e.getMessage().contains("not found")) {
        return ResponseEntity.notFound().build();
      }
      if (e.getMessage().contains("default")) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
      }
      if (e.getMessage().contains("already exists")) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(e.getMessage()));
      }
      return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    } catch (Exception e) {
      logger.severe("Failed to update category: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorResponse("Failed to update category"));
    }
  }

  /**
   * Delete a spending category.
   *
   * @param rcId the responsibility centre ID
   * @param fyId the fiscal year ID
   * @param categoryId the category ID
   * @param authentication the authentication principal
   * @return no content on success
   */
  @DeleteMapping("/{categoryId}")
  @Operation(summary = "Delete a spending category",
      description = "Deletes a spending category")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Access denied or cannot delete default category"),
      @ApiResponse(responseCode = "404", description = "Category not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<?> deleteCategory(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      @PathVariable Long categoryId,
      Authentication authentication) {
    String username = getUsername(authentication);
    logger.info("DELETE /responsibility-centres/" + rcId + "/fiscal-years/" + fyId + 
        "/spending-categories/" + categoryId + " - Deleting category for user: " + username);

    try {
      categoryService.deleteCategory(categoryId, username);
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException e) {
      logger.warning("Failed to delete category: " + e.getMessage());
      if (e.getMessage().contains("not found")) {
        return ResponseEntity.notFound().build();
      }
      if (e.getMessage().contains("default")) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
      }
      return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    } catch (Exception e) {
      logger.severe("Failed to delete category: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorResponse("Failed to delete category"));
    }
  }

  /**
   * Ensure default categories exist for a fiscal year.
   *
   * @param rcId the responsibility centre ID
   * @param fyId the fiscal year ID
   * @param authentication the authentication principal
   * @return list of all categories including defaults
   */
  @PostMapping("/ensure-defaults")
  @Operation(summary = "Ensure default categories exist",
      description = "Creates the default spending categories if they don't exist")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Default categories ensured"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<?> ensureDefaults(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      Authentication authentication) {
    String username = getUsername(authentication);
    logger.info("POST /responsibility-centres/" + rcId + "/fiscal-years/" + fyId + 
        "/spending-categories/ensure-defaults - Ensuring defaults for user: " + username);

    try {
      List<SpendingCategoryDTO> categories = categoryService.ensureDefaultCategoriesExist(fyId, username);
      return ResponseEntity.ok(categories);
    } catch (IllegalArgumentException e) {
      logger.warning("Failed to ensure defaults: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
    } catch (Exception e) {
      logger.severe("Failed to ensure defaults: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorResponse("Failed to ensure default categories"));
    }
  }

  /**
   * Reorder spending categories.
   *
   * @param rcId the responsibility centre ID
   * @param fyId the fiscal year ID
   * @param authentication the authentication principal
   * @param request the reorder request
   * @return the reordered categories
   */
  @PostMapping("/reorder")
  @Operation(summary = "Reorder spending categories",
      description = "Updates the display order of spending categories")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Categories reordered successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Access denied"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<?> reorderCategories(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      Authentication authentication,
      @RequestBody ReorderRequest request) {
    String username = getUsername(authentication);
    logger.info("POST /responsibility-centres/" + rcId + "/fiscal-years/" + fyId + 
        "/spending-categories/reorder - Reordering categories for user: " + username);

    try {
      List<SpendingCategoryDTO> categories = categoryService.reorderCategories(fyId, username, request.categoryIds);
      return ResponseEntity.ok(categories);
    } catch (IllegalArgumentException e) {
      logger.warning("Failed to reorder categories: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
    } catch (Exception e) {
      logger.severe("Failed to reorder categories: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ErrorResponse("Failed to reorder categories"));
    }
  }

  /**
   * Extract username from authentication.
   */
  private String getUsername(Authentication authentication) {
    if (authentication != null && authentication.getName() != null) {
      return authentication.getName();
    }
    return "anonymous";
  }

  // Request DTOs
  public static class CategoryCreateRequest {
    public String name;
    public String description;
  }

  public static class CategoryUpdateRequest {
    public String name;
    public String description;
  }

  public static class ReorderRequest {
    public List<Long> categoryIds;
  }
}
