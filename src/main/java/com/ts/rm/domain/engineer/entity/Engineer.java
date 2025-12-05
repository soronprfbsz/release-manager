package com.ts.rm.domain.engineer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "engineer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Engineer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "engineer_id")
    private Long engineerId;

    @Column(name = "engineer_name", nullable = false, length = 50)
    private String engineerName;

    @Column(name = "engineer_email", nullable = false, unique = true, length = 100)
    private String engineerEmail;

    @Column(name = "engineer_phone", length = 20)
    private String engineerPhone;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "description", length = 500)
    private String description;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", nullable = false, length = 100)
    private String updatedBy;
}
