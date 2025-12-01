package com.ts.rm.domain.common.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Code 엔티티 복합키 클래스
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CodeId implements Serializable {

    private String codeTypeId;
    private String codeId;
}
