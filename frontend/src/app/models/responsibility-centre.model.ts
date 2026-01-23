/*
 * Cinema Box Office - Responsibility Centre DTO Model
 * Copyright (c) 2026 Box Office Team
 * Licensed under MIT License
 */

export interface ResponsibilityCentreDTO {
  id: number;
  name: string;
  description?: string;
  ownerUsername: string;
  accessLevel: 'READ_ONLY' | 'READ_WRITE';
  isOwner: boolean;  // Backend uses @JsonProperty("isOwner")
  createdAt?: string;
  updatedAt?: string;
}
