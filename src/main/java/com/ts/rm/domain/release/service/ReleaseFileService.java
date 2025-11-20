package com.ts.rm.domain.release.service;

import com.ts.rm.domain.release.dto.ReleaseFileDto;
import com.ts.rm.domain.release.entity.ReleaseFile;
import com.ts.rm.domain.release.entity.ReleaseVersion;
import com.ts.rm.domain.release.mapper.ReleaseFileDtoMapper;
import com.ts.rm.domain.release.repository.ReleaseFileRepository;
import com.ts.rm.domain.release.repository.ReleaseVersionRepository;
import com.ts.rm.global.common.exception.BusinessException;
import com.ts.rm.global.common.exception.ErrorCode;
import com.ts.rm.global.file.FileStorageService;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * ReleaseFile Service
 *
 * <p>릴리즈 파일 관리 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReleaseFileService {

    private final ReleaseFileRepository releaseFileRepository;
    private final ReleaseVersionRepository releaseVersionRepository;
    private final ReleaseFileDtoMapper mapper;
    private final FileStorageService fileStorageService;

    /**
     * 릴리즈 파일 생성
     */
    @Transactional
    public ReleaseFileDto.DetailResponse createReleaseFile(ReleaseFileDto.CreateRequest request) {
        log.info("Creating release file: {} for versionId: {}", request.fileName(),
                request.releaseVersionId());

        ReleaseVersion releaseVersion = findReleaseVersionById(request.releaseVersionId());

        ReleaseFile releaseFile = ReleaseFile.builder()
                .releaseVersion(releaseVersion)
                .databaseType(request.databaseType())
                .fileName(request.fileName())
                .filePath(request.filePath())
                .fileSize(request.fileSize())
                .checksum(request.checksum())
                .executionOrder(request.executionOrder())
                .description(request.description())
                .build();

        ReleaseFile savedReleaseFile = releaseFileRepository.save(releaseFile);

        log.info("Release file created successfully with id: {}",
                savedReleaseFile.getReleaseFileId());
        return mapper.toDetailResponse(savedReleaseFile);
    }

    /**
     * 릴리즈 파일 조회 (ID)
     */
    public ReleaseFileDto.DetailResponse getReleaseFileById(Long releaseFileId) {
        ReleaseFile releaseFile = findReleaseFileById(releaseFileId);
        return mapper.toDetailResponse(releaseFile);
    }

    /**
     * 버전별 릴리즈 파일 목록 조회
     */
    public List<ReleaseFileDto.SimpleResponse> getReleaseFilesByVersion(Long versionId) {
        findReleaseVersionById(versionId);

        List<ReleaseFile> releaseFiles = releaseFileRepository
                .findAllByReleaseVersionIdOrderByExecutionOrderAsc(versionId);
        return mapper.toSimpleResponseList(releaseFiles);
    }

    /**
     * 버전+DB타입별 릴리즈 파일 목록 조회
     */
    public List<ReleaseFileDto.SimpleResponse> getReleaseFilesByVersionAndDbType(Long versionId,
            String databaseType) {
        findReleaseVersionById(versionId);

        List<ReleaseFile> releaseFiles = releaseFileRepository.findByVersionAndDatabaseType(versionId,
                databaseType);
        return mapper.toSimpleResponseList(releaseFiles);
    }

    /**
     * 파일 경로로 릴리즈 파일 조회
     */
    public ReleaseFileDto.DetailResponse getReleaseFileByPath(String filePath) {
        ReleaseFile releaseFile = releaseFileRepository.findByFilePath(filePath)
                .orElseThrow(() -> new BusinessException(ErrorCode.PATCH_FILE_NOT_FOUND));
        return mapper.toDetailResponse(releaseFile);
    }

    /**
     * 버전 범위 내 릴리즈 파일 조회
     */
    public List<ReleaseFileDto.SimpleResponse> getReleaseFilesBetweenVersions(String fromVersion,
            String toVersion, String databaseType) {

        List<ReleaseFile> releaseFiles = releaseFileRepository.findReleaseFilesBetweenVersions(fromVersion,
                toVersion, databaseType);
        return mapper.toSimpleResponseList(releaseFiles);
    }

    /**
     * 릴리즈 파일 정보 수정
     */
    @Transactional
    public ReleaseFileDto.DetailResponse updateReleaseFile(Long releaseFileId,
            ReleaseFileDto.UpdateRequest request) {
        log.info("Updating release file with releaseFileId: {}", releaseFileId);

        findReleaseFileById(releaseFileId);

        releaseFileRepository.updateReleaseFileInfo(releaseFileId, request.description(),
                request.executionOrder());

        ReleaseFile updatedReleaseFile = findReleaseFileById(releaseFileId);

        log.info("Release file updated successfully with releaseFileId: {}", releaseFileId);
        return mapper.toDetailResponse(updatedReleaseFile);
    }

    /**
     * 릴리즈 파일 삭제
     */
    @Transactional
    public void deleteReleaseFile(Long releaseFileId) {
        log.info("Deleting release file with releaseFileId: {}", releaseFileId);

        ReleaseFile releaseFile = findReleaseFileById(releaseFileId);
        releaseFileRepository.delete(releaseFile);

        log.info("Release file deleted successfully with releaseFileId: {}", releaseFileId);
    }

    /**
     * 릴리즈 파일 업로드 (다중 파일)
     */
    @Transactional
    public List<ReleaseFileDto.DetailResponse> uploadReleaseFiles(Long versionId,
            List<MultipartFile> files, ReleaseFileDto.UploadRequest request) {
        log.info("Uploading {} release files for versionId: {}", files.size(), versionId);

        ReleaseVersion releaseVersion = findReleaseVersionById(versionId);

        List<ReleaseFileDto.DetailResponse> responses = new ArrayList<>();

        // 기존 파일 중 최대 실행 순서 조회 (동일 DB 타입 기준)
        int maxOrder = releaseFileRepository
                .findByVersionAndDatabaseType(versionId, request.databaseType())
                .stream()
                .mapToInt(ReleaseFile::getExecutionOrder)
                .max()
                .orElse(0);

        int executionOrder = maxOrder + 1;

        for (MultipartFile file : files) {
            try {
                // 파일 검증
                validateFile(file);

                byte[] content = file.getBytes();
                String checksum = calculateChecksum(content);

                // 파일 경로 생성: releases/{type}/{majorMinor}/{version}/patch/{dbType}/{fileName}
                String relativePath = String.format("releases/%s/%s/%s/patch/%s/%s",
                        releaseVersion.getReleaseType().toLowerCase(),
                        releaseVersion.getMajorMinor(),
                        releaseVersion.getVersion(),
                        request.databaseType().toLowerCase(),
                        file.getOriginalFilename());

                // 실제 파일 저장
                fileStorageService.saveFile(file, relativePath);

                // DB에 메타데이터 저장
                ReleaseFile releaseFile = ReleaseFile.builder()
                        .releaseVersion(releaseVersion)
                        .databaseType(request.databaseType())
                        .fileName(file.getOriginalFilename())
                        .filePath(relativePath)
                        .fileSize(file.getSize())
                        .checksum(checksum)
                        .executionOrder(executionOrder++)
                        .description(request.uploadedBy() + "가 업로드한 파일")
                        .build();

                ReleaseFile savedReleaseFile = releaseFileRepository.save(releaseFile);
                responses.add(mapper.toDetailResponse(savedReleaseFile));

                log.info("Release file uploaded: {} -> {}", file.getOriginalFilename(), relativePath);

            } catch (IOException e) {
                log.error("Failed to upload release file: {}", file.getOriginalFilename(), e);
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "파일 업로드 실패: " + e.getMessage());
            }
        }

        log.info("Successfully uploaded {} release files", responses.size());
        return responses;
    }

    /**
     * 파일 검증
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "빈 파일입니다");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".sql")) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "SQL 파일만 업로드 가능합니다: " + fileName);
        }

        // 10MB 제한
        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "파일 크기는 10MB를 초과할 수 없습니다");
        }
    }

    /**
     * 릴리즈 파일 다운로드
     */
    public Resource downloadReleaseFile(Long releaseFileId) {
        log.info("Downloading release file with releaseFileId: {}", releaseFileId);

        ReleaseFile releaseFile = findReleaseFileById(releaseFileId);

        // 실제 파일 로드
        Resource resource = fileStorageService.loadFile(releaseFile.getFilePath());

        log.info("Release file downloaded successfully: {}", releaseFile.getFileName());
        return resource;
    }

    /**
     * 체크섬 계산 (MD5)
     */
    public String calculateChecksum(byte[] content) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(content);

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            log.error("MD5 algorithm not found", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private ReleaseVersion findReleaseVersionById(Long versionId) {
        return releaseVersionRepository.findById(versionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RELEASE_VERSION_NOT_FOUND));
    }

    private ReleaseFile findReleaseFileById(Long releaseFileId) {
        return releaseFileRepository.findById(releaseFileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PATCH_FILE_NOT_FOUND));
    }
}
