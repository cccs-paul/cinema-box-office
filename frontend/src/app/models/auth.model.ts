/*
 * myRC - Authentication Models
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */

/**
 * App account configuration from server.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-20
 */
export interface AppAccountConfig {
  enabled: boolean;
  allowRegistration: boolean;
}

/**
 * Login methods configuration from server.
 * Indicates which authentication methods are enabled.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-20
 */
export interface LoginMethods {
  appAccount: AppAccountConfig;
  ldapEnabled: boolean;
  oauth2Enabled: boolean;
}

/**
 * Registration request data.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-20
 */
export interface RegistrationRequest {
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
  fullName?: string;
}

/**
 * Registration response from server.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-20
 */
export interface RegistrationResponse {
  success: boolean;
  message: string;
  userId?: number;
}

/**
 * Username availability check response.
 *
 * @author myRC Team
 * @version 1.0.0
 * @since 2026-01-20
 */
export interface UsernameAvailabilityResponse {
  available: boolean;
  message: string;
}
