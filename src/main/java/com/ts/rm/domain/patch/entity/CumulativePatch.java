package com.ts.rm.domain.patch.entity;

import com.ts.rm.domain.customer.entity.Customer;
import com.ts.rm.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cumulative_patch_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CumulativePatch extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cumulative_patch_id")
    private Long cumulativePatchId;

    @Column(name = "release_type", nullable = false, length = 20)
    private String releaseType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "from_version", nullable = false, length = 50)
    private String fromVersion;

    @Column(name = "to_version", nullable = false, length = 50)
    private String toVersion;

    @Column(name = "patch_name", nullable = false, length = 100)
    private String patchName;

    @Column(name = "output_path", nullable = false, length = 500)
    private String outputPath;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "generated_by", nullable = false, length = 100)
    private String generatedBy;

    @Column(length = 20)
    @Builder.Default
    private String status = "SUCCESS";

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
