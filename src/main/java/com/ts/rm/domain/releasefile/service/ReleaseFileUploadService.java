package com.ts.rm.domain.releasefile.service;

import com.ts.rm.domain.common.service.FileStorageService;
import com.ts.rm.domain.releasefile.dto.ReleaseFileDto;
import com.ts.rm.domain.releasefile.entity.ReleaseFile;
import com.ts.rm.domain.releasefile.enums.FileCategory;
import com.ts.rm.domain.releasefile.mapper.ReleaseFileDtoMapper;
import com.ts.rm.domain.releasefile.repository.ReleaseFileRepository;
import com.ts.rm.domain.releasefile.util.SubCategoryValidator;
import com.ts.rm.domain.releaseversion.entity.ReleaseVersion;
import com.ts.rm.domain.releaseversion.repository.ReleaseVersionRepository;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * ReleaseFile 업로드 전용 서비스
 *
 * <p>파일 업로드, 검증, 메타데이터 추출을 담당합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReleaseFileUploadService {

    private final ReleaseFileRepository releaseFileRepository;
    private final ReleaseVersionRepository releaseVersionRepository;
    private final ReleaseFileDtoMapper mapper;
    private final FileStorageService fileStorageService;

    /**
     * 릴리즈 파일 업로드 (다중 파일)
     *
     * @param versionId 릴리즈 버전 ID
     * @param files     업로드할 파일 목록
     * @param request   업로드 요청 정보 (카테고리, 업로드자 등)
     * @return 생성된 파일 정보 목록
     */
    @Transactional
    public List<ReleaseFileDto.DetailResponse> uploadReleaseFiles(Long versionId,
            List<MultipartFile> files, ReleaseFileDto.UploadRequest request) {
        log.info("Uploading {} release files for versionId: {}", files.size(), versionId);

        ReleaseVersion releaseVersion = findReleaseVersionById(versionId);

        List<ReleaseFileDto.DetailResponse> responses = new ArrayList<>();

        int maxOrder = releaseFileRepository
                .findAllByReleaseVersion_ReleaseVersionIdOrderByExecutionOrderAsc(versionId)
                .stream()
                .mapToInt(ReleaseFile::getExecutionOrder)
                .max()
                .orElse(0);

        int executionOrder = maxOrder + 1;

        for (MultipartFile file : files) {
            try {
                validateFile(file);

                byte[] content = file.getBytes();
                String checksum = calculateChecksum(content);

                FileCategory fileCategory = request.fileCategory() != null
                        ? FileCategory.fromCode(request.fileCategory())
                        : determineFileCategory(file.getOriginalFilename(), request.subCategory());

                String subCategory = request.subCategory() != null
                        ? request.subCategory()
                        : determineSubCategory(fileCategory, null);

                String projectId = releaseVersion.getProject() != null ? releaseVersion.getProject().getProjectId() : "infraeye2";
                String categoryPath = subCategory != null ? subCategory : fileCategory.getCode();
                String relativePath = String.format("versions/%s/%s/%s/%s/%s/%s",
                        projectId,
                        releaseVersion.getReleaseType().toLowerCase(),
                        releaseVersion.getMajorMinor(),
                        releaseVersion.getVersion(),
                        categoryPath.toLowerCase(),
                        file.getOriginalFilename());

                fileStorageService.saveFile(file, relativePath);

                String fileType = determineFileType(file.getOriginalFilename());

                ReleaseFile releaseFile = ReleaseFile.builder()
                        .releaseVersion(releaseVersion)
                        .fileType(fileType)
                        .fileCategory(fileCategory)
                        .subCategory(subCategory)
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
     * 파일 유효성 검증
     *
     * @param file 검증할 파일
     * @throws BusinessException 파일이 유효하지 않을 경우
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

        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "파일 크기는 10MB를 초과할 수 없습니다");
        }
    }

    /**
     * 파일 체크섬(MD5) 계산
     *
     * @param content 파일 내용
     * @return MD5 해시값 (32자리 16진수)
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

    /**
     * 파일 확장자로 파일 타입 결정
     *
     * @param fileName 파일명
     * @return 파일 타입 (SQL, MD, PDF, EXE, etc.)
     */
    private String determineFileType(String fileName) {
        String lowerCaseFileName = fileName.toLowerCase();

        if (lowerCaseFileName.endsWith(".sql")) {
            return "SQL";
        } else if (lowerCaseFileName.endsWith(".md")) {
            return "MD";
        } else if (lowerCaseFileName.endsWith(".pdf")) {
            return "PDF";
        } else if (lowerCaseFileName.endsWith(".exe")) {
            return "EXE";
        } else if (lowerCaseFileName.endsWith(".sh")) {
            return "SH";
        } else if (lowerCaseFileName.endsWith(".txt")) {
            return "TXT";
        } else if (lowerCaseFileName.endsWith(".war")) {
            return "WAR";
        } else if (lowerCaseFileName.endsWith(".jar")) {
            return "JAR";
        } else if (lowerCaseFileName.endsWith(".tar") || lowerCaseFileName.endsWith(".tar.gz")) {
            return "TAR";
        } else if (lowerCaseFileName.endsWith(".zip")) {
            return "ZIP";
        } else {
            return "UNDEFINED";
        }
    }

    /**
     * 파일명과 하위 카테고리로 파일 카테고리 결정
     *
     * @param fileName    파일명
     * @param subCategory 하위 카테고리 (nullable)
     * @return 파일 카테고리 (DATABASE, WEB, ENGINE)
     */
    private FileCategory determineFileCategory(String fileName, String subCategory) {
        String lowerCaseFileName = fileName.toLowerCase();

        // SQL 파일 → DATABASE
        if (lowerCaseFileName.endsWith(".sql")) {
            return FileCategory.DATABASE;
        }

        // 빌드 산출물 파일 → WEB, ENGINE
        if (lowerCaseFileName.endsWith(".war")) {
            return FileCategory.WEB;
        }
        if (lowerCaseFileName.endsWith(".jar")) {
            return lowerCaseFileName.contains("engine") ? FileCategory.ENGINE : FileCategory.WEB;
        }
        if (lowerCaseFileName.endsWith(".tar") || lowerCaseFileName.endsWith(".tar.gz")) {
            if (lowerCaseFileName.contains("web")) {
                return FileCategory.WEB;
            } else {
                return FileCategory.ENGINE;
            }
        }

        // 문서 파일 → ENGINE (기본 카테고리)
        if (lowerCaseFileName.endsWith(".pdf") || lowerCaseFileName.endsWith(".md")
                || lowerCaseFileName.endsWith(".txt")) {
            return FileCategory.ENGINE;
        }

        // 실행 파일 → ENGINE (기본 카테고리)
        if (lowerCaseFileName.endsWith(".exe") || lowerCaseFileName.endsWith(".sh")) {
            return FileCategory.ENGINE;
        }

        // 기본값: 하위 카테고리가 DB 타입이면 DATABASE, 아니면 ENGINE
        return (subCategory != null && (subCategory.equalsIgnoreCase("mariadb")
                || subCategory.equalsIgnoreCase("cratedb")))
                ? FileCategory.DATABASE
                : FileCategory.ENGINE;
    }

    /**
     * 파일 카테고리에 따른 하위 카테고리 결정 및 검증
     *
     * @param fileCategory 파일 카테고리
     * @param subCategory  요청된 하위 카테고리 (nullable)
     * @return 검증된 하위 카테고리 또는 null
     */
    private String determineSubCategory(FileCategory fileCategory, String subCategory) {
        if (subCategory != null && !subCategory.isEmpty()) {
            String upperSubCategory = subCategory.toUpperCase();

            // DATABASE와 ENGINE 카테고리는 폴더명 기반 판단 후 CODE 테이블에 존재하는지 확인
            if (fileCategory == FileCategory.DATABASE || fileCategory == FileCategory.ENGINE) {
                // SubCategoryValidator를 통해 유효한 값인지 확인
                if (SubCategoryValidator.isValid(fileCategory, upperSubCategory)) {
                    return upperSubCategory;
                } else {
                    // 유효하지 않은 값이면 ETC로 반환
                    return "ETC";
                }
            }

            // 다른 카테고리(WEB, INSTALL)는 명시적으로 전달된 값만 사용
            return upperSubCategory;
        }
        return null;
    }

    /**
     * 릴리즈 버전 조회
     *
     * @param versionId 버전 ID
     * @return ReleaseVersion 엔티티
     * @throws BusinessException 버전을 찾을 수 없을 경우
     */
    private ReleaseVersion findReleaseVersionById(Long versionId) {
        return releaseVersionRepository.findById(versionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RELEASE_VERSION_NOT_FOUND));
    }
}
