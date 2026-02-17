/*
 * myRC - Responsibility Centre DTO Model
 * Copyright (c) 2026 myRC Team
 * Licensed under MIT License
 */

export interface ResponsibilityCentreDTO {
  id: number;
  name: string;
  description?: string;
  ownerUsername: string;
  accessLevel: 'OWNER' | 'READ_ONLY' | 'READ_WRITE';
  isOwner: boolean;  // Backend uses @JsonProperty("isOwner")
  canEdit?: boolean; // Backend uses @JsonProperty("canEdit")
  trainingEnabled?: boolean;
  travelEnabled?: boolean;
  createdAt?: string;
  updatedAt?: string;
}
