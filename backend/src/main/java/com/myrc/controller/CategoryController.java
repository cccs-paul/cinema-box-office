/*
 * myRC - Category REST Controller
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 *
 * Author: myRC Team
 * Date: 2026-01-27
 * Version: 1.0.0
 *
 * Description:
 * REST Controller for Category management.
 */
package com.myrc.controller;

import com.myrc.dto.CategoryDTO;
import com.myrc.dto.ErrorResponse;
import com.myrc.model.FundingType;
import com.myrc.service.CategoryService;
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
 * REST Controller for Category management.
 * Categories are used to group both funding and spending items within a fiscal year.
 * Default categories are read-only; only custom categories can be added, renamed, and deleted.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-27
 */
@RestController
@RequestMapping("/responsibility-centres/{rcId}/fiscal-years/{fyId}/categories")
@Tag(name = "Category Management", description = "APIs for managing categories within fiscal years")
public class CategoryController {

  private static final Logger logger = Logger.getLogger(CategoryController.class.getName());
  private final CategoryService categoryService;

  public CategoryController(CategoryService categoryService) {
    this.categoryService = categoryService;
  }

  /**
   * Get all categories for a fiscal year.
   *
   * @param rcId the responsibility centre ID
   * @param fyId the fiscal year ID
   * @param authentication the authentication principal
   * @return list of categories
   */
  @GetMapping
  @Operation(summary = "Get all categories for a fiscal year",
      description = "Retrieves all categories configured for a fiscal year.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Access denied to this RC"),
      @ApiResponse(responseCode = "404", description = "Fiscal year not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<List<CategoryDTO>> getCategories(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      Authentication authentication) {
    String username = getUsername(authentication);
    logger.info("GET /responsibility-centres/" + rcId + "/fiscal-years/" + fyId + 
        "/categories - Fetching categories for user: " + username);

    try {
      List<CategoryDTO> categories = categoryService.getCategoriesByFiscalYearId(fyId, username);
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
   * Get a specific category by ID.
   *
   * @param rcId the responsibility centre ID
   * @param fyId the fiscal year ID
   * @param categoryId the category ID
   * @param authentication the authentication principal
   * @return the category
   */
  @GetMapping("/{categoryId}")
  @Operation(summary = "Get a specific category",
      description = "Retrieves a specific category by ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Category retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "Category not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<CategoryDTO> getCategory(
      @PathVariable Long rcId,
      @PathVariable Long fyId,
      @PathVariable Long categoryId,
      Authentication authentication) {
    String username = getUsername(authentication);
    logger.info("GET /responsibility-centres/" + rcId + "/fiscal-years/" + fyId + 
        "/categories/" + categoryId + " - Fetching category for user: " + username);

    try {
      Optional<CategoryDTO> categoryOpt = categoryService.getCategoryById(categoryId, username);
      return categoryOpt.map(ResponseEntity::ok)
          .orElseGet(() -> ResponseEntity.notFound().build());
    } catch (Exception e) {
      logger.severe("Failed to fetch category: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Create a new category for a fiscal year.
   * Note: Only custom categories can be created. Default categories are system-managed.
   *
   * @param rcId the responsibility centre ID
   * @param fyId the fiscal year ID
   * @param authentication the authentication principal
   * @param request the category creation request
   * @return the created category
   */
  @PostMapping
  @Operation(summary = "Create a new category",
      description = "Creates a new custom category for a fiscal year. Default categories cannot be created.")
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
        "/categories - Creating category for user: " + username);

    try {
      FundingType fundingType = null;
      if (request.fundingType != null && !request.fundingType.isEmpty()) {
        try {
          fundingType = FundingType.valueOf(request.fundingType);
        } catch (IllegalArgumentException e) {
          return ResponseEntity.badRequest().body(new ErrorResponse("Invalid funding type. Must be CAP_ONLY, OM_ONLY, or BOTH"));
        }
      }
      CategoryDTO created = categoryService.createCategory(
          fyId, username, request.name, request.description, fundingType);
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
   * Update an existing category.
   * Note: Only custom categories can be updated. Default categories are read-only.
   *
   * @param rcId the responsibility centre ID
   * @param fyId the fiscal year ID
   * @param categoryId the category ID
   * @param authentication the authentication principal
   * @param request the category update request
   * @return the updated category
   */
  @PutMapping("/{categoryId}")
  @Operation(summary = "Update a category",
      description = "Updates an existing custom category. Default categories are read-only and cannot be modified.")
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
        "/categories/" + categoryId + " - Updating category for user: " + username);

    try {
      FundingType fundingType = null;
      if (request.fundingType != null && !request.fundingType.isEmpty()) {
        try {
          fundingType = FundingType.valueOf(request.fundingType);
        } catch (IllegalArgumentException e) {
          return ResponseEntity.badRequest().body(new ErrorResponse("Invalid funding type. Must be CAP_ONLY, OM_ONLY, or BOTH"));
        }
      }
      CategoryDTO updated = categoryService.updateCategory(
          categoryId, username, request.name, request.description, fundingType);
      return ResponseEntity.ok(updated);
    } catch (IllegalArgumentException e) {
      logger.warning("Failed to update category: " + e.getMessage());
      if (e.getMessage().contains("not found")) {
        return ResponseEntity.notFound().build();
      }
      if (e.getMessage().contains("default") || e.getMessage().contains("read-only")) {
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
   * Delete a category.
   * Note: Only custom categories can be deleted. Default categories cannot be deleted.
   *
   * @param rcId the responsibility centre ID
   * @param fyId the fiscal year ID
   * @param categoryId the category ID
   * @param authentication the authentication principal
   * @return no content on success
   */
  @DeleteMapping("/{categoryId}")
  @Operation(summary = "Delete a category",
      description = "Deletes a custom category. Default categories cannot be deleted.")
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
        "/categories/" + categoryId + " - Deleting category for user: " + username);

    try {
      categoryService.deleteCategory(categoryId, username);
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException e) {
      logger.warning("Failed to delete category: " + e.getMessage());
      if (e.getMessage().contains("not found")) {
        return ResponseEntity.notFound().build();
      }
      if (e.getMessage().contains("default") || e.getMessage().contains("read-only")) {
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
      description = "Creates the default categories if they don't exist")
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
        "/categories/ensure-defaults - Ensuring defaults for user: " + username);

    try {
      List<CategoryDTO> categories = categoryService.ensureDefaultCategoriesExist(fyId, username);
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
   * Reorder categories.
   *
   * @param rcId the responsibility centre ID
   * @param fyId the fiscal year ID
   * @param authentication the authentication principal
   * @param request the reorder request
   * @return the reordered categories
   */
  @PostMapping("/reorder")
  @Operation(summary = "Reorder categories",
      description = "Updates the display order of categories")
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
        "/categories/reorder - Reordering categories for user: " + username);

    try {
      List<CategoryDTO> categories = categoryService.reorderCategories(fyId, username, request.categoryIds);
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
    public String fundingType;
  }

  public static class CategoryUpdateRequest {
    public String name;
    public String description;
    public String fundingType;
  }

  public static class ReorderRequest {
    public List<Long> categoryIds;
  }
}
