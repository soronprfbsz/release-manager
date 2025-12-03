package com.ts.rm.domain.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Code DTO 통합 클래스
 */
public class CodeDto {

    private CodeDto() {
    }

    /**
     * 코드 간단 응답 (셀렉트박스, 라디오버튼 등에서 사용)
     */
    @Schema(description = "코드 간단 응답")
    public record SimpleResponse(
            @Schema(description = "코드 값 (value)", example = "PATCH")
            String value,

            @Schema(description = "코드 이름 (표시용 label)", example = "패치본")
            String name,

            @Schema(description = "정렬 순서", example = "1")
            Integer sortOrder
    ) {

    }
}
