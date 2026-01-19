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
public class FiscalYearDTO {
    private Long id;
    private String name;
    private Long rcId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
