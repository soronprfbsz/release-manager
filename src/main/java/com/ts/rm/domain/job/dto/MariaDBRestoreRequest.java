package com.ts.rm.domain.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * MariaDB 복원 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "MariaDB 복원 요청")
public class MariaDBRestoreRequest {

    @NotBlank(message = "컨테이너 이름은 필수입니다")
    @Schema(description = "MariaDB Docker 컨테이너 이름", example = "release-manager-mariadb", required = true, defaultValue = "release-manager-mariadb")
    @Builder.Default
    private String containerName = "release-manager-mariadb";

    @NotBlank(message = "호스트는 필수입니다")
    @Schema(description = "MariaDB 호스트 주소", example = "192.168.1.100", required = true)
    private String host;

    @NotNull(message = "포트는 필수입니다")
    @Min(value = 1, message = "포트는 1 이상이어야 합니다")
    @Max(value = 65535, message = "포트는 65535 이하여야 합니다")
    @Schema(description = "MariaDB 포트", example = "3306", required = true)
    private Integer port;

    @NotBlank(message = "사용자명은 필수입니다")
    @Schema(description = "MariaDB 사용자명", example = "root", required = true)
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Schema(description = "MariaDB 비밀번호", example = "password", required = true)
    private String password;

    @NotBlank(message = "백업 파일명은 필수입니다")
    @Schema(description = "복원할 백업 파일명", example = "backup_20251203_171500.sql", required = true)
    private String backupFileName;
}
