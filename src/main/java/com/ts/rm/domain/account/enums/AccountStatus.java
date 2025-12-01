package com.ts.rm.domain.account.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 계정 상태 Enum - Swagger에서 드롭다운으로 표시됨
 */
@Schema(description = "계정 상태")
public enum AccountStatus {
    @Schema(description = "활성")
    ACTIVE,

    @Schema(description = "비활성")
    INACTIVE,

    @Schema(description = "정지")
    SUSPENDED
}
