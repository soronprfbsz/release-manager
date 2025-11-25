package com.ts.rm.domain.patch.controller;

import com.ts.rm.domain.patch.dto.PatchHistoryDto;
import com.ts.rm.domain.patch.entity.PatchHistory;
import com.ts.rm.domain.patch.mapper.PatchHistoryDtoMapper;
import com.ts.rm.domain.patch.service.PatchHistoryService;
import com.ts.rm.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 패치 이력 관리 API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/patch-histories")
@RequiredArgsConstructor
@Tag(name = "패치 이력 관리", description = "패치 이력 생성 및 조회 API")
public class PatchHistoryController {

    private final PatchHistoryService patchHistoryService;
    private final PatchHistoryDtoMapper patchHistoryDtoMapper;

    /**
     * 패치 이력 생성 (누적 패치 생성)
     */
    @PostMapping("/generate")
    @Operation(summary = "패치 이력 생성", description = "From 버전부터 To 버전까지의 누적 패치를 생성합니다.")
    public ApiResponse<PatchHistoryDto.DetailResponse> generatePatchHistory(
            @Valid @RequestBody PatchHistoryDto.GenerateRequest request) {

        log.info("패치 이력 생성 요청 - From: {}, To: {}, Type: {}, PatchName: {}",
                request.fromVersion(), request.toVersion(), request.type(), request.patchName());

        PatchHistory patch = patchHistoryService.generatePatchHistoryByVersion(
                request.type(),
                request.customerId(),
                request.fromVersion(),
                request.toVersion(),
                request.generatedBy(),
                request.description(),
                request.patchedBy(),
                request.patchName()
        );

        PatchHistoryDto.DetailResponse response = patchHistoryDtoMapper.toDetailResponse(patch);

        return ApiResponse.success(response);
    }

    /**
     * 패치 이력 상세 조회
     */
    @GetMapping("/{id}")
    @Operation(summary = "패치 이력 상세 조회", description = "패치 이력 ID로 상세 정보를 조회합니다.")
    public ApiResponse<PatchHistoryDto.DetailResponse> getPatchHistory(
            @PathVariable Long id) {

        log.info("패치 이력 조회 요청 - ID: {}", id);

        PatchHistory patch = patchHistoryService.getPatchHistory(id);
        PatchHistoryDto.DetailResponse response = patchHistoryDtoMapper.toDetailResponse(patch);

        return ApiResponse.success(response);
    }

    /**
     * 패치 이력 목록 조회
     */
    @GetMapping
    @Operation(summary = "패치 이력 목록 조회", description = "패치 이력 목록을 조회합니다.")
    public ApiResponse<List<PatchHistoryDto.SimpleResponse>> listPatchHistories(
            @RequestParam(required = false) String releaseType) {

        log.info("패치 이력 목록 조회 요청 - releaseType: {}", releaseType);

        List<PatchHistory> patches = patchHistoryService.listPatchHistories(releaseType);
        List<PatchHistoryDto.SimpleResponse> response = patchHistoryDtoMapper.toSimpleResponseList(
                patches);

        return ApiResponse.success(response);
    }

    /**
     * 패치 이력 다운로드 (ZIP)
     */
    @GetMapping("/{id}/download")
    @Operation(summary = "패치 이력 다운로드",
            description = "패치 이력 파일을 ZIP 형식으로 다운로드합니다.\n\n"
                    + "ZIP 파일에는 다음이 포함됩니다:\n"
                    + "- mariadb/ : MariaDB 패치 스크립트 및 SQL 파일\n"
                    + "- cratedb/ : CrateDB 패치 스크립트 및 SQL 파일\n"
                    + "- README.md : 패치 설명 파일")
    public ResponseEntity<byte[]> downloadPatchHistory(@PathVariable Long id) {

        log.info("패치 이력 다운로드 요청 - ID: {}", id);

        byte[] zipBytes = patchHistoryService.downloadPatchHistoryAsZip(id);
        String fileName = patchHistoryService.getZipFileName(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(zipBytes.length))
                .body(zipBytes);
    }
}
