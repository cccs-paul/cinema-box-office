package com.boxoffice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponsibilityCentreDTO {
    private Long id;
    private String name;
    private String description;
    private String ownerUsername;
    private String accessLevel;
    private boolean isOwner;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
