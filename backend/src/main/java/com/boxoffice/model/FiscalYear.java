package com.boxoffice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "fiscal_years", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "rc_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiscalYear {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rc_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ResponsibilityCentre rc;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
