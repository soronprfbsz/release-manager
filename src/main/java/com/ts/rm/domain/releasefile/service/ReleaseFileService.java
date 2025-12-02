package com.ts.rm.domain.releasefile.service;

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
import com.ts.rm.global.file.ZipUtil;
import com.ts.rm.global.file.StreamingZipUtil;
import com.ts.rm.domain.common.service.FileStorageService;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    @Transactional
    public ReleaseFileDto.DetailResponse createReleaseFile(ReleaseFileDto.CreateRequest request) {
        log.info("Creating release file: {} for versionId: {}", request.fileName(),
                request.releaseVersionId());

        ReleaseVersion releaseVersion = findReleaseVersionById(request.releaseVersionId());

        FileCategory fileCategory = request.fileCategory() != null
                ? FileCategory.fromCode(request.fileCategory())
                : determineFileCategory(request.fileName(), request.subCategory());

        String subCategory = request.subCategory() != null
                ? request.subCategory()
                : determineSubCategory(fileCategory, request.subCategory());

        ReleaseFile releaseFile = ReleaseFile.builder()
                .releaseVersion(releaseVersion)
                .fileCategory(fileCategory)
                .subCategory(subCategory)
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

    public ReleaseFileDto.DetailResponse getReleaseFileById(Long releaseFileId) {
        ReleaseFile releaseFile = findReleaseFileById(releaseFileId);
        return mapper.toDetailResponse(releaseFile);
    }

    public List<ReleaseFileDto.SimpleResponse> getReleaseFilesByVersion(Long versionId) {
        findReleaseVersionById(versionId);

        List<ReleaseFile> releaseFiles = releaseFileRepository
                .findAllByReleaseVersionIdOrderByExecutionOrderAsc(versionId);
        return mapper.toSimpleResponseList(releaseFiles);
    }

    public List<ReleaseFileDto.SimpleResponse> getReleaseFilesByVersionAndCategory(Long versionId,
            FileCategory fileCategory) {
        findReleaseVersionById(versionId);

        List<ReleaseFile> releaseFiles = releaseFileRepository
                .findByReleaseVersionIdAndFileCategory(versionId, fileCategory);
        return mapper.toSimpleResponseList(releaseFiles);
    }

    public List<ReleaseFileDto.SimpleResponse> getReleaseFilesByVersionAndSubCategory(Long versionId,
            String subCategory) {
        findReleaseVersionById(versionId);

        List<ReleaseFile> releaseFiles = releaseFileRepository
                .findByReleaseVersionIdAndSubCategory(versionId, subCategory);
        return mapper.toSimpleResponseList(releaseFiles);
    }

    public ReleaseFileDto.DetailResponse getReleaseFileByPath(String filePath) {
        ReleaseFile releaseFile = releaseFileRepository.findByFilePath(filePath)
                .orElseThrow(() -> new BusinessException(ErrorCode.PATCH_FILE_NOT_FOUND));
        return mapper.toDetailResponse(releaseFile);
    }

    public List<ReleaseFileDto.SimpleResponse> getReleaseFilesBetweenVersions(String fromVersion,
            String toVersion) {

        List<ReleaseFile> releaseFiles = releaseFileRepository
                .findReleaseFilesBetweenVersionsExcludingInstall(fromVersion, toVersion);
        return mapper.toSimpleResponseList(releaseFiles);
    }

    @Transactional
    public ReleaseFileDto.DetailResponse updateReleaseFile(Long releaseFileId,
            ReleaseFileDto.UpdateRequest request) {
        log.info("Updating release file with releaseFileId: {}", releaseFileId);

        ReleaseFile releaseFile = findReleaseFileById(releaseFileId);

        if (request.description() != null) {
            releaseFile.setDescription(request.description());
        }
        if (request.executionOrder() != null) {
            releaseFile.setExecutionOrder(request.executionOrder());
        }

        log.info("Release file updated successfully with releaseFileId: {}", releaseFileId);
        return mapper.toDetailResponse(releaseFile);
    }

    @Transactional
    public void deleteReleaseFile(Long releaseFileId) {
        log.info("Deleting release file with releaseFileId: {}", releaseFileId);

        ReleaseFile releaseFile = findReleaseFileById(releaseFileId);
        releaseFileRepository.delete(releaseFile);

        log.info("Release file deleted successfully with releaseFileId: {}", releaseFileId);
    }

    @Transactional
    public List<ReleaseFileDto.DetailResponse> uploadReleaseFiles(Long versionId,
            List<MultipartFile> files, ReleaseFileDto.UploadRequest request) {
        log.info("Uploading {} release files for versionId: {}", files.size(), versionId);

        ReleaseVersion releaseVersion = findReleaseVersionById(versionId);

        List<ReleaseFileDto.DetailResponse> responses = new ArrayList<>();

        int maxOrder = releaseFileRepository
                .findAllByReleaseVersionIdOrderByExecutionOrderAsc(versionId)
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

                String categoryPath = subCategory != null ? subCategory : fileCategory.getCode();
                String relativePath = String.format("versions/%s/%s/%s/%s/%s",
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

    public Resource downloadReleaseFile(Long releaseFileId) {
        log.info("Downloading release file with releaseFileId: {}", releaseFileId);

        ReleaseFile releaseFile = findReleaseFileById(releaseFileId);

        Resource resource = fileStorageService.loadFile(releaseFile.getFilePath());

        log.info("Release file downloaded successfully: {}", releaseFile.getFileName());
        return resource;
    }

    /**
     * 버전별 파일을 스트리밍 방식으로 ZIP 압축
     *
     * <p>메모리에 전체 ZIP을 생성하지 않고 OutputStream에 직접 스트리밍합니다.
     * 메모리 사용량이 O(1)로 대용량 파일도 안전하게 처리 가능합니다.
     *
     * @param versionId    릴리즈 버전 ID
     * @param outputStream 출력 스트림 (HttpServletResponse.getOutputStream())
     * @throws BusinessException 파일이 없거나 압축 실패 시
     */
    public void streamVersionFilesAsZip(Long versionId, OutputStream outputStream) {
        log.info("버전별 파일 스트리밍 다운로드 요청 - versionId: {}", versionId);

        ReleaseVersion releaseVersion = findReleaseVersionById(versionId);

        List<ReleaseFile> releaseFiles = releaseFileRepository
                .findAllByReleaseVersionIdOrderByExecutionOrderAsc(versionId);

        if (releaseFiles.isEmpty()) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND,
                    "버전 " + releaseVersion.getVersion() + "에 파일이 없습니다");
        }

        List<ZipUtil.ZipFileEntry> zipEntries = buildZipEntries(releaseFiles);

        log.info("스트리밍 ZIP 압축 시작 - 버전: {}, 파일 개수: {}",
                releaseVersion.getVersion(), zipEntries.size());

        // 스트리밍 방식으로 압축 (메모리 효율적)
        StreamingZipUtil.compressFilesToStream(outputStream, zipEntries);

        log.info("버전 {} 스트리밍 압축 완료 - {} 개 파일",
                releaseVersion.getVersion(), releaseFiles.size());
    }

    /**
     * 버전별 파일을 메모리 기반으로 ZIP 압축 (레거시)
     *
     * @param versionId 릴리즈 버전 ID
     * @return ZIP 파일 바이트 배열
     * @deprecated 메모리 사용량이 많아 OOM 위험이 있습니다. {@link #streamVersionFilesAsZip(Long, OutputStream)} 사용을 권장합니다.
     */
    @Deprecated(since = "1.0", forRemoval = false)
    public byte[] downloadVersionFilesAsZip(Long versionId) {
        log.warn("레거시 메모리 기반 ZIP 압축 호출 - versionId: {} (스트리밍 방식 사용 권장)", versionId);

        ReleaseVersion releaseVersion = findReleaseVersionById(versionId);

        List<ReleaseFile> releaseFiles = releaseFileRepository
                .findAllByReleaseVersionIdOrderByExecutionOrderAsc(versionId);

        if (releaseFiles.isEmpty()) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND,
                    "버전 " + releaseVersion.getVersion() + "에 파일이 없습니다");
        }

        List<ZipUtil.ZipFileEntry> zipEntries = buildZipEntries(releaseFiles);

        log.info("ZIP 압축 시작 - 버전: {}, 파일 개수: {}", releaseVersion.getVersion(), zipEntries.size());

        byte[] zipBytes = ZipUtil.compressFiles(zipEntries);

        log.info("버전 {} 파일 압축 완료 - {} 개 파일, {} bytes",
                releaseVersion.getVersion(), releaseFiles.size(), zipBytes.length);

        return zipBytes;
    }

    /**
     * ReleaseFile 목록을 ZipFileEntry 목록으로 변환 (공통 로직)
     */
    private List<ZipUtil.ZipFileEntry> buildZipEntries(List<ReleaseFile> releaseFiles) {
        List<ZipUtil.ZipFileEntry> zipEntries = new ArrayList<>();

        for (ReleaseFile file : releaseFiles) {
            Path sourcePath = fileStorageService.getAbsolutePath(file.getFilePath());

            String zipEntryPath;
            if (file.getFileCategory() != null) {
                // ZIP 경로는 소문자로 변환
                String categoryPath = file.getFileCategory().getCode().toLowerCase();
                if (file.getSubCategory() != null && !file.getSubCategory().isEmpty()) {
                    zipEntryPath = String.format("%s/%s/%s",
                            categoryPath, file.getSubCategory().toLowerCase(), file.getFileName());
                } else {
                    zipEntryPath = String.format("%s/%s", categoryPath, file.getFileName());
                }
            } else {
                zipEntryPath = file.getFileName();
            }

            log.debug("ZIP 엔트리 추가 - 카테고리: {}, 하위카테고리: {}, 파일: {}, ZIP경로: {}",
                    file.getFileCategory(), file.getSubCategory(), file.getFileName(), zipEntryPath);

            zipEntries.add(new ZipUtil.ZipFileEntry(sourcePath, zipEntryPath));
        }

        return zipEntries;
    }

    public String getVersionZipFileName(Long versionId) {
        ReleaseVersion releaseVersion = findReleaseVersionById(versionId);
        return String.format("release_%s.zip", releaseVersion.getVersion());
    }

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
     * 파일명으로 파일 카테고리 결정
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

        // 문서 파일 → INSTALL
        if (lowerCaseFileName.endsWith(".pdf") || lowerCaseFileName.endsWith(".md")
                || lowerCaseFileName.endsWith(".txt")) {
            return FileCategory.INSTALL;
        }

        // 실행 파일 → INSTALL
        if (lowerCaseFileName.endsWith(".exe") || lowerCaseFileName.endsWith(".sh")) {
            return FileCategory.INSTALL;
        }

        // 기본값: 하위 카테고리가 DB 타입이면 DATABASE, 아니면 INSTALL
        return (subCategory != null && (subCategory.equalsIgnoreCase("mariadb")
                || subCategory.equalsIgnoreCase("cratedb")))
                ? FileCategory.DATABASE
                : FileCategory.INSTALL;
    }

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

}
