package com.ts.rm.domain.account.controller;

import com.ts.rm.domain.account.dto.AccountDto;
import com.ts.rm.domain.account.enums.AccountStatus;
import com.ts.rm.domain.account.service.AccountService;
import com.ts.rm.global.common.response.ApiResponse;
import com.ts.rm.global.common.response.SwaggerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Account", description = "계정 관리 API")
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@SwaggerResponse
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "계정 생성", description = "새로운 계정을 생성합니다")
    @PostMapping
    public ResponseEntity<ApiResponse<AccountDto.DetailResponse>> createAccount(
            @Valid @RequestBody AccountDto.CreateRequest request) {
        AccountDto.DetailResponse response = accountService.createAccount(
                request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @Operation(summary = "계정 조회", description = "ID로 계정 정보를 조회합니다")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountDto.DetailResponse>> getAccount(
            @Parameter(description = "계정 ID", required = true) @PathVariable Long id) {
        AccountDto.DetailResponse response = accountService.getAccountByAccountId(
                id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "계정 목록 조회",
            description = "계정 목록을 조회합니다. status로 상태 필터링, keyword로 계정명 검색 가능")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountDto.SimpleResponse>>> getAccounts(
            @Parameter(description = "계정 상태 (ACTIVE, INACTIVE 등)")
            @RequestParam(required = false) AccountStatus status,
            @Parameter(description = "계정명 검색 키워드")
            @RequestParam(required = false) String keyword) {
        List<AccountDto.SimpleResponse> response = accountService.getAccounts(status, keyword);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "계정 수정", description = "계정 정보를 수정합니다")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountDto.DetailResponse>> updateAccount(
            @Parameter(description = "계정 ID", required = true) @PathVariable Long id,
            @Valid @RequestBody AccountDto.UpdateRequest request) {
        AccountDto.DetailResponse response = accountService.updateAccount(id,
                request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "계정 삭제", description = "계정을 삭제합니다 (Soft Delete)")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @Parameter(description = "계정 ID", required = true) @PathVariable Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "계정 상태 변경", description = "계정의 활성화 상태를 변경합니다")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateAccountStatus(
            @Parameter(description = "계정 ID", required = true) @PathVariable Long id,
            @Parameter(description = "계정 상태", required = true) @RequestParam AccountStatus status) {
        accountService.updateAccountStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
