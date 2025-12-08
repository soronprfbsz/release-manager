package com.ts.rm.domain.account.controller;

import com.ts.rm.domain.account.dto.AccountDto;
import com.ts.rm.domain.account.enums.AccountStatus;
import com.ts.rm.domain.account.service.AccountService;
import com.ts.rm.global.response.ApiResponse;
import com.ts.rm.global.security.RoleValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 계정 관리 API
 *
 * <p>계정 목록 조회, 수정, 삭제 기능 제공
 * <p>수정 및 삭제는 ADMIN 권한 필수
 */
@Slf4j
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "계정", description = "계정 관리 API")
public class AccountController {

    private final AccountService accountService;

    /**
     * 계정 목록 조회
     *
     * @param status  계정 상태 필터 (선택사항: ACTIVE, INACTIVE)
     * @param keyword 계정명 검색 키워드 (선택사항)
     * @return 계정 목록
     */
    @GetMapping
    @Operation(summary = "계정 목록 조회", description = "등록된 모든 계정 목록을 조회합니다. 상태 필터 및 계정명 검색을 지원합니다.")
    public ResponseEntity<ApiResponse<List<AccountDto.SimpleResponse>>> getAccounts(
            @Parameter(description = "계정 상태 (ACTIVE, INACTIVE)", example = "ACTIVE")
            @RequestParam(required = false) AccountStatus status,
            @Parameter(description = "계정명 검색 키워드", example = "홍길동")
            @RequestParam(required = false) String keyword) {
        log.info("GET /api/accounts - status: {}, keyword: {}", status, keyword);

        List<AccountDto.SimpleResponse> accounts = accountService.getAccounts(status, keyword);

        log.info("Found {} accounts", accounts.size());
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    /**
     * 계정 수정 (ADMIN 전용)
     *
     * <p>비밀번호, 권한(role), 상태(status)를 수정할 수 있습니다.
     *
     * @param accountId 계정 ID
     * @param request   수정 요청 (password, role, status)
     * @return 수정된 계정 상세 정보
     */
    @PutMapping("/{accountId}")
    @Operation(summary = "계정 수정 (ADMIN 전용)", description = "특정 계정의 비밀번호, 권한, 상태를 수정합니다. ADMIN 권한이 필요합니다.")
    public ResponseEntity<ApiResponse<AccountDto.DetailResponse>> updateAccount(
            @Parameter(description = "계정 ID", example = "1", required = true)
            @PathVariable Long accountId,
            @Valid @RequestBody AccountDto.AdminUpdateRequest request) {
        log.info("PUT /api/accounts/{} - request: {}", accountId, request);

        // ADMIN 권한 체크
        RoleValidator.requireAdmin();

        AccountDto.DetailResponse response = accountService.adminUpdateAccount(accountId, request);

        log.info("Account updated successfully - accountId: {}", accountId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 계정 삭제 (ADMIN 전용)
     *
     * @param accountId 계정 ID
     * @return 삭제 완료 응답
     */
    @DeleteMapping("/{accountId}")
    @Operation(summary = "계정 삭제 (ADMIN 전용)", description = "특정 계정을 삭제합니다. ADMIN 권한이 필요합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @Parameter(description = "계정 ID", example = "1", required = true)
            @PathVariable Long accountId) {
        log.info("DELETE /api/accounts/{}", accountId);

        // ADMIN 권한 체크
        RoleValidator.requireAdmin();

        accountService.deleteAccount(accountId);

        log.info("Account deleted successfully - accountId: {}", accountId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
