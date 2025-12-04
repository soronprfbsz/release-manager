package com.ts.rm.domain.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * MariaDB 복원 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "MariaDB 복원 요청")
public class MariaDBRestoreRequest {

    @NotNull(message = "백업 파일 ID는 필수입니다")
    @Schema(description = "복원할 백업 파일 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long backupFileId;

    @NotBlank(message = "호스트는 필수입니다")
    @Schema(description = "MariaDB 호스트", example = "192.168.1.100", requiredMode = Schema.RequiredMode.REQUIRED)
    private String host;

    @NotNull(message = "포트는 필수입니다")
    @Schema(description = "MariaDB 포트", example = "3306", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer port;

    @NotBlank(message = "사용자명은 필수입니다")
    @Schema(description = "MariaDB 사용자명", example = "root", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Schema(description = "MariaDB 비밀번호", example = "password", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
