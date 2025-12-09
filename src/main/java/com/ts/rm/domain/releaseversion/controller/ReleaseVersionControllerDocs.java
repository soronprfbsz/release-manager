package com.ts.rm.domain.releaseversion.controller;

import com.ts.rm.domain.releaseversion.dto.ReleaseVersionDto;
import com.ts.rm.global.response.ApiResponse;
import com.ts.rm.global.response.SwaggerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * ReleaseVersionController Swagger 문서화 인터페이스
 */
@Tag(name = "릴리즈 버전", description = "릴리즈 버전 관리 API")
@SwaggerResponse
public interface ReleaseVersionControllerDocs {

    @Operation(
            summary = "표준 릴리즈 버전 생성",
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
                    + "- Authorization 헤더에 JWT 토큰 필수 (Bearer {token})",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateVersionApiResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<ReleaseVersionDto.CreateVersionResponse>> createStandardVersion(
            @Parameter(description = "버전 정보 (version, comment)", required = true)
            @Valid @ModelAttribute ReleaseVersionDto.CreateStandardVersionRequest request,

            @Parameter(description = "패치 파일 ZIP", required = true)
            @RequestPart("patchFiles") MultipartFile patchFiles,

            @Parameter(description = "JWT 토큰 (Bearer {token})", required = true)
            @RequestHeader("Authorization") String authorization
    );

    @Operation(
            summary = "릴리즈 버전 조회 (ID)",
            description = "ID로 릴리즈 버전 정보를 조회합니다",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DetailApiResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<ReleaseVersionDto.DetailResponse>> getVersionById(
            @Parameter(description = "버전 ID", required = true)
            @PathVariable Long id
    );

    @Operation(
            summary = "표준 릴리즈 버전 트리 조회",
            description = "프로젝트별 표준 릴리즈 버전들을 계층 구조로 조회합니다 (프론트엔드 트리 렌더링용)\n\n"
                    + "**응답 구조** (3단계 중첩):\n"
                    + "1. majorMinorGroups: 메이저.마이너 그룹 목록 (예: 1.1.x, 1.2.x)\n"
                    + "2. versions: 각 그룹 내의 버전 목록 (예: 1.1.0, 1.1.1, 1.1.2)\n"
                    + "3. fileCategories: 각 버전의 파일 카테고리 목록 (예: DATABASE, WEB, ENGINE)",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TreeApiResponse.class),
                            examples = @ExampleObject(
                                    name = "표준 릴리즈 버전 트리 조회 성공 예시",
                                    value = """
                                            {
                                              "status": "success",
                                              "data": {
                                                "releaseType": "STANDARD",
                                                "customerCode": null,
                                                "majorMinorGroups": [
                                                  {
                                                    "majorMinor": "1.1.x",
                                                    "versions": [
                                                      {
                                                        "versionId": 1,
                                                        "version": "1.1.0",
                                                        "createdAt": "2025-11-20",
                                                        "createdBy": "jhlee@tscientific",
                                                        "comment": "Initial release",
                                                        "fileCategories": ["DATABASE", "WEB", "ENGINE"]
                                                      },
                                                      {
                                                        "versionId": 2,
                                                        "version": "1.1.1",
                                                        "createdAt": "2025-11-25",
                                                        "createdBy": "jhlee@tscientific",
                                                        "comment": "Bug fixes",
                                                        "fileCategories": ["DATABASE", "WEB"]
                                                      }
                                                    ]
                                                  },
                                                  {
                                                    "majorMinor": "1.2.x",
                                                    "versions": [
                                                      {
                                                        "versionId": 3,
                                                        "version": "1.2.0",
                                                        "createdAt": "2025-12-01",
                                                        "createdBy": "jhlee@tscientific",
                                                        "comment": "New features",
                                                        "fileCategories": ["DATABASE", "WEB", "ENGINE"]
                                                      }
                                                    ]
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            )
    )
    ResponseEntity<ApiResponse<ReleaseVersionDto.TreeResponse>> getStandardReleaseTree(
            @Parameter(description = "프로젝트 ID", required = true, example = "infraeye2")
            @PathVariable String projectId
    );

    @Operation(
            summary = "커스텀 릴리즈 버전 트리 조회",
            description = "프로젝트별 특정 고객사의 커스텀 릴리즈 버전들을 계층 구조로 조회합니다 (프론트엔드 트리 렌더링용)\n\n"
                    + "**응답 구조** (3단계 중첩):\n"
                    + "1. majorMinorGroups: 메이저.마이너 그룹 목록 (예: 1.1.x, 1.2.x)\n"
                    + "2. versions: 각 그룹 내의 버전 목록 (예: 1.1.0, 1.1.1, 1.1.2)\n"
                    + "3. fileCategories: 각 버전의 파일 카테고리 목록 (예: DATABASE, WEB, ENGINE)",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TreeApiResponse.class),
                            examples = @ExampleObject(
                                    name = "커스텀 릴리즈 버전 트리 조회 성공 예시",
                                    value = """
                                            {
                                              "status": "success",
                                              "data": {
                                                "releaseType": "CUSTOM",
                                                "customerCode": "company_a",
                                                "majorMinorGroups": [
                                                  {
                                                    "majorMinor": "1.1.x",
                                                    "versions": [
                                                      {
                                                        "versionId": 101,
                                                        "version": "1.1.0",
                                                        "createdAt": "2025-11-22",
                                                        "createdBy": "jhlee@tscientific",
                                                        "comment": "Company A custom release",
                                                        "fileCategories": ["DATABASE", "WEB"]
                                                      }
                                                    ]
                                                  },
                                                  {
                                                    "majorMinor": "1.2.x",
                                                    "versions": [
                                                      {
                                                        "versionId": 102,
                                                        "version": "1.2.0",
                                                        "createdAt": "2025-12-03",
                                                        "createdBy": "jhlee@tscientific",
                                                        "comment": "Custom features for Company A",
                                                        "fileCategories": ["DATABASE", "WEB", "ENGINE"]
                                                      }
                                                    ]
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            )
    )
    ResponseEntity<ApiResponse<ReleaseVersionDto.TreeResponse>> getCustomReleaseTree(
            @Parameter(description = "프로젝트 ID", required = true, example = "infraeye2")
            @PathVariable String projectId,

            @Parameter(description = "고객사 코드", required = true, example = "company_a")
            @PathVariable("customer-code") String customerCode
    );

    @Operation(
            summary = "릴리즈 버전 삭제",
            description = "릴리즈 버전을 완전히 삭제합니다.\n\n"
                    + "**삭제되는 항목**:\n"
                    + "- 데이터베이스: release_version, release_file, release_version_hierarchy\n"
                    + "- 파일 시스템: versions/{type}/{majorMinor}/{version}/ 디렉토리\n"
                    + "- release_metadata.json: 해당 버전 정보",
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
    ResponseEntity<ApiResponse<Void>> deleteVersion(
            @Parameter(description = "버전 ID", required = true)
            @PathVariable Long id
    );

    @Operation(
            summary = "릴리즈 버전 파일 트리 조회",
            description = "릴리즈 버전의 파일 구조를 트리 형태로 조회합니다.\n\n"
                    + "**응답 구조**:\n"
                    + "- 디렉토리와 파일을 계층 구조로 반환\n"
                    + "- 각 파일 노드에는 releaseFileId 포함 (다운로드 시 사용)\n"
                    + "- relativePath를 기반으로 트리 구조 생성",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FileTreeApiResponse.class),
                            examples = @ExampleObject(
                                    name = "파일 트리 조회 성공 예시",
                                    value = """
                                            {
                                              "status": "success",
                                              "data": {
                                                "versionId": 1,
                                                "version": "1.1.0",
                                                "root": {
                                                  "name": "1.1.0",
                                                  "type": "directory",
                                                  "path": "",
                                                  "releaseFileId": null,
                                                  "size": null,
                                                  "children": [
                                                    {
                                                      "name": "database",
                                                      "type": "directory",
                                                      "path": "database",
                                                      "releaseFileId": null,
                                                      "size": null,
                                                      "children": [
                                                        {
                                                          "name": "mariadb",
                                                          "type": "directory",
                                                          "path": "database/mariadb",
                                                          "releaseFileId": null,
                                                          "size": null,
                                                          "children": [
                                                            {
                                                              "name": "1.patch_ddl.sql",
                                                              "type": "file",
                                                              "path": "database/mariadb/1.patch_ddl.sql",
                                                              "releaseFileId": 101,
                                                              "size": 5678,
                                                              "children": null
                                                            },
                                                            {
                                                              "name": "2.patch_dml.sql",
                                                              "type": "file",
                                                              "path": "database/mariadb/2.patch_dml.sql",
                                                              "releaseFileId": 102,
                                                              "size": 3456,
                                                              "children": null
                                                            }
                                                          ]
                                                        }
                                                      ]
                                                    },
                                                    {
                                                      "name": "web",
                                                      "type": "directory",
                                                      "path": "web",
                                                      "releaseFileId": null,
                                                      "size": null,
                                                      "children": [
                                                        {
                                                          "name": "build",
                                                          "type": "directory",
                                                          "path": "web/build",
                                                          "releaseFileId": null,
                                                          "size": null,
                                                          "children": [
                                                            {
                                                              "name": "frontend.war",
                                                              "type": "file",
                                                              "path": "web/build/frontend.war",
                                                              "releaseFileId": 201,
                                                              "size": 25678912,
                                                              "children": null
                                                            }
                                                          ]
                                                        }
                                                      ]
                                                    }
                                                  ]
                                                }
                                              }
                                            }
                                            """
                            )
                    )
            )
    )
    ResponseEntity<ApiResponse<ReleaseVersionDto.FileTreeResponse>> getVersionFileTree(
            @Parameter(description = "버전 ID", required = true)
            @PathVariable Long id
    );

    /**
     * Swagger 스키마용 wrapper 클래스 - 버전 생성 응답
     */
    @Schema(description = "버전 생성 API 응답")
    class CreateVersionApiResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "생성된 버전 정보")
        public ReleaseVersionDto.CreateVersionResponse data;
    }

    /**
     * Swagger 스키마용 wrapper 클래스 - 버전 상세 응답
     */
    @Schema(description = "버전 상세 API 응답")
    class DetailApiResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "버전 상세 정보")
        public ReleaseVersionDto.DetailResponse data;
    }

    /**
     * Swagger 스키마용 wrapper 클래스 - 트리 응답
     */
    @Schema(description = "버전 트리 API 응답")
    class TreeApiResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "버전 트리")
        public ReleaseVersionDto.TreeResponse data;
    }

    /**
     * Swagger 스키마용 wrapper 클래스 - 파일 트리 응답
     */
    @Schema(description = "파일 트리 API 응답")
    class FileTreeApiResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "파일 트리")
        public ReleaseVersionDto.FileTreeResponse data;
    }
}
