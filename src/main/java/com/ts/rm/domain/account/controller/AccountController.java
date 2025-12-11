package com.ts.rm.domain.account.controller;

import com.ts.rm.domain.account.dto.AccountDto;
import com.ts.rm.domain.account.enums.AccountStatus;
import com.ts.rm.domain.account.service.AccountService;
import com.ts.rm.global.response.ApiResponse;
import com.ts.rm.global.security.RoleValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
public class AccountController implements AccountControllerDocs {

    private final AccountService accountService;

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AccountDto.ListResponse>>> getAccounts(
            @RequestParam(required = false) AccountStatus status,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "accountId", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/accounts - status: {}, keyword: {}, pageable: {}", status, keyword, pageable);

        Page<AccountDto.ListResponse> accountPage = accountService.getAccounts(status, keyword, pageable);

        log.info("Found {} accounts (page {}/{})", accountPage.getNumberOfElements(),
                accountPage.getNumber() + 1, accountPage.getTotalPages());
        return ResponseEntity.ok(ApiResponse.success(accountPage));
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountDto.DetailResponse>> updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody AccountDto.AdminUpdateRequest request) {
        log.info("PUT /api/accounts/{} - request: {}", id, request);

        // ADMIN 권한 체크
        RoleValidator.requireAdmin();

        AccountDto.DetailResponse response = accountService.adminUpdateAccount(id, request);

        log.info("Account updated successfully - accountId: {}", id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@PathVariable Long id) {
        log.info("DELETE /api/accounts/{}", id);

        // ADMIN 권한 체크
        RoleValidator.requireAdmin();

        accountService.deleteAccount(id);

        log.info("Account deleted successfully - accountId: {}", id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
