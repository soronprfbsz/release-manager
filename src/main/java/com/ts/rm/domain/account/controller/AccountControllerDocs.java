package com.ts.rm.domain.account.controller;

import com.ts.rm.domain.account.dto.AccountDto;
import com.ts.rm.domain.account.enums.AccountStatus;
import com.ts.rm.global.response.ApiResponse;
import com.ts.rm.global.response.SwaggerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * AccountController Swagger 문서화 인터페이스
 */
@Tag(name = "계정", description = "계정 관리 API (ADMIN 전용)")
@SwaggerResponse
public interface AccountControllerDocs {

    @Operation(
            summary = "계정 목록 조회 (페이징)",
            description = "등록된 계정 목록을 페이징으로 조회합니다. 상태 필터 및 계정명 검색을 지원합니다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountListResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<Page<AccountDto.ListResponse>>> getAccounts(
            @Parameter(description = "계정 상태 (ACTIVE, INACTIVE)", example = "ACTIVE")
            @RequestParam(required = false) AccountStatus status,
            @Parameter(description = "계정명 검색 키워드", example = "홍길동")
            @RequestParam(required = false) String keyword,
            Pageable pageable
    );

    @Operation(
            summary = "계정 수정 (ADMIN 전용)",
            description = "계정의 권한(role), 상태(status)를 수정합니다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountDetailResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<AccountDto.DetailResponse>> updateAccount(
            @Parameter(description = "계정 ID", example = "1")
            @PathVariable Long id,
            @RequestBody AccountDto.AdminUpdateRequest request
    );

    @Operation(
            summary = "계정 삭제 (ADMIN 전용)",
            description = "계정을 삭제합니다. 마지막 ADMIN 계정은 삭제할 수 없습니다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    example = "{\"status\": \"success\", \"data\": null}"
                            )
                    )
            )
    )
    ResponseEntity<ApiResponse<Void>> deleteAccount(
            @Parameter(description = "계정 ID", example = "1")
            @PathVariable Long id
    );

    /**
     * Swagger 스키마용 wrapper 클래스 - 계정 목록 (페이징)
     */
    @Schema(description = "계정 목록 API 응답 (페이징)")
    class AccountListResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "페이징된 계정 목록")
        public PageData data;

        @Schema(description = "페이지 데이터")
        static class PageData {
            @Schema(description = "계정 목록")
            public List<AccountDto.ListResponse> content;

            @Schema(description = "전체 페이지 수", example = "1")
            public int totalPages;

            @Schema(description = "전체 요소 수", example = "5")
            public long totalElements;

            @Schema(description = "페이지 크기", example = "20")
            public int size;

            @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
            public int number;

            @Schema(description = "현재 페이지의 요소 수", example = "5")
            public int numberOfElements;

            @Schema(description = "첫 페이지 여부", example = "true")
            public boolean first;

            @Schema(description = "마지막 페이지 여부", example = "true")
            public boolean last;

            @Schema(description = "비어있는 페이지 여부", example = "false")
            public boolean empty;
        }
    }

    /**
     * Swagger 스키마용 wrapper 클래스 - 계정 상세
     */
    @Schema(description = "계정 상세 API 응답")
    class AccountDetailResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "계정 상세 정보")
        public AccountDto.DetailResponse data;
    }
}
