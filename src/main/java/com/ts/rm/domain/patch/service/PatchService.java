package com.ts.rm.domain.patch.service;

import com.ts.rm.domain.patch.dto.PatchDto;
import com.ts.rm.domain.patch.entity.Patch;
import com.ts.rm.domain.patch.mapper.PatchDtoMapper;
import com.ts.rm.domain.patch.repository.PatchRepository;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import com.ts.rm.global.pagination.PageRowNumberUtil;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 패치 서비스 (오케스트레이터)
 *
 * <p>패치 CRUD 및 다른 서비스들을 조율하는 역할을 담당합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PatchService {

    private final PatchRepository patchRepository;
    private final PatchDtoMapper patchDtoMapper;
    private final PatchGenerationService patchGenerationService;
    private final PatchDownloadService patchDownloadService;

    @Value("${app.release.base-path:src/main/resources/release}")
    private String releaseBasePath;

    /**
     * 패치 생성 (버전 문자열 기반) - 위임
     */
    @Transactional
    public Patch generatePatchByVersion(String projectId, String releaseType, Long customerId,
            String fromVersion, String toVersion, String createdBy, String description,
            Long engineerId, String patchName) {
        return patchGenerationService.generatePatchByVersion(
                projectId, releaseType, customerId, fromVersion, toVersion,
                createdBy, description, engineerId, patchName);
    }

    /**
     * 패치 생성 (버전 ID 기반) - 위임
     */
    @Transactional
    public Patch generatePatch(String projectId, Long fromVersionId, Long toVersionId, Long customerId,
            String createdBy, String description, Long engineerId, String patchName) {
        return patchGenerationService.generatePatch(
                projectId, fromVersionId, toVersionId, customerId,
                createdBy, description, engineerId, patchName);
    }

    /**
     * 패치 조회
     */
    @Transactional(readOnly = true)
    public Patch getPatch(Long patchId) {
        return patchRepository.findById(patchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND,
                        "패치를 찾을 수 없습니다: " + patchId));
    }

    /**
     * 패치 목록 페이징 조회
     *
     * @param releaseType 릴리즈 타입 (STANDARD/CUSTOM, null이면 전체)
     * @param pageable    페이징 정보
     * @return 패치 목록 페이지 (rowNumber 포함)
     */
    @Transactional(readOnly = true)
    public Page<PatchDto.ListResponse> listPatchesWithPaging(String releaseType, Pageable pageable) {
        Page<Patch> patches;
        if (releaseType != null) {
            patches = patchRepository.findAllByReleaseTypeOrderByCreatedAtDesc(
                    releaseType.toUpperCase(), pageable);
        } else {
            patches = patchRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        // rowNumber 계산 (공통 유틸리티 사용)
        return PageRowNumberUtil.mapWithRowNumber(patches, (patch, rowNumber) -> {
            PatchDto.ListResponse response = patchDtoMapper.toListResponse(patch);
            return new PatchDto.ListResponse(
                    rowNumber,
                    response.patchId(),
                    response.projectId(),
                    response.releaseType(),
                    response.customerCode(),
                    response.customerName(),
                    response.fromVersion(),
                    response.toVersion(),
                    response.patchName(),
                    response.createdBy(),
                    response.description(),
                    response.engineerId(),
                    response.engineerName(),
                    response.createdAt()
            );
        });
    }

    /**
     * 패치를 스트리밍 방식으로 ZIP 압축하여 출력 스트림에 작성 - 위임
     */
    @Transactional(readOnly = true)
    public void streamPatchAsZip(Long patchId, OutputStream outputStream) {
        Patch patch = getPatch(patchId);
        patchDownloadService.streamPatchAsZip(patch, outputStream);
    }

    /**
     * 패치 ZIP 파일명 생성 - 위임
     */
    public String getZipFileName(Long patchId) {
        Patch patch = getPatch(patchId);
        return patchDownloadService.getZipFileName(patch);
    }

    /**
     * 패치 디렉토리의 압축 전 총 크기 계산 - 위임
     */
    public long calculateUncompressedSize(Long patchId) {
        Patch patch = getPatch(patchId);
        return patchDownloadService.calculateUncompressedSize(patch);
    }

    /**
     * 패치 ZIP 파일 내부 구조 조회 - 위임
     */
    @Transactional(readOnly = true)
    public PatchDto.DirectoryNode getZipFileStructure(Long patchId) {
        Patch patch = getPatch(patchId);
        return patchDownloadService.getZipFileStructure(patch);
    }

    /**
     * 패치 파일 내용 조회 - 위임
     */
    @Transactional(readOnly = true)
    public PatchDto.FileContentResponse getFileContent(Long patchId, String relativePath) {
        Patch patch = getPatch(patchId);
        return patchDownloadService.getFileContent(patch, relativePath);
    }

    /**
     * 패치 삭제 (DB 레코드 + 실제 파일)
     *
     * @param patchId 패치 ID
     */
    @Transactional
    public void deletePatch(Long patchId) {
        // 1. 패치 조회
        Patch patch = getPatch(patchId);

        // 2. 실제 파일 디렉토리 삭제
        Path patchDir = Paths.get(releaseBasePath, patch.getOutputPath());

        if (Files.exists(patchDir)) {
            try {
                deleteDirectoryRecursively(patchDir);
                log.info("패치 디렉토리 삭제 완료: {}", patchDir.toAbsolutePath());
            } catch (IOException e) {
                log.error("패치 디렉토리 삭제 실패: {}", patchDir, e);
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "패치 파일 삭제 중 오류가 발생했습니다: " + e.getMessage());
            }
        } else {
            log.warn("패치 디렉토리가 존재하지 않습니다: {}", patchDir);
        }

        // 3. DB 레코드 삭제
        patchRepository.delete(patch);

        log.info("패치 삭제 완료 - ID: {}, Name: {}", patchId, patch.getPatchName());
    }

    /**
     * 디렉토리 재귀적 삭제
     */
    private void deleteDirectoryRecursively(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return;
        }

        try (var stream = Files.walk(directory)) {
            stream.sorted((p1, p2) -> -p1.compareTo(p2)) // 역순 정렬 (하위 항목부터 삭제)
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            log.debug("파일/디렉토리 삭제: {}", path);
                        } catch (IOException e) {
                            log.warn("파일/디렉토리 삭제 실패: {}", path, e);
                        }
                    });
        }
    }
}
