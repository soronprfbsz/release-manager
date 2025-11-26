package com.ts.rm.domain.releaseversion.controller;

import com.ts.rm.domain.releaseversion.dto.ReleaseVersionDto;
import com.ts.rm.domain.releaseversion.service.ReleaseVersionService;
import com.ts.rm.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * ReleaseVersion Controller
 *
 * <p>릴리즈 버전 관리 REST API
 */
@Tag(name = "릴리즈 버전", description = "릴리즈 버전 관리 API")
@RestController
@RequestMapping("/api/releases")
@RequiredArgsConstructor
public class ReleaseVersionController {

    private final ReleaseVersionService releaseVersionService;

    /**
     * 표준 릴리즈 버전 생성
     *
     * @param request 버전 생성 요청
     * @return 생성된 버전 정보
     */
    @Operation(summary = "표준 릴리즈 버전 생성", description = "새로운 표준 릴리즈 버전을 생성합니다")
    @PostMapping("/standard/versions")
    public ResponseEntity<ApiResponse<ReleaseVersionDto.DetailResponse>> createStandardVersion(
            @Valid @RequestBody ReleaseVersionDto.CreateRequest request) {
        ReleaseVersionDto.DetailResponse response = releaseVersionService.createStandardVersion(
                request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 표준 릴리즈 버전 및 파일 일괄 생성
     *
     * @param request      버전 생성 요청 (JSON)
     * @param mariadbFiles MariaDB SQL 파일들 (선택)
     * @param cratedbFiles CrateDB SQL 파일들 (선택)
     * @return 생성된 버전 정보
     */
    @Operation(summary = "표준 릴리즈 버전 및 파일 일괄 생성",
               description = "새로운 표준 릴리즈 버전 생성과 SQL 파일 업로드를 한 번에 처리합니다")
    @PostMapping(value = "/standard/versions/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ReleaseVersionDto.DetailResponse>> createStandardVersionWithFiles(
            @Parameter(description = "버전 생성 요청 (JSON)", required = true,
                       content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart(value = "request") @Valid ReleaseVersionDto.CreateRequest request,
            @Parameter(description = "MariaDB SQL 파일들")
            @RequestPart(value = "mariadbFiles", required = false) List<MultipartFile> mariadbFiles,
            @Parameter(description = "CrateDB SQL 파일들")
            @RequestPart(value = "cratedbFiles", required = false) List<MultipartFile> cratedbFiles) {

        ReleaseVersionDto.DetailResponse response = releaseVersionService.createStandardVersionWithFiles(
                request, mariadbFiles, cratedbFiles);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 커스텀 릴리즈 버전 생성
     *
     * @param request 버전 생성 요청 (customerId 필수)
     * @return 생성된 버전 정보
     */
    @Operation(summary = "커스텀 릴리즈 버전 생성", description = "새로운 커스텀 릴리즈 버전을 생성합니다 (고객사 ID 필수)")
    @PostMapping("/custom/versions")
    public ResponseEntity<ApiResponse<ReleaseVersionDto.DetailResponse>> createCustomVersion(
            @Valid @RequestBody ReleaseVersionDto.CreateRequest request) {
        ReleaseVersionDto.DetailResponse response = releaseVersionService.createCustomVersion(
                request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 릴리즈 버전 조회 (ID)
     *
     * @param id 버전 ID
     * @return 버전 상세 정보
     */
    @Operation(summary = "릴리즈 버전 조회 (ID)", description = "ID로 릴리즈 버전 정보를 조회합니다")
    @GetMapping("/versions/{id}")
    public ResponseEntity<ApiResponse<ReleaseVersionDto.DetailResponse>> getVersionById(
            @Parameter(description = "버전 ID", required = true) @PathVariable Long id) {
        ReleaseVersionDto.DetailResponse response = releaseVersionService.getVersionById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 타입별 버전 목록 조회
     *
     * @param type       릴리즈 타입 (standard/custom)
     * @param customerId 고객사 ID (custom 타입인 경우)
     * @return 버전 목록
     */
    @Operation(summary = "타입별 버전 목록 조회", description = "릴리즈 타입별로 버전 목록을 조회합니다")
    @GetMapping("/{type}/versions")
    public ResponseEntity<ApiResponse<List<ReleaseVersionDto.SimpleResponse>>> getVersionsByType(
            @Parameter(description = "릴리즈 타입 (standard/custom)", required = true) @PathVariable String type,
            @Parameter(description = "고객사 ID (custom 타입인 경우)") @RequestParam(required = false) Long customerId) {

        List<ReleaseVersionDto.SimpleResponse> response;
        if ("custom".equals(type) && customerId != null) {
            response = releaseVersionService.getVersionsByCustomer(customerId);
        } else {
            response = releaseVersionService.getVersionsByType(type);
        }

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Major.Minor 버전 목록 조회
     *
     * @param type       릴리즈 타입 (standard/custom)
     * @param majorMinor 메이저.마이너 버전 (예: 1.1.x)
     * @return 버전 목록
     */
    @Operation(summary = "Major.Minor 버전 목록 조회", description = "특정 Major.Minor 버전의 패치 버전 목록을 조회합니다")
    @GetMapping("/{type}/versions/{majorMinor}")
    public ResponseEntity<ApiResponse<List<ReleaseVersionDto.SimpleResponse>>> getVersionsByMajorMinor(
            @Parameter(description = "릴리즈 타입 (standard/custom)", required = true) @PathVariable String type,
            @Parameter(description = "메이저.마이너 버전 (예: 1.1.x)", required = true) @PathVariable String majorMinor) {
        List<ReleaseVersionDto.SimpleResponse> response = releaseVersionService.getVersionsByMajorMinor(
                type, majorMinor);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 릴리즈 버전 정보 수정
     *
     * @param id      버전 ID
     * @param request 수정 요청
     * @return 수정된 버전 정보
     */
    @Operation(summary = "릴리즈 버전 정보 수정", description = "릴리즈 버전의 코멘트 및 설치본 여부를 수정합니다")
    @PutMapping("/versions/{id}")
    public ResponseEntity<ApiResponse<ReleaseVersionDto.DetailResponse>> updateVersion(
            @Parameter(description = "버전 ID", required = true) @PathVariable Long id,
            @Valid @RequestBody ReleaseVersionDto.UpdateRequest request) {
        ReleaseVersionDto.DetailResponse response = releaseVersionService.updateVersion(id,
                request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 릴리즈 버전 삭제
     *
     * @param id 버전 ID
     * @return 성공 응답
     */
    @Operation(summary = "릴리즈 버전 삭제", description = "릴리즈 버전을 삭제합니다 (연관된 패치 파일도 함께 삭제)")
    @DeleteMapping("/versions/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVersion(
            @Parameter(description = "버전 ID", required = true) @PathVariable Long id) {
        releaseVersionService.deleteVersion(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 표준 릴리즈 버전 트리 조회
     *
     * @return 릴리즈 버전 트리 (계층 구조)
     */
    @Operation(summary = "표준 릴리즈 버전 트리 조회",
               description = "표준 릴리즈 버전들을 계층 구조로 조회합니다 (프론트엔드 트리 렌더링용)")
    @GetMapping("/standard/tree")
    public ResponseEntity<ApiResponse<ReleaseVersionDto.TreeResponse>> getStandardReleaseTree() {
        ReleaseVersionDto.TreeResponse response = releaseVersionService.getStandardReleaseTree();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 커스텀 릴리즈 버전 트리 조회
     *
     * @param customerCode 고객사 코드
     * @return 릴리즈 버전 트리 (계층 구조)
     */
    @Operation(summary = "커스텀 릴리즈 버전 트리 조회",
               description = "특정 고객사의 커스텀 릴리즈 버전들을 계층 구조로 조회합니다 (프론트엔드 트리 렌더링용)")
    @GetMapping("/custom/{customerCode}/tree")
    public ResponseEntity<ApiResponse<ReleaseVersionDto.TreeResponse>> getCustomReleaseTree(
            @Parameter(description = "고객사 코드", required = true, example = "company_a")
            @PathVariable String customerCode) {
        ReleaseVersionDto.TreeResponse response = releaseVersionService.getCustomReleaseTree(customerCode);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
