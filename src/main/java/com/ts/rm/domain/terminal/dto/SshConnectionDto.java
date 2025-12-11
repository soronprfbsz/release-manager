package com.ts.rm.domain.terminal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SSH 연결 정보 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "SSH 연결 정보")
public class SshConnectionDto {
    @Schema(description = "호스트 주소", example = "192.168.1.100")
    @NotBlank(message = "호스트는 필수입니다")
    private String host;

    @Schema(description = "SSH 포트", example = "22")
    @NotNull(message = "포트는 필수입니다")
    @Min(value = 1, message = "포트는 1 이상이어야 합니다")
    @Max(value = 65535, message = "포트는 65535 이하여야 합니다")
    private Integer port;

    @Schema(description = "사용자명", example = "deploy")
    @NotBlank(message = "사용자명은 필수입니다")
    private String username;

    @Schema(description = "비밀번호")
    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;
}
