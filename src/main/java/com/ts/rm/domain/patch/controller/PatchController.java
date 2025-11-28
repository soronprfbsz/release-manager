package com.ts.rm.domain.patch.controller;

import com.ts.rm.domain.patch.dto.PatchDto;
import com.ts.rm.domain.patch.entity.Patch;
import com.ts.rm.domain.patch.mapper.PatchDtoMapper;
import com.ts.rm.domain.patch.service.PatchService;
import com.ts.rm.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 패치 관리 API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/patches")
@RequiredArgsConstructor
@Tag(name = "패치", description = "패치 생성 및 조회 API")
public class PatchController {

    private final PatchService patchService;
    private final PatchDtoMapper patchDtoMapper;

    /**
     * 패치 생성 (누적 패치 생성)
     */
    @PostMapping("/generate")
    @Operation(summary = "패치 생성", description = "From 버전부터 To 버전까지의 누적 패치를 생성합니다.")
    public ApiResponse<PatchDto.DetailResponse> generatePatch(
            @Valid @RequestBody PatchDto.GenerateRequest request) {

        log.info("패치 생성 요청 - From: {}, To: {}, Type: {}, PatchName: {}",
                request.fromVersion(), request.toVersion(), request.type(), request.patchName());

        Patch patch = patchService.generatePatchByVersion(
                request.type(),
                request.customerId(),
                request.fromVersion(),
                request.toVersion(),
                request.generatedBy(),
                request.description(),
                request.patchedBy(),
                request.patchName()
        );

        PatchDto.DetailResponse response = patchDtoMapper.toDetailResponse(patch);

        return ApiResponse.success(response);
    }

    /**
     * 패치 상세 조회
     */
    @GetMapping("/{id}")
    @Operation(summary = "패치 상세 조회", description = "패치 ID로 상세 정보를 조회합니다.")
    public ApiResponse<PatchDto.DetailResponse> getPatch(
            @PathVariable Long id) {

        log.info("패치 조회 요청 - ID: {}", id);

        Patch patch = patchService.getPatch(id);
        PatchDto.DetailResponse response = patchDtoMapper.toDetailResponse(patch);

        return ApiResponse.success(response);
    }

    /**
     * 패치 목록 조회 (페이징)
     */
    @GetMapping
    @Operation(summary = "패치 목록 조회", description = "패치 목록을 페이징하여 조회합니다. page, size, sort 파라미터 사용 가능")
    public ApiResponse<Page<PatchDto.SimpleResponse>> listPatches(
            @Parameter(description = "릴리즈 타입 (STANDARD/CUSTOM)")
            @RequestParam(required = false) String releaseType,
            @ParameterObject @PageableDefault(size = 10, sort = "generatedAt") Pageable pageable) {

        log.info("패치 목록 조회 요청 - releaseType: {}, page: {}, size: {}",
                releaseType, pageable.getPageNumber(), pageable.getPageSize());

        Page<Patch> patches = patchService.listPatchesWithPaging(releaseType, pageable);
        Page<PatchDto.SimpleResponse> response = patches.map(patchDtoMapper::toSimpleResponse);

        return ApiResponse.success(response);
    }

    /**
     * 패치 파일 구조 조회
     */
    @GetMapping("/{id}/files")
    @Operation(summary = "패치 파일 구조 조회",
            description = "패치 ZIP 파일 내부 구조를 재귀적으로 조회합니다.\n\n"
                    + "응답에는 다음이 포함됩니다:\n"
                    + "- root: 루트 디렉토리 (재귀 구조)\n"
                    + "  - mariadb: MariaDB 패치 파일 및 디렉토리\n"
                    + "  - cratedb: CrateDB 패치 파일 및 디렉토리\n"
                    + "  - README.md: 패치 설명 파일\n"
                    + "  - 실행 스크립트 파일들\n\n"
                    + "각 노드는 FileNode 인터페이스를 구현하며 FileInfo(파일) 또는 DirectoryNode(디렉토리)입니다.\n"
                    + "DirectoryNode는 children을 통해 재귀적으로 하위 파일/디렉토리를 포함합니다.")
    public ApiResponse<PatchDto.FileStructureResponse> getPatchFileStructure(@PathVariable Long id) {

        log.info("패치 파일 구조 조회 요청 - ID: {}", id);

        Patch patch = patchService.getPatch(id);
        PatchDto.DirectoryNode root = patchService.getZipFileStructure(id);

        PatchDto.FileStructureResponse response = new PatchDto.FileStructureResponse(
                patch.getPatchId(),
                patch.getPatchName(),
                root
        );

        return ApiResponse.success(response);
    }

    /**
     * 패치 다운로드 (ZIP)
     */
    @GetMapping("/{id}/download")
    @Operation(summary = "패치 다운로드",
            description = "패치 파일을 ZIP 형식으로 다운로드합니다.\n\n"
                    + "ZIP 파일에는 다음이 포함됩니다:\n"
                    + "- mariadb/ : MariaDB 패치 스크립트 및 SQL 파일\n"
                    + "- cratedb/ : CrateDB 패치 스크립트 및 SQL 파일\n"
                    + "- README.md : 패치 설명 파일")
    public ResponseEntity<byte[]> downloadPatch(@PathVariable Long id) {

        log.info("패치 다운로드 요청 - ID: {}", id);

        byte[] zipBytes = patchService.downloadPatchAsZip(id);
        String fileName = patchService.getZipFileName(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(zipBytes.length))
                .body(zipBytes);
    }

    /**
     * 패치 파일 내용 조회
     */
    @GetMapping("/{id}/content")
    @Operation(summary = "패치 파일 내용 조회",
            description = "패치 디렉토리 내의 특정 파일 내용을 조회합니다.\n\n"
                    + "**사용 예시**:\n"
                    + "- `GET /api/patches/1/content?path=mariadb/source_files/1.1.1/1.patch_mariadb_ddl.sql`\n"
                    + "- `GET /api/patches/1/content?path=README.md`\n\n"
                    + "**제약사항**:\n"
                    + "- 파일 크기: 최대 10MB\n"
                    + "- 파일 형식: 텍스트 파일만 지원 (SQL, MD, SH 등)\n"
                    + "- 보안: 경로 탐색 공격 방지 (../ 등 차단)")
    public ApiResponse<PatchDto.FileContentResponse> getFileContent(
            @PathVariable Long id,
            @Parameter(description = "파일 상대 경로", example = "mariadb/source_files/1.1.1/1.patch_mariadb_ddl.sql", required = true)
            @RequestParam String path) {

        log.info("패치 파일 내용 조회 요청 - ID: {}, Path: {}", id, path);

        PatchDto.FileContentResponse response = patchService.getFileContent(id, path);

        return ApiResponse.success(response);
    }

    /**
     * 패치 삭제
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "패치 삭제",
            description = "패치를 삭제합니다.\n\n"
                    + "**삭제 범위**:\n"
                    + "- DB 레코드 (cumulative_patch 테이블)\n"
                    + "- 실제 파일 디렉토리 (patches/{patchName}/ 전체)\n\n"
                    + "**주의사항**:\n"
                    + "- 삭제된 데이터는 복구할 수 없습니다.\n"
                    + "- 디렉토리 삭제 실패 시에도 DB 레코드는 삭제됩니다.")
    public ApiResponse<Void> deletePatch(@PathVariable Long id) {

        log.info("패치 삭제 요청 - ID: {}", id);

        patchService.deletePatch(id);

        return ApiResponse.success(null);
    }
}
