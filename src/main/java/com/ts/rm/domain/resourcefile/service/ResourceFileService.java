package com.ts.rm.domain.resourcefile.service;

import com.ts.rm.domain.resourcefile.dto.ResourceFileDto;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import com.ts.rm.global.file.FileContentUtil;
import com.ts.rm.global.file.StreamingZipUtil;
import com.ts.rm.global.file.ZipExtractUtil;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * ResourceFile Service
 *
 * <p>카테고리별 리소스 파일 관리 비즈니스 로직 (파일시스템 기반)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceFileService {

    @Value("${app.release.base-path:data/release-manager}")
    private String baseReleasePath;

    private static final String RESOURCES_DIR = "resources/file";

    /**
     * 카테고리 목록 조회 (파일시스템 기반)
     *
     * <p>resources/file 하위 폴더를 카테고리로 인식
     *
     * @return 카테고리 목록 응답
     */
    public ResourceFileDto.CategoriesResponse getCategories() {
        log.info("카테고리 목록 조회 요청");

        Path resourcesPath = Paths.get(baseReleasePath, RESOURCES_DIR);

        // 디렉토리가 존재하지 않는 경우 빈 응답 반환
        if (!Files.exists(resourcesPath) || !Files.isDirectory(resourcesPath)) {
            log.info("리소스 디렉토리 미존재 - path: {}", resourcesPath);
            return new ResourceFileDto.CategoriesResponse(0, List.of());
        }

        List<ResourceFileDto.CategoryInfo> categories = new ArrayList<>();

        try (Stream<Path> stream = Files.list(resourcesPath)) {
            stream.filter(Files::isDirectory)
                    .sorted(Comparator.comparing(p -> p.getFileName().toString().toLowerCase()))
                    .forEach(categoryPath -> {
                        String categoryName = categoryPath.getFileName().toString();
                        CategoryStats stats = calculateCategoryStats(categoryPath);
                        categories.add(new ResourceFileDto.CategoryInfo(
                                categoryName,
                                stats.fileCount,
                                stats.totalSize
                        ));
                    });
        } catch (IOException e) {
            log.error("카테고리 목록 조회 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "카테고리 목록 조회에 실패했습니다");
        }

        log.info("카테고리 목록 조회 완료 - categoryCount: {}", categories.size());
        return new ResourceFileDto.CategoriesResponse(categories.size(), categories);
    }

    /**
     * 카테고리 생성 (resources/file 하위에 폴더 생성)
     *
     * @param categoryName 카테고리명 (폴더명)
     * @return 생성 결과 응답
     */
    public ResourceFileDto.CategoryCreateResponse createCategory(String categoryName) {
        log.info("카테고리 생성 요청 - categoryName: {}", categoryName);

        if (categoryName == null || categoryName.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "카테고리명이 비어있습니다");
        }

        String normalizedCategory = categoryName.toLowerCase().trim();

        // 특수문자 검증 (영문, 숫자, 하이픈, 언더스코어만 허용)
        if (!normalizedCategory.matches("^[a-z0-9_-]+$")) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "카테고리명은 영문 소문자, 숫자, 하이픈(-), 언더스코어(_)만 사용 가능합니다");
        }

        Path categoryPath = Paths.get(baseReleasePath, RESOURCES_DIR, normalizedCategory);

        if (Files.exists(categoryPath)) {
            throw new BusinessException(ErrorCode.DATA_CONFLICT, "이미 존재하는 카테고리입니다: " + normalizedCategory);
        }

        try {
            Files.createDirectories(categoryPath);
            log.info("카테고리 생성 완료 - path: {}", categoryPath);
        } catch (IOException e) {
            log.error("카테고리 생성 실패 - categoryName: {}, error: {}", categoryName, e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "카테고리 생성에 실패했습니다: " + e.getMessage());
        }

        return new ResourceFileDto.CategoryCreateResponse(
                normalizedCategory,
                "카테고리가 생성되었습니다."
        );
    }

    /**
     * 카테고리 삭제 (resources/file 하위의 폴더 삭제)
     *
     * <p>카테고리 내에 파일이 있으면 삭제 불가
     *
     * @param category 카테고리명
     * @return 삭제 결과 응답
     */
    public ResourceFileDto.CategoryDeleteResponse deleteCategory(String category) {
        log.info("카테고리 삭제 요청 - category: {}", category);

        String normalizedCategory = category.toLowerCase();
        Path categoryPath = Paths.get(baseReleasePath, RESOURCES_DIR, normalizedCategory);

        if (!Files.exists(categoryPath) || !Files.isDirectory(categoryPath)) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND, "카테고리를 찾을 수 없습니다: " + category);
        }

        // 폴더 내 파일 존재 여부 확인
        try (Stream<Path> stream = Files.walk(categoryPath)) {
            boolean hasFiles = stream.anyMatch(Files::isRegularFile);
            if (hasFiles) {
                throw new BusinessException(ErrorCode.DATA_CONFLICT,
                        "카테고리 내에 파일이 존재하여 삭제할 수 없습니다. 먼저 파일을 삭제해주세요.");
            }
        } catch (IOException e) {
            log.error("카테고리 파일 확인 실패 - category: {}, error: {}", category, e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "카테고리 확인에 실패했습니다");
        }

        // 빈 폴더 삭제 (하위 빈 디렉토리 포함)
        try (Stream<Path> walk = Files.walk(categoryPath)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            log.warn("디렉토리 삭제 실패: {}", path, e);
                        }
                    });
            log.info("카테고리 삭제 완료 - path: {}", categoryPath);
        } catch (IOException e) {
            log.error("카테고리 삭제 실패 - category: {}, error: {}", category, e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_DELETE_FAILED, "카테고리 삭제에 실패했습니다");
        }

        return new ResourceFileDto.CategoryDeleteResponse(
                normalizedCategory,
                "카테고리가 삭제되었습니다."
        );
    }

    /**
     * 카테고리별 파일 트리 조회 (파일시스템 기반)
     *
     * @param category 카테고리명
     * @return 파일 트리 응답
     */
    public ResourceFileDto.FilesResponse getFiles(String category) {
        log.info("파일 트리 조회 요청 - category: {}", category);

        String normalizedCategory = category.toLowerCase();
        Path categoryPath = Paths.get(baseReleasePath, RESOURCES_DIR, normalizedCategory);

        // 디렉토리가 존재하지 않는 경우 빈 응답 반환
        if (!Files.exists(categoryPath) || !Files.isDirectory(categoryPath)) {
            log.info("카테고리 디렉토리 미존재 - path: {}", categoryPath);
            return new ResourceFileDto.FilesResponse(
                    normalizedCategory,
                    false,
                    0,
                    0L,
                    ResourceFileDto.FileNode.directory("root", "/", RESOURCES_DIR + "/" + normalizedCategory, List.of())
            );
        }

        // 파일 트리 빌드
        FileTreeResult result = buildFileTree(categoryPath, normalizedCategory);

        log.info("파일 트리 조회 완료 - category: {}, fileCount: {}, totalSize: {}",
                normalizedCategory, result.fileCount, result.totalSize);

        return new ResourceFileDto.FilesResponse(
                normalizedCategory,
                result.fileCount > 0,
                result.fileCount,
                result.totalSize,
                result.rootNode
        );
    }

    /**
     * 디렉토리 생성
     *
     * @param category 카테고리명
     * @param path     생성할 디렉토리 경로 (예: /mariadb/scripts)
     * @return 생성 결과 응답
     */
    public ResourceFileDto.DirectoryResponse createDirectory(String category, String path) {
        log.info("디렉토리 생성 요청 - category: {}, path: {}", category, path);

        String normalizedCategory = category.toLowerCase();
        String normalizedPath = path.replaceAll("^/+|/+$", "");
        if (normalizedPath.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "디렉토리 경로가 비어있습니다");
        }

        Path fullPath = Paths.get(baseReleasePath, RESOURCES_DIR, normalizedCategory, normalizedPath);

        try {
            if (Files.exists(fullPath)) {
                throw new BusinessException(ErrorCode.DATA_CONFLICT, "이미 존재하는 경로입니다: " + path);
            }
            Files.createDirectories(fullPath);
            log.info("디렉토리 생성 완료 - path: {}", fullPath);

        } catch (IOException e) {
            log.error("디렉토리 생성 실패 - category: {}, error: {}", category, e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "디렉토리 생성에 실패했습니다: " + e.getMessage());
        }

        return new ResourceFileDto.DirectoryResponse(
                normalizedCategory,
                "/" + normalizedPath,
                "디렉토리가 생성되었습니다."
        );
    }

    /**
     * 파일 업로드 (ZIP 또는 단일 파일)
     *
     * @param category   카테고리명
     * @param file       업로드할 파일
     * @param targetPath 대상 경로 (null이면 루트에 저장)
     * @param extractZip ZIP 파일 압축 해제 여부 (true: 압축 해제, false: 원본 유지)
     * @return 업로드 결과 응답
     */
    public ResourceFileDto.UploadResponse uploadFile(
            String category, MultipartFile file, String targetPath, boolean extractZip) {

        log.info("파일 업로드 요청 - category: {}, fileName: {}, targetPath: {}, extractZip: {}",
                category, file.getOriginalFilename(), targetPath, extractZip);

        String normalizedCategory = category.toLowerCase();
        Path categoryBasePath = Paths.get(baseReleasePath, RESOURCES_DIR, normalizedCategory);

        // 대상 경로 설정
        Path uploadTargetPath = categoryBasePath;
        String targetSubPath = "";
        if (targetPath != null && !targetPath.isBlank()) {
            String normalizedPath = targetPath.replaceAll("^/+|/+$", "");
            if (!normalizedPath.isEmpty()) {
                uploadTargetPath = categoryBasePath.resolve(normalizedPath);
                targetSubPath = normalizedPath;
            }
        }

        List<ResourceFileDto.UploadedFileInfo> uploadedFiles = new ArrayList<>();

        try {
            Files.createDirectories(uploadTargetPath);

            String originalFileName = file.getOriginalFilename();
            if (originalFileName == null) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "파일명이 없습니다");
            }

            // ZIP 파일이고 extractZip이 true인 경우 압축 해제
            if (extractZip && originalFileName.toLowerCase().endsWith(".zip")) {
                uploadedFiles = extractZipFile(file, uploadTargetPath, targetSubPath);
            } else {
                // 단일 파일 저장
                Path targetFilePath = uploadTargetPath.resolve(originalFileName);
                Files.copy(file.getInputStream(), targetFilePath, StandardCopyOption.REPLACE_EXISTING);

                long fileSize = Files.size(targetFilePath);
                String relativePath = "/" + targetSubPath + (targetSubPath.isEmpty() ? "" : "/") + originalFileName;
                uploadedFiles.add(new ResourceFileDto.UploadedFileInfo(
                        originalFileName,
                        relativePath,
                        fileSize
                ));
            }

            log.info("파일 업로드 완료 - category: {}, uploadedCount: {}", category, uploadedFiles.size());

            return new ResourceFileDto.UploadResponse(
                    normalizedCategory,
                    uploadedFiles.size(),
                    uploadedFiles,
                    uploadedFiles.size() + "개 파일이 업로드되었습니다."
            );

        } catch (IOException e) {
            log.error("파일 업로드 실패 - category: {}, error: {}", category, e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "파일 업로드에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 파일 삭제 (파일 또는 디렉토리)
     *
     * @param category 카테고리명
     * @param filePath 파일 경로 (예: /mariadb/backup.sh)
     * @return 삭제 결과 응답
     */
    public ResourceFileDto.DeleteResponse deleteFile(String category, String filePath) {
        log.info("파일 삭제 요청 - category: {}, filePath: {}", category, filePath);

        String normalizedCategory = category.toLowerCase();
        String normalizedPath = filePath.replaceAll("^/+", "");
        Path fullPath = Paths.get(baseReleasePath, RESOURCES_DIR, normalizedCategory, normalizedPath);

        if (!Files.exists(fullPath)) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND, "파일/디렉토리를 찾을 수 없습니다: " + filePath);
        }

        try {
            if (Files.isDirectory(fullPath)) {
                // 디렉토리인 경우 내부 파일까지 모두 삭제
                try (Stream<Path> walk = Files.walk(fullPath)) {
                    walk.sorted(Comparator.reverseOrder())
                            .forEach(path -> {
                                try {
                                    Files.delete(path);
                                } catch (IOException e) {
                                    log.warn("파일 삭제 실패: {}", path, e);
                                }
                            });
                }
                log.info("디렉토리 삭제 완료 - path: {}", fullPath);
            } else {
                Files.delete(fullPath);
                log.info("파일 삭제 완료 - path: {}", fullPath);
            }

            return new ResourceFileDto.DeleteResponse(
                    normalizedCategory,
                    "/" + normalizedPath,
                    "삭제되었습니다."
            );

        } catch (IOException e) {
            log.error("파일 삭제 실패 - category: {}, error: {}", category, e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_DELETE_FAILED, "파일 삭제에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 카테고리 전체 파일 ZIP 다운로드
     *
     * @param category     카테고리명
     * @param outputStream 출력 스트림
     */
    public void downloadAllFiles(String category, OutputStream outputStream) {
        log.info("전체 파일 다운로드 요청 - category: {}", category);

        String normalizedCategory = category.toLowerCase();
        Path categoryPath = Paths.get(baseReleasePath, RESOURCES_DIR, normalizedCategory);

        if (!Files.exists(categoryPath) || !Files.isDirectory(categoryPath)) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND, "파일이 없습니다");
        }

        try {
            List<StreamingZipUtil.ZipFileEntry> fileEntries = new ArrayList<>();
            try (Stream<Path> stream = Files.walk(categoryPath)) {
                stream.filter(Files::isRegularFile)
                        .forEach(path -> {
                            String relativePath = categoryPath.relativize(path).toString().replace("\\", "/");
                            fileEntries.add(new StreamingZipUtil.ZipFileEntry(path, relativePath));
                        });
            }

            if (fileEntries.isEmpty()) {
                throw new BusinessException(ErrorCode.FILE_NOT_FOUND, "다운로드할 파일이 없습니다");
            }

            StreamingZipUtil.compressFilesToStream(outputStream, fileEntries);
            log.info("전체 파일 다운로드 완료 - category: {}, fileCount: {}", category, fileEntries.size());

        } catch (IOException e) {
            log.error("전체 파일 다운로드 실패 - category: {}, error: {}", category, e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_DOWNLOAD_FAILED, "파일 다운로드에 실패했습니다");
        }
    }

    /**
     * 카테고리 전체 파일 크기 조회 (압축 전)
     */
    public long getTotalSize(String category) {
        String normalizedCategory = category.toLowerCase();
        Path categoryPath = Paths.get(baseReleasePath, RESOURCES_DIR, normalizedCategory);

        if (!Files.exists(categoryPath)) {
            return 0L;
        }

        try (Stream<Path> stream = Files.walk(categoryPath)) {
            return stream.filter(Files::isRegularFile)
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            return 0L;
                        }
                    })
                    .sum();
        } catch (IOException e) {
            return 0L;
        }
    }

    // ========================================
    // Private Helper Methods
    // ========================================

    private CategoryStats calculateCategoryStats(Path categoryPath) {
        int[] fileCount = {0};
        long[] totalSize = {0L};

        try (Stream<Path> stream = Files.walk(categoryPath)) {
            stream.filter(Files::isRegularFile)
                    .forEach(path -> {
                        fileCount[0]++;
                        try {
                            totalSize[0] += Files.size(path);
                        } catch (IOException e) {
                            // ignore
                        }
                    });
        } catch (IOException e) {
            log.warn("카테고리 통계 계산 실패: {}", categoryPath, e);
        }

        return new CategoryStats(fileCount[0], totalSize[0]);
    }

    private FileTreeResult buildFileTree(Path rootPath, String category) {
        int[] fileCount = {0};
        long[] totalSize = {0L};

        String resourceRelativeBase = RESOURCES_DIR + "/" + category;
        ResourceFileDto.FileNode rootNode = buildDirectoryNode(rootPath, rootPath, resourceRelativeBase, fileCount, totalSize);

        return new FileTreeResult(rootNode, fileCount[0], totalSize[0]);
    }

    private ResourceFileDto.FileNode buildDirectoryNode(Path currentPath, Path rootPath,
            String resourceRelativeBase, int[] fileCount, long[] totalSize) {
        List<ResourceFileDto.FileNode> children = new ArrayList<>();

        try (Stream<Path> stream = Files.list(currentPath)) {
            List<Path> sortedPaths = stream
                    .sorted(Comparator
                            .comparing((Path p) -> Files.isDirectory(p) ? 0 : 1)
                            .thenComparing(p -> p.getFileName().toString().toLowerCase()))
                    .toList();

            for (Path path : sortedPaths) {
                String relativePath = "/" + rootPath.relativize(path).toString().replace("\\", "/");
                String name = path.getFileName().toString();
                String filePathStr = resourceRelativeBase + "/" + rootPath.relativize(path).toString().replace("\\", "/");

                if (Files.isDirectory(path)) {
                    ResourceFileDto.FileNode dirNode = buildDirectoryNode(path, rootPath, resourceRelativeBase, fileCount, totalSize);
                    children.add(dirNode);
                } else {
                    try {
                        long size = Files.size(path);
                        LocalDateTime modifiedAt = getLastModifiedTime(path);
                        String mimeType = FileContentUtil.getMimeType(path);
                        fileCount[0]++;
                        totalSize[0] += size;
                        children.add(ResourceFileDto.FileNode.file(name, relativePath, filePathStr, size, modifiedAt, mimeType));
                    } catch (IOException e) {
                        log.warn("파일 정보 조회 실패: {}", path, e);
                        children.add(ResourceFileDto.FileNode.file(name, relativePath, filePathStr, 0L, null, null));
                    }
                }
            }
        } catch (IOException e) {
            log.error("디렉토리 읽기 실패: {}", currentPath, e);
        }

        String relativePath = currentPath.equals(rootPath)
                ? "/"
                : "/" + rootPath.relativize(currentPath).toString().replace("\\", "/");
        String name = currentPath.equals(rootPath)
                ? "root"
                : currentPath.getFileName().toString();
        String filePathStr = currentPath.equals(rootPath)
                ? resourceRelativeBase
                : resourceRelativeBase + "/" + rootPath.relativize(currentPath).toString().replace("\\", "/");

        return ResourceFileDto.FileNode.directory(name, relativePath, filePathStr, children);
    }

    /**
     * 파일의 수정 날짜 조회
     */
    private LocalDateTime getLastModifiedTime(Path path) throws IOException {
        FileTime fileTime = Files.getLastModifiedTime(path);
        return LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.systemDefault());
    }

    private List<ResourceFileDto.UploadedFileInfo> extractZipFile(
            MultipartFile zipFile, Path targetDir, String targetSubPath) throws IOException {

        List<ResourceFileDto.UploadedFileInfo> uploadedFiles = new ArrayList<>();

        ZipExtractUtil.extractWithCallback(zipFile.getInputStream(), targetDir, (entry, filePath) -> {
            try {
                String fileRelativePath = targetSubPath.isEmpty()
                        ? entry.getName()
                        : targetSubPath + "/" + entry.getName();

                long fileSize = Files.size(filePath);
                uploadedFiles.add(new ResourceFileDto.UploadedFileInfo(
                        filePath.getFileName().toString(),
                        "/" + fileRelativePath,
                        fileSize
                ));
            } catch (IOException e) {
                log.warn("파일 크기 조회 실패: {}", filePath, e);
            }
        });

        return uploadedFiles;
    }

    private record CategoryStats(int fileCount, long totalSize) {
    }

    private record FileTreeResult(
            ResourceFileDto.FileNode rootNode,
            int fileCount,
            long totalSize
    ) {
    }
}
