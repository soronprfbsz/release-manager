package com.ts.rm.domain.remote.dto.request;

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
 * MariaDB 백업 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "MariaDB 원격 백업 요청")
public class MariaDBBackupRequest {

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

    @NotBlank(message = "데이터베이스명은 필수입니다")
    @Schema(description = "백업할 데이터베이스명", example = "NMS_DB", required = true)
    private String database;

    @Schema(description = "백업 설명 (선택사항)", example = "월간 정기 백업")
    private String description;

    @Schema(description = "출력 파일명 (선택사항, 미지정 시 자동 생성)", example = "nms_db_backup_20251204.sql")
    private String outputFileName;
}
