package com.ts.rm.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 회원가입 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "회원가입 요청")
public class SignUpRequest {

    @Schema(description = "이메일", example = "user@example.com")
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Size(max = 50, message = "이메일은 최대 50자까지 입력 가능합니다.")
    private String email;

    @Schema(description = "비밀번호", example = "password123!")
    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하로 입력해주세요.")
    private String password;

    @Schema(description = "계정 이름", example = "홍길동")
    @NotBlank(message = "계정 이름은 필수 입력 항목입니다.")
    @Size(max = 50, message = "계정 이름은 최대 50자까지 입력 가능합니다.")
    private String accountName;
}
