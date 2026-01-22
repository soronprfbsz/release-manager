package com.ts.rm.domain.resourcefile.controller;

import com.ts.rm.domain.resourcefile.dto.ResourceFileDto;
import com.ts.rm.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/**
 * ResourceFile Controller API 문서 (Swagger)
 */
@Tag(name = "Resource File", description = "카테고리별 리소스 파일 관리 API (파일시스템 기반)")
public interface ResourceFileControllerDocs {

    @Operation(
            summary = "카테고리 목록 조회",
            description = """
                    리소스 파일 카테고리 목록을 조회합니다.

                    **카테고리**: `resources/file/` 하위 폴더가 카테고리로 인식됩니다.

                    **응답 정보**:
                    - `category`: 카테고리명 (폴더명)
                    - `fileCount`: 해당 카테고리 내 파일 수
                    - `totalSize`: 해당 카테고리 내 총 파일 크기 (bytes)
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ResourceFileDto.CategoriesResponse.class))
            )
    })
    ResponseEntity<ApiResponse<ResourceFileDto.CategoriesResponse>> getCategories();

    @Operation(
            summary = "카테고리 생성",
            description = """
                    리소스 파일 카테고리를 생성합니다.

                    **카테고리명 규칙**:
                    - 영문 소문자, 숫자, 하이픈(-), 언더스코어(_)만 사용 가능
                    - 입력값은 자동으로 소문자 변환

                    **예시**: `script`, `docker`, `my-docs`
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = ResourceFileDto.CategoryCreateResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 카테고리명"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 카테고리")
    })
    ResponseEntity<ApiResponse<ResourceFileDto.CategoryCreateResponse>> createCategory(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "카테고리 생성 요청",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ResourceFileDto.CategoryCreateRequest.class))
            ) ResourceFileDto.CategoryCreateRequest request);

    @Operation(
            summary = "카테고리 삭제",
            description = """
                    리소스 파일 카테고리를 삭제합니다.

                    **삭제 조건**:
                    - 카테고리 내에 파일이 없어야 삭제 가능
                    - 빈 하위 디렉토리는 함께 삭제됨

                    **주의사항**: 파일이 존재하면 409 에러 반환
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "삭제 성공",
                    content = @Content(schema = @Schema(implementation = ResourceFileDto.CategoryDeleteResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "카테고리 내 파일 존재")
    })
    ResponseEntity<ApiResponse<ResourceFileDto.CategoryDeleteResponse>> deleteCategory(
            @Parameter(description = "카테고리명", example = "docker") String category);

    @Operation(
            summary = "파일 트리 조회",
            description = """
                    카테고리별 파일 트리를 조회합니다.

                    **응답 구조**:
                    - 트리 구조로 디렉토리와 파일 목록 반환
                    - 디렉토리는 `children`으로 하위 노드 포함
                    - 파일은 `size`로 크기 정보 포함
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ResourceFileDto.FilesResponse.class))
            )
    })
    ResponseEntity<ApiResponse<ResourceFileDto.FilesResponse>> getFiles(
            @Parameter(description = "카테고리명", example = "script") String category);

    @Operation(
            summary = "디렉토리 생성",
            description = """
                    카테고리 내에 디렉토리를 생성합니다.

                    **경로 형식**: `/하위폴더/하위폴더` (슬래시로 시작)

                    **예시**:
                    - `/mariadb` → `resources/file/script/mariadb` 생성
                    - `/mariadb/backup` → `resources/file/script/mariadb/backup` 생성
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = ResourceFileDto.DirectoryResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 경로")
    })
    ResponseEntity<ApiResponse<ResourceFileDto.DirectoryResponse>> createDirectory(
            @Parameter(description = "카테고리명", example = "script") String category,
            @Parameter(description = "생성할 디렉토리 경로", example = "/mariadb/backup") String path);

    @Operation(
            summary = "파일 업로드",
            description = """
                    카테고리에 파일을 업로드합니다.

                    **ZIP 파일 처리**:
                    - `extractZip=true` (기본값): ZIP 파일 압축 해제
                    - `extractZip=false`: ZIP 파일 원본 유지

                    **대상 경로 (targetPath)**:
                    - 생략 시: 카테고리 루트에 저장
                    - 지정 시: 해당 경로에 저장 (경로 자동 생성)
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "업로드 성공",
                    content = @Content(schema = @Schema(implementation = ResourceFileDto.UploadResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    ResponseEntity<ApiResponse<ResourceFileDto.UploadResponse>> uploadFile(
            @Parameter(description = "카테고리명", example = "script") String category,
            @Parameter(description = "업로드할 파일") MultipartFile file,
            @Parameter(description = "대상 경로 (선택)", example = "/mariadb") String targetPath,
            @Parameter(description = "ZIP 파일 압축 해제 여부 (기본값: true)") Boolean extractZip);

    @Operation(
            summary = "파일/디렉토리 삭제",
            description = """
                    파일 또는 디렉토리를 삭제합니다.

                    **동작 방식**:
                    - 파일 삭제: 해당 파일만 삭제
                    - 디렉토리 삭제: 내부 파일/폴더 모두 삭제

                    **주의사항**: 삭제된 데이터는 복구할 수 없습니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "삭제 성공",
                    content = @Content(schema = @Schema(implementation = ResourceFileDto.DeleteResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음")
    })
    ResponseEntity<ApiResponse<ResourceFileDto.DeleteResponse>> deleteFile(
            @Parameter(description = "카테고리명", example = "script") String category,
            @Parameter(description = "삭제할 파일/디렉토리 경로", example = "/mariadb/backup.sh") String filePath);

    @Operation(
            summary = "전체 파일 ZIP 다운로드",
            description = """
                    카테고리 내 전체 파일을 ZIP으로 다운로드합니다.

                    **응답 헤더**:
                    - `Content-Disposition`: 파일명 포함
                    - `X-Uncompressed-Size`: 압축 전 총 크기 (bytes)
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "다운로드 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음")
    })
    void downloadAllFiles(
            @Parameter(description = "카테고리명", example = "script") String category,
            HttpServletResponse response) throws IOException;
}
