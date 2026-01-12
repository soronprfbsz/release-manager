package com.ts.rm.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 회원가입 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "회원가입 응답")
public class SignUpResponse {

    @Schema(description = "계정 ID", example = "1")
    private Long accountId;

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "계정 이름", example = "홍길동")
    private String accountName;

    @Schema(description = "연락처", example = "010-1234-5678")
    private String phone;

    @Schema(description = "직급 코드", example = "MANAGER")
    private String position;

    @Schema(description = "직급명", example = "과장")
    private String positionName;

    @Schema(description = "권한", example = "USER")
    private String role;

    @Schema(description = "계정 생성일시", example = "2025-11-24T10:00:00")
    private LocalDateTime createdAt;
}
