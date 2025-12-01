package com.ts.rm.domain.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "code_type")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeType extends BaseEntity {

    @Id
    @Column(name = "code_type_id", length = 50)
    private String codeTypeId;

    @Column(name = "code_type_name", nullable = false, length = 100)
    private String codeTypeName;

    @Column(length = 200)
    private String description;

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private Boolean isEnabled = true;

    @OneToMany(mappedBy = "codeType")
    @Builder.Default
    private List<Code> codes = new ArrayList<>();
}
