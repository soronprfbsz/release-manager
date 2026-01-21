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
@Tag(name = "계정", description = "계정 관리 API")
@SwaggerResponse
public interface AccountControllerDocs {

    @Operation(
            summary = "내 정보 조회",
            description = "현재 로그인한 사용자의 계정 정보를 조회합니다. JWT 토큰 기반으로 본인 확인합니다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountDetailResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<AccountDto.DetailResponse>> getMyAccount();

    @Operation(
            summary = "내 정보 수정",
            description = "현재 로그인한 사용자의 이름, 비밀번호를 수정합니다. JWT 토큰 기반으로 본인만 수정 가능합니다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountDetailResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<AccountDto.DetailResponse>> updateMyAccount(
            @RequestBody AccountDto.UpdateRequest request
    );

    @Operation(
            summary = "계정 목록 조회 (페이징)",
            description = """
                    등록된 계정 목록을 페이징으로 조회합니다. 상태, 부서, 부서유형, 키워드 필터를 지원합니다.

                    **부서 필터링 옵션:**
                    - `departmentId` 미지정: 전체 계정 조회
                    - `departmentId=6`: 부서 ID가 6인 계정만 조회
                    - `departmentId=6&includeSubDepartments=true`: 부서 6 + 하위 부서의 모든 계정 조회
                    - `departmentId=null`: 부서 미배치 계정만 조회

                    **부서 유형 필터링 옵션:**
                    - `departmentType=DEVELOPMENT`: 개발 부서 소속 계정만 조회
                    - `departmentType=ENGINEER`: 엔지니어 부서 소속 계정만 조회
                    """,
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
            @Parameter(description = "부서 ID (숫자: 해당 부서, 'null': 미배치 계정)", example = "6")
            @RequestParam(required = false) String departmentId,
            @Parameter(description = "하위 부서 포함 여부 (true: 해당 부서 + 모든 하위 부서, false: 해당 부서만)", example = "false")
            @RequestParam(required = false) Boolean includeSubDepartments,
            @Parameter(description = "부서 유형 (DEVELOPMENT: 개발, ENGINEER: 엔지니어)", example = "ENGINEER")
            @RequestParam(required = false) String departmentType,
            @Parameter(description = "계정명/이메일 검색 키워드", example = "홍길동")
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

    @Operation(
            summary = "계정 일괄 부서 이동 (ADMIN 전용)",
            description = """
                    여러 계정의 부서를 일괄로 변경합니다.

                    **targetDepartmentId 동작:**
                    - `null`: 부서 배치 해제 (미배치 상태)
                    - `값`: 해당 부서로 일괄 이동
                    """,
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "이동 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = BatchTransferDepartmentApiResponse.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "계정 또는 부서를 찾을 수 없음"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "권한 없음 (ADMIN만 가능)"
                    )
            }
    )
    ResponseEntity<ApiResponse<AccountDto.BatchTransferDepartmentResponse>> batchTransferDepartment(
            @RequestBody AccountDto.BatchTransferDepartmentRequest request
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

    /**
     * Swagger 스키마용 wrapper 클래스 - 일괄 부서 이동 결과
     */
    @Schema(description = "일괄 부서 이동 API 응답")
    class BatchTransferDepartmentApiResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "일괄 부서 이동 결과")
        public AccountDto.BatchTransferDepartmentResponse data;
    }
}
