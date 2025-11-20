package com.ts.rm.domain.patch.controller;

import com.ts.rm.domain.patch.dto.CumulativePatchDto;
import com.ts.rm.domain.patch.entity.CumulativePatch;
import com.ts.rm.domain.patch.mapper.CumulativePatchDtoMapper;
import com.ts.rm.domain.patch.service.CumulativePatchService;
import com.ts.rm.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 누적 패치 생성 API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/cumulative-patches")
@RequiredArgsConstructor
@Tag(name = "누적 패치 관리", description = "누적 패치 생성 및 조회 API")
public class CumulativePatchController {

    private final CumulativePatchService cumulativePatchService;
    private final CumulativePatchDtoMapper cumulativePatchDtoMapper;

    /**
     * 누적 패치 생성
     */
    @PostMapping("/generate")
    @Operation(summary = "누적 패치 생성", description = "From 버전부터 To 버전까지의 누적 패치를 생성합니다.")
    public ApiResponse<CumulativePatchDto.DetailResponse> generateCumulativePatch(
            @Valid @RequestBody CumulativePatchDto.GenerateRequest request) {

        log.info("누적 패치 생성 요청 - From: {}, To: {}, Type: {}",
                request.fromVersion(), request.toVersion(), request.type());

        CumulativePatch patch = cumulativePatchService.generateCumulativePatchByVersion(
                request.type(),
                request.customerId(),
                request.fromVersion(),
                request.toVersion(),
                request.generatedBy()
        );

        CumulativePatchDto.DetailResponse response = cumulativePatchDtoMapper.toDetailResponse(
                patch);

        return ApiResponse.success(response);
    }

    /**
     * 누적 패치 상세 조회
     */
    @GetMapping("/{id}")
    @Operation(summary = "누적 패치 상세 조회", description = "누적 패치 ID로 상세 정보를 조회합니다.")
    public ApiResponse<CumulativePatchDto.DetailResponse> getCumulativePatch(
            @PathVariable Long id) {

        log.info("누적 패치 조회 요청 - ID: {}", id);

        CumulativePatch patch = cumulativePatchService.getCumulativePatch(id);
        CumulativePatchDto.DetailResponse response = cumulativePatchDtoMapper.toDetailResponse(
                patch);

        return ApiResponse.success(response);
    }

    /**
     * 누적 패치 목록 조회
     */
    @GetMapping
    @Operation(summary = "누적 패치 목록 조회", description = "누적 패치 이력 목록을 조회합니다.")
    public ApiResponse<List<CumulativePatchDto.SimpleResponse>> listCumulativePatches(
            @RequestParam(required = false) String releaseType) {

        log.info("누적 패치 목록 조회 요청 - releaseType: {}", releaseType);

        List<CumulativePatch> patches = cumulativePatchService.listCumulativePatches(releaseType);
        List<CumulativePatchDto.SimpleResponse> response = cumulativePatchDtoMapper.toSimpleResponseList(
                patches);

        return ApiResponse.success(response);
    }
}
