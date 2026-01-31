/**
 * Error Response DTO
 *
 * Standardized error response format for REST API validation and business logic errors.
 * Used by all controllers to return consistent error messages to the frontend.
 *
 * @author BoxOffice Development Team
 * @version 1.0.0
 * @since 2025-01-14
 */
package com.boxoffice.dto;

import java.time.Instant;

/**
 * Data Transfer Object for API error responses.
 * Provides a consistent structure for error information returned to clients.
 */
public class ErrorResponse {

  /**
   * Human-readable error message describing what went wrong.
   */
  private String message;

  /**
   * Optional error code for programmatic error handling.
   */
  private String errorCode;

  /**
   * Timestamp when the error occurred.
   */
  private Instant timestamp;

  /**
   * Default constructor required for JSON deserialization.
   */
  public ErrorResponse() {
    this.timestamp = Instant.now();
  }

  /**
   * Creates an error response with just a message.
   *
   * @param message The human-readable error message
   */
  public ErrorResponse(String message) {
    this.message = message;
    this.timestamp = Instant.now();
  }

  /**
   * Creates an error response with a message and error code.
   *
   * @param message   The human-readable error message
   * @param errorCode An optional error code for programmatic handling
   */
  public ErrorResponse(String message, String errorCode) {
    this.message = message;
    this.errorCode = errorCode;
    this.timestamp = Instant.now();
  }

  /**
   * Gets the error message.
   *
   * @return The human-readable error message
   */
  public String getMessage() {
    return message;
  }

  /**
   * Sets the error message.
   *
   * @param message The human-readable error message
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * Gets the error code.
   *
   * @return The optional error code
   */
  public String getErrorCode() {
    return errorCode;
  }

  /**
   * Sets the error code.
   *
   * @param errorCode An optional error code for programmatic handling
   */
  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }

  /**
   * Gets the timestamp when the error occurred.
   *
   * @return The error timestamp
   */
  public Instant getTimestamp() {
    return timestamp;
  }

  /**
   * Sets the timestamp when the error occurred.
   *
   * @param timestamp The error timestamp
   */
  public void setTimestamp(Instant timestamp) {
    this.timestamp = timestamp;
  }
}
