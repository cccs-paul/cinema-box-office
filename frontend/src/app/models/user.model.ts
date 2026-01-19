/*
 * Cinema Box Office - User Model
 * Copyright (c) 2026 Box Office Team
 * Licensed under MIT License
 */

/**
 * User model representing an authenticated user.
 *
 * @author Box Office Team
 * @version 1.0.0
 * @since 2026-01-17
 */
export interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
  authProvider: 'LOCAL' | 'LDAP' | 'OAUTH2';
  enabled: boolean;
  accountLocked: boolean;
  emailVerified: boolean;
  roles: string[];
  profileDescription: string;
  lastLoginAt: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * Authentication response from API.
 */
export interface AuthResponse {
  user: User;
  token?: string;
}
