package com.ts.rm.domain.releaseversion.controller;

import com.ts.rm.domain.releaseversion.dto.ReleaseVersionDto;
import com.ts.rm.domain.releaseversion.service.ReleaseVersionService;
import com.ts.rm.domain.releaseversion.service.ReleaseVersionTreeService;
import com.ts.rm.domain.releaseversion.service.ReleaseVersionUploadService;
import com.ts.rm.global.response.ApiResponse;
import com.ts.rm.global.security.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * ReleaseVersion Controller
 *
 * <p>릴리즈 버전 관리 REST API
 */
@Slf4j
@Tag(name = "릴리즈 버전", description = "릴리즈 버전 관리 API")
@RestController
@RequestMapping("/api/releases")
@RequiredArgsConstructor
public class ReleaseVersionController {

    private final ReleaseVersionService releaseVersionService;
    private final ReleaseVersionUploadService uploadService;
    private final ReleaseVersionTreeService treeService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 표준 릴리즈 버전 생성 (ZIP 파일 업로드)
     *
     * @param request       버전 생성 요청 (version, comment)
     * @param patchFiles    패치 파일 ZIP
     * @param authorization JWT 토큰 (Bearer {token})
     * @return 생성된 버전 정보
     */
    @Operation(summary = "표준 릴리즈 버전 생성",
            description = "ZIP 파일로 표준 릴리즈 버전을 생성합니다.\n\n"
                    + "**ZIP 파일 구조 규칙**:\n"
                    + "```\n"
                    + "patch_1.1.3.zip\n"
                    + "├── database/              ← 데이터베이스 관련 파일\n"
                    + "│   ├── mariadb/\n"
                    + "│   │   ├── 1.patch_ddl.sql\n"
                    + "│   │   └── 2.patch_dml.sql\n"
                    + "│   └── cratedb/\n"
                    + "│       └── 1.patch_crate.sql\n"
                    + "├── web/                   ← 웹 애플리케이션 빌드 산출물\n"
                    + "│   └── build/\n"
                    + "│       ├── frontend.war\n"
                    + "│       └── admin.war\n"
                    + "├── engine/                ← 엔진 빌드 산출물\n"
                    + "│   ├── build/\n"
                    + "│   │   └── engine.jar\n"
                    + "│   └── scripts/\n"
                    + "│       └── startup.sh\n"
                    + "└── install/               ← 설치본 파일 (패치 생성 시 제외됨)\n"
                    + "    ├── installer.exe\n"
                    + "    └── setup_guide.md\n"
                    + "```\n\n"
                    + "**카테고리 폴더**:\n"
                    + "- `database/` - 데이터베이스 SQL 스크립트\n"
                    + "- `web/` - 웹 애플리케이션 빌드 산출물 (WAR, JAR)\n"
                    + "- `engine/` - 엔진 빌드 산출물 및 실행 스크립트\n"
                    + "- `install/` - 설치본 파일 (※ 패치 생성 시 제외)\n"
                    + "- 최소 1개 이상의 카테고리 폴더 필수\n\n"
                    + "**제약사항**:\n"
                    + "- ZIP 파일 크기: application.yml의 max-file-size 설정값 (기본 1GB)\n"
                    + "- 압축 해제 후 크기: application.yml의 max-file-size 설정값 (기본 1GB)\n"
                    + "- 허용 확장자: 모든 확장자 허용 (제한 없음)\n"
                    + "- Authorization 헤더에 JWT 토큰 필수 (Bearer {token})")
    @PostMapping(value = "/standard/versions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ReleaseVersionDto.CreateVersionResponse>> createStandardVersion(
            @Parameter(description = "버전 정보 (version, comment)", required = true)
            @Valid @ModelAttribute ReleaseVersionDto.CreateStandardVersionRequest request,

            @Parameter(description = "패치 파일 ZIP", required = true)
            @RequestPart("patchFiles") MultipartFile patchFiles,

            @Parameter(description = "JWT 토큰 (Bearer {token})", required = true)
            @RequestHeader("Authorization") String authorization) {

        log.info("표준 릴리즈 버전 생성 요청 - version: {}, releaseCategory: {}, comment: {}, fileSize: {}",
                request.version(), request.releaseCategory(), request.comment(), patchFiles.getSize());

        // JWT 토큰에서 이메일 추출
        String token = extractToken(authorization);
        String createdBy = jwtTokenProvider.getEmail(token);

        log.info("버전 생성자: {}", createdBy);

        // 버전 생성
        ReleaseVersionDto.CreateVersionResponse response = uploadService.createStandardVersionWithZip(
                request.version(),
                request.releaseCategory(),
                request.comment(),
                patchFiles,
                createdBy
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Authorization 헤더에서 JWT 토큰 추출
     *
     * @param authorization "Bearer {token}" 형식
     * @return JWT 토큰
     */
    private String extractToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new com.ts.rm.global.exception.BusinessException(
                    com.ts.rm.global.exception.ErrorCode.INVALID_CREDENTIALS,
                    "유효하지 않은 Authorization 헤더입니다");
        }
        return authorization.substring(7);
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
     * 표준 릴리즈 버전 트리 조회
     *
     * @return 릴리즈 버전 트리 (계층 구조)
     */
    @Operation(summary = "표준 릴리즈 버전 트리 조회",
               description = "표준 릴리즈 버전들을 계층 구조로 조회합니다 (프론트엔드 트리 렌더링용)")
    @GetMapping("/standard/tree")
    public ResponseEntity<ApiResponse<ReleaseVersionDto.TreeResponse>> getStandardReleaseTree() {
        ReleaseVersionDto.TreeResponse response = treeService.getStandardReleaseTree();
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
    @GetMapping("/custom/{customer-code}/tree")
    public ResponseEntity<ApiResponse<ReleaseVersionDto.TreeResponse>> getCustomReleaseTree(
            @Parameter(description = "고객사 코드", required = true, example = "company_a")
            @PathVariable("customer-code") String customerCode) {
        ReleaseVersionDto.TreeResponse response = treeService.getCustomReleaseTree(customerCode);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 릴리즈 버전 삭제
     *
     * @param id 버전 ID
     * @return 성공 응답
     */
    @Operation(summary = "릴리즈 버전 삭제",
               description = "릴리즈 버전을 완전히 삭제합니다.\\n\\n" +
                             "**삭제되는 항목**:\\n" +
                             "- 데이터베이스: release_version, release_file, release_version_hierarchy\\n" +
                             "- 파일 시스템: versions/{type}/{majorMinor}/{version}/ 디렉토리\\n" +
                             "- release_metadata.json: 해당 버전 정보")
    @DeleteMapping("/versions/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVersion(
            @Parameter(description = "버전 ID", required = true) @PathVariable Long id) {

        log.info("릴리즈 버전 삭제 요청 - ID: {}", id);

        releaseVersionService.deleteVersion(id);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 릴리즈 버전 파일 트리 조회
     *
     * @param id 버전 ID
     * @return 파일 트리 응답
     */
    @Operation(summary = "릴리즈 버전 파일 트리 조회",
               description = "릴리즈 버전의 파일 구조를 트리 형태로 조회합니다.\\n\\n" +
                             "**응답 구조**:\\n" +
                             "- 디렉토리와 파일을 계층 구조로 반환\\n" +
                             "- 각 파일 노드에는 releaseFileId 포함 (다운로드 시 사용)\\n" +
                             "- relativePath를 기반으로 트리 구조 생성")
    @GetMapping("/versions/{id}/files")
    public ResponseEntity<ApiResponse<ReleaseVersionDto.FileTreeResponse>> getVersionFileTree(
            @Parameter(description = "버전 ID", required = true) @PathVariable Long id) {

        log.info("릴리즈 버전 파일 트리 조회 요청 - ID: {}", id);

        // 버전 조회
        com.ts.rm.domain.releaseversion.entity.ReleaseVersion version = releaseVersionService.findVersionById(id);

        ReleaseVersionDto.FileTreeResponse response = treeService.getVersionFileTree(id, version);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
