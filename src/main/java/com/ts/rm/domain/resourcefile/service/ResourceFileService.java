package com.ts.rm.domain.resourcefile.service;

import com.ts.rm.domain.resourcefile.dto.ResourceFileDto;
import com.ts.rm.domain.resourcefile.entity.ResourceFile;
import com.ts.rm.domain.resourcefile.repository.ResourceFileRepository;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import com.ts.rm.global.file.FileChecksumUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * ResourceFile 서비스
 *
 * <p>리소스 파일 업로드, 다운로드, 삭제 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResourceFileService {

    private final ResourceFileRepository resourceFileRepository;

    @Value("${app.release.base-path:src/main/resources/release}")
    private String baseReleasePath;

    /**
     * 리소스 파일 업로드
     *
     * @param file       업로드할 파일
     * @param request    업로드 요청 정보
     * @return 저장된 리소스 파일 엔티티
     */
    @Transactional
    public ResourceFile uploadFile(MultipartFile file, ResourceFileDto.UploadRequest request) {
        log.info("리소스 파일 업로드 시작 - 파일명: {}, 카테고리: {}, 서브카테고리: {}",
                file.getOriginalFilename(), request.fileCategory(), request.subCategory());

        // 파일명 검증
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "파일명이 없습니다");
        }

        // 파일 타입 추출 (확장자 대문자)
        String fileType = extractFileType(originalFileName);

        // 저장 경로 생성 (resource/script/MARIADB 또는 resource/document 등)
        String relativePath = buildRelativePath(request.fileCategory(), request.subCategory(), originalFileName);
        Path targetPath = Paths.get(baseReleasePath, relativePath);

        // 중복 파일 검사
        if (resourceFileRepository.existsByFilePath(relativePath)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 동일한 경로에 파일이 존재합니다: " + relativePath);
        }

        // sortOrder 자동 채번: 파일 카테고리별로 최대값 + 1
        Integer maxSortOrder = resourceFileRepository.findMaxSortOrderByFileCategory(request.fileCategory().toUpperCase());
        Integer sortOrder = maxSortOrder + 1;

        try {
            // 디렉토리 생성
            Files.createDirectories(targetPath.getParent());

            // 파일 저장
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("파일 저장 완료: {}", targetPath);

            // 체크섬 계산
            String checksum = FileChecksumUtil.calculateChecksum(targetPath);

            // 엔티티 생성 및 저장
            ResourceFile resourceFile = ResourceFile.builder()
                    .fileType(fileType)
                    .fileCategory(request.fileCategory().toUpperCase())
                    .subCategory(request.subCategory() != null ? request.subCategory().toUpperCase() : null)
                    .resourceFileName(request.resourceFileName())
                    .fileName(originalFileName)
                    .filePath(relativePath)
                    .fileSize(file.getSize())
                    .checksum(checksum)
                    .description(request.description())
                    .sortOrder(sortOrder)
                    .createdBy(request.createdBy())
                    .build();

            ResourceFile saved = resourceFileRepository.save(resourceFile);
            log.info("리소스 파일 업로드 완료 - ID: {}, 경로: {}", saved.getResourceFileId(), relativePath);

            return saved;

        } catch (IOException e) {
            log.error("파일 저장 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "파일 저장에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 리소스 파일 다운로드 (스트리밍)
     *
     * @param id           리소스 파일 ID
     * @param outputStream 출력 스트림
     */
    public void downloadFile(Long id, OutputStream outputStream) {
        ResourceFile resourceFile = getResourceFile(id);
        Path filePath = Paths.get(baseReleasePath, resourceFile.getFilePath());

        log.info("리소스 파일 다운로드 - ID: {}, 경로: {}", id, filePath);

        if (!Files.exists(filePath)) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND, "파일이 존재하지 않습니다: " + resourceFile.getFilePath());
        }

        try (InputStream is = Files.newInputStream(filePath)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        } catch (IOException e) {
            log.error("파일 다운로드 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_DOWNLOAD_FAILED, "파일 다운로드에 실패했습니다");
        }
    }

    /**
     * 리소스 파일 수정
     *
     * @param id      리소스 파일 ID
     * @param request 수정 요청 정보
     * @return 수정된 리소스 파일 엔티티
     */
    @Transactional
    public ResourceFile updateFile(Long id, ResourceFileDto.UpdateRequest request) {
        log.info("리소스 파일 수정 시작 - ID: {}, 파일명: {}", id, request.resourceFileName());

        ResourceFile resourceFile = getResourceFile(id);

        // 엔티티 업데이트 (파일은 수정하지 않고 메타데이터만 수정)
        resourceFile.setFileCategory(request.fileCategory().toUpperCase());
        resourceFile.setSubCategory(request.subCategory() != null ? request.subCategory().toUpperCase() : null);
        resourceFile.setResourceFileName(request.resourceFileName());
        resourceFile.setDescription(request.description());

        log.info("리소스 파일 수정 완료 - ID: {}", id);
        return resourceFile;
    }

    /**
     * 리소스 파일 삭제
     *
     * @param id 리소스 파일 ID
     */
    @Transactional
    public void deleteFile(Long id) {
        ResourceFile resourceFile = getResourceFile(id);
        Path filePath = Paths.get(baseReleasePath, resourceFile.getFilePath());

        log.info("리소스 파일 삭제 시작 - ID: {}, 경로: {}", id, filePath);

        // 실제 파일 삭제
        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("실제 파일 삭제 완료: {}", filePath);
            } else {
                log.warn("삭제할 파일이 존재하지 않음: {}", filePath);
            }
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", e.getMessage(), e);
            // 파일 삭제 실패해도 DB 레코드는 삭제 진행
        }

        // DB 레코드 삭제
        resourceFileRepository.delete(resourceFile);
        log.info("리소스 파일 삭제 완료 - ID: {}", id);
    }

    /**
     * 리소스 파일 단건 조회
     */
    public ResourceFile getResourceFile(Long id) {
        return resourceFileRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "리소스 파일을 찾을 수 없습니다: " + id));
    }

    /**
     * 전체 리소스 파일 목록 조회 (sortOrder 오름차순, 생성일시 내림차순)
     */
    public List<ResourceFile> listAllFiles() {
        return resourceFileRepository.findAllByOrderBySortOrderAscCreatedAtDesc();
    }

    /**
     * 파일 카테고리별 리소스 파일 목록 조회 (sortOrder 오름차순, 생성일시 내림차순)
     *
     * @param fileCategory 파일 카테고리 (SCRIPT, DOCUMENT, ETC)
     */
    public List<ResourceFile> listFilesByCategory(String fileCategory) {
        return resourceFileRepository.findByFileCategoryOrderBySortOrderAscCreatedAtDesc(fileCategory.toUpperCase());
    }

    /**
     * 리소스 파일 목록 조회 (카테고리 필터링 + 키워드 검색)
     * QueryDSL을 사용한 다중 필드 키워드 검색 (리소스파일명, 파일명, 설명)
     *
     * @param fileCategory 파일 카테고리 (null이면 전체)
     * @param keyword 검색 키워드 - 리소스파일명, 파일명, 설명 통합 검색 (null이면 전체)
     * @return 리소스 파일 목록
     */
    public List<ResourceFile> listFilesWithFilters(String fileCategory, String keyword) {
        return resourceFileRepository.findAllWithFilters(fileCategory, keyword);
    }

    /**
     * 리소스 파일 순서 변경
     *
     * @param request 순서 변경 요청
     */
    @Transactional
    public void reorderResourceFiles(ResourceFileDto.ReorderResourceFilesRequest request) {
        log.info("리소스 파일 순서 변경 시작 - fileCategory: {}, resourceFileIds: {}",
                request.fileCategory(), request.resourceFileIds());

        List<Long> resourceFileIds = request.resourceFileIds();
        if (resourceFileIds == null || resourceFileIds.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "리소스 파일 ID 목록은 비어있을 수 없습니다");
        }

        // 모든 리소스 파일이 존재하고 동일한 fileCategory인지 확인
        for (Long resourceFileId : resourceFileIds) {
            ResourceFile resourceFile = getResourceFile(resourceFileId);
            if (!request.fileCategory().equalsIgnoreCase(resourceFile.getFileCategory())) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                        "리소스 파일 " + resourceFileId + "는 " + request.fileCategory() + " 카테고리에 속하지 않습니다");
            }
        }

        // sortOrder 업데이트 (1부터 시작)
        int sortOrder = 1;
        for (Long resourceFileId : resourceFileIds) {
            ResourceFile resourceFile = getResourceFile(resourceFileId);
            resourceFile.setSortOrder(sortOrder++);
        }

        log.info("리소스 파일 순서 변경 완료 - fileCategory: {}", request.fileCategory());
    }

    /**
     * 파일명에서 확장자 추출 (대문자)
     */
    private String extractFileType(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "UNKNOWN";
        }
        return fileName.substring(lastDotIndex + 1).toUpperCase();
    }

    /**
     * 저장 경로 생성
     *
     * @param category     카테고리 (script, document, docker 등)
     * @param subDirectory 서브 디렉토리 (MARIADB, CRATEDB, INFRAEYE1, INFRAEYE2 등)
     * @param fileName     파일명
     * @return 상대 경로 (예: resource/script/MARIADB/backup.sh)
     */
    private String buildRelativePath(String category, String subDirectory, String fileName) {
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append("resource/");
        pathBuilder.append(category.toLowerCase());

        if (subDirectory != null && !subDirectory.isBlank()) {
            pathBuilder.append("/").append(subDirectory.toUpperCase());
        }

        pathBuilder.append("/").append(fileName);
        return pathBuilder.toString();
    }

    /**
     * 파일명 조회
     */
    public String getFileName(Long id) {
        return getResourceFile(id).getFileName();
    }

    /**
     * 파일 크기 조회
     */
    public long getFileSize(Long id) {
        ResourceFile resourceFile = getResourceFile(id);
        return resourceFile.getFileSize() != null ? resourceFile.getFileSize() : 0L;
    }
}
