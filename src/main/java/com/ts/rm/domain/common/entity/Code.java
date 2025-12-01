package com.ts.rm.domain.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "code")
@IdClass(CodeId.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Code extends BaseEntity {

    @Id
    @Column(name = "code_type_id", length = 50, insertable = false, updatable = false)
    private String codeTypeId;

    @Id
    @Column(name = "code_id", length = 100)
    private String codeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code_type_id", nullable = false)
    private CodeType codeType;

    @Column(name = "code_name", nullable = false, length = 100)
    private String codeName;

    @Column(length = 200)
    private String description;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private Boolean isEnabled = true;
}
