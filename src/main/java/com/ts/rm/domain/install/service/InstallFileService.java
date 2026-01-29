package com.ts.rm.domain.install.service;

import com.ts.rm.domain.install.dto.InstallFileDto;
import com.ts.rm.domain.project.entity.Project;
import com.ts.rm.domain.project.repository.ProjectRepository;
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
 * Install File Service
 *
 * <p>프로젝트별 인스톨 파일 관리 비즈니스 로직 (파일시스템 기반)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InstallFileService {

    private final ProjectRepository projectRepository;

    @Value("${app.release.base-path:data/release-manager}")
    private String baseReleasePath;

    private static final String INSTALLS_DIR = "installs";

    /**
     * 프로젝트별 인스톨 파일 트리 조회 (파일시스템 기반)
     *
     * @param projectId 프로젝트 ID
     * @return 인스톨 파일 트리 응답
     */
    public InstallFileDto.FilesResponse getInstallFiles(String projectId) {
        log.info("인스톨 파일 조회 요청 - projectId: {}", projectId);

        Project project = findProjectById(projectId);

        Path installPath = Paths.get(baseReleasePath, INSTALLS_DIR, projectId);

        // 디렉토리가 존재하지 않는 경우 빈 응답 반환
        if (!Files.exists(installPath) || !Files.isDirectory(installPath)) {
            log.info("인스톨 디렉토리 미존재 - path: {}", installPath);
            return new InstallFileDto.FilesResponse(
                    projectId,
                    project.getProjectName(),
                    false,
                    0,
                    0L,
                    InstallFileDto.FileNode.directory("root", "/", INSTALLS_DIR + "/" + projectId, List.of())
            );
        }

        // 파일 트리 빌드
        FileTreeResult result = buildInstallFileTree(installPath, projectId);

        log.info("인스톨 파일 조회 완료 - projectId: {}, fileCount: {}, totalSize: {}",
                projectId, result.fileCount, result.totalSize);

        return new InstallFileDto.FilesResponse(
                projectId,
                project.getProjectName(),
                result.fileCount > 0,
                result.fileCount,
                result.totalSize,
                result.rootNode
        );
    }

    /**
     * 인스톨 디렉토리 생성
     *
     * @param projectId 프로젝트 ID
     * @param path      생성할 디렉토리 경로 (예: /mariadb/scripts)
     * @return 생성 결과 응답
     */
    public InstallFileDto.DirectoryResponse createDirectory(String projectId, String path) {
        log.info("인스톨 디렉토리 생성 요청 - projectId: {}, path: {}", projectId, path);

        findProjectById(projectId);

        String normalizedPath = path.replaceAll("^/+|/+$", "");
        if (normalizedPath.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "디렉토리 경로가 비어있습니다");
        }

        Path fullPath = Paths.get(baseReleasePath, INSTALLS_DIR, projectId, normalizedPath);

        try {
            if (Files.exists(fullPath)) {
                throw new BusinessException(ErrorCode.DATA_CONFLICT, "이미 존재하는 경로입니다: " + path);
            }
            Files.createDirectories(fullPath);
            log.info("인스톨 디렉토리 생성 완료 - path: {}", fullPath);

        } catch (IOException e) {
            log.error("인스톨 디렉토리 생성 실패 - projectId: {}, error: {}", projectId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "디렉토리 생성에 실패했습니다: " + e.getMessage());
        }

        return new InstallFileDto.DirectoryResponse(
                projectId,
                "/" + normalizedPath,
                "디렉토리가 생성되었습니다."
        );
    }

    /**
     * 인스톨 파일 업로드 (ZIP 또는 단일 파일)
     *
     * @param projectId  프로젝트 ID
     * @param file       업로드할 파일
     * @param targetPath 대상 경로 (null이면 루트에 저장)
     * @param extractZip ZIP 파일 압축 해제 여부 (true: 압축 해제, false: 원본 유지)
     * @return 업로드 결과 응답
     */
    public InstallFileDto.UploadResponse uploadFile(
            String projectId, MultipartFile file, String targetPath, boolean extractZip) {

        log.info("인스톨 파일 업로드 요청 - projectId: {}, fileName: {}, targetPath: {}, extractZip: {}",
                projectId, file.getOriginalFilename(), targetPath, extractZip);

        findProjectById(projectId);

        Path installBasePath = Paths.get(baseReleasePath, INSTALLS_DIR, projectId);

        // 대상 경로 설정
        Path uploadTargetPath = installBasePath;
        String targetSubPath = "";
        if (targetPath != null && !targetPath.isBlank()) {
            String normalizedPath = targetPath.replaceAll("^/+|/+$", "");
            if (!normalizedPath.isEmpty()) {
                uploadTargetPath = installBasePath.resolve(normalizedPath);
                targetSubPath = normalizedPath;
            }
        }

        List<InstallFileDto.UploadedFileInfo> uploadedFiles = new ArrayList<>();

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
                uploadedFiles.add(new InstallFileDto.UploadedFileInfo(
                        originalFileName,
                        relativePath,
                        fileSize
                ));
            }

            log.info("인스톨 파일 업로드 완료 - projectId: {}, uploadedCount: {}", projectId, uploadedFiles.size());

            return new InstallFileDto.UploadResponse(
                    projectId,
                    uploadedFiles.size(),
                    uploadedFiles,
                    uploadedFiles.size() + "개 파일이 업로드되었습니다."
            );

        } catch (IOException e) {
            log.error("인스톨 파일 업로드 실패 - projectId: {}, error: {}", projectId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "파일 업로드에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 인스톨 파일 삭제 (파일 또는 디렉토리)
     *
     * @param projectId 프로젝트 ID
     * @param filePath  파일 경로 (예: /mariadb/init.sql)
     * @return 삭제 결과 응답
     */
    public InstallFileDto.DeleteResponse deleteFile(String projectId, String filePath) {
        log.info("인스톨 파일 삭제 요청 - projectId: {}, filePath: {}", projectId, filePath);

        findProjectById(projectId);

        String normalizedPath = filePath.replaceAll("^/+", "");
        Path fullPath = Paths.get(baseReleasePath, INSTALLS_DIR, projectId, normalizedPath);

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
                log.info("인스톨 디렉토리 삭제 완료 - path: {}", fullPath);
            } else {
                Files.delete(fullPath);
                log.info("인스톨 파일 삭제 완료 - path: {}", fullPath);
            }

            return new InstallFileDto.DeleteResponse(
                    projectId,
                    "/" + normalizedPath,
                    "삭제되었습니다."
            );

        } catch (IOException e) {
            log.error("인스톨 파일 삭제 실패 - projectId: {}, error: {}", projectId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_DELETE_FAILED, "파일 삭제에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 인스톨 전체 파일 ZIP 다운로드
     *
     * @param projectId    프로젝트 ID
     * @param outputStream 출력 스트림
     */
    public void downloadAllFiles(String projectId, OutputStream outputStream) {
        log.info("인스톨 전체 파일 다운로드 요청 - projectId: {}", projectId);

        findProjectById(projectId);

        Path installPath = Paths.get(baseReleasePath, INSTALLS_DIR, projectId);

        if (!Files.exists(installPath) || !Files.isDirectory(installPath)) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND, "인스톨 파일이 없습니다");
        }

        try {
            List<StreamingZipUtil.ZipFileEntry> fileEntries = new ArrayList<>();
            try (Stream<Path> stream = Files.walk(installPath)) {
                stream.filter(Files::isRegularFile)
                        .forEach(path -> {
                            String relativePath = installPath.relativize(path).toString().replace("\\", "/");
                            fileEntries.add(new StreamingZipUtil.ZipFileEntry(path, relativePath));
                        });
            }

            if (fileEntries.isEmpty()) {
                throw new BusinessException(ErrorCode.FILE_NOT_FOUND, "다운로드할 파일이 없습니다");
            }

            StreamingZipUtil.compressFilesToStream(outputStream, fileEntries);
            log.info("인스톨 전체 파일 다운로드 완료 - projectId: {}, fileCount: {}", projectId, fileEntries.size());

        } catch (IOException e) {
            log.error("인스톨 전체 파일 다운로드 실패 - projectId: {}, error: {}", projectId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_DOWNLOAD_FAILED, "파일 다운로드에 실패했습니다");
        }
    }

    /**
     * 인스톨 전체 파일 크기 조회 (압축 전)
     */
    public long getTotalSize(String projectId) {
        Path installPath = Paths.get(baseReleasePath, INSTALLS_DIR, projectId);

        if (!Files.exists(installPath)) {
            return 0L;
        }

        try (Stream<Path> stream = Files.walk(installPath)) {
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

    private Project findProjectById(String projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
    }

    private FileTreeResult buildInstallFileTree(Path rootPath, String projectId) {
        int[] fileCount = {0};
        long[] totalSize = {0L};

        String installRelativeBase = INSTALLS_DIR + "/" + projectId;
        InstallFileDto.FileNode rootNode = buildDirectoryNode(rootPath, rootPath, installRelativeBase, fileCount, totalSize);

        return new FileTreeResult(rootNode, fileCount[0], totalSize[0]);
    }

    private InstallFileDto.FileNode buildDirectoryNode(Path currentPath, Path rootPath,
            String installRelativeBase, int[] fileCount, long[] totalSize) {
        List<InstallFileDto.FileNode> children = new ArrayList<>();

        try (Stream<Path> stream = Files.list(currentPath)) {
            List<Path> sortedPaths = stream
                    .sorted(Comparator
                            .comparing((Path p) -> Files.isDirectory(p) ? 0 : 1)
                            .thenComparing(p -> p.getFileName().toString().toLowerCase()))
                    .toList();

            for (Path path : sortedPaths) {
                String relativePath = "/" + rootPath.relativize(path).toString().replace("\\", "/");
                String name = path.getFileName().toString();
                String filePathStr = installRelativeBase + "/" + rootPath.relativize(path).toString().replace("\\", "/");

                if (Files.isDirectory(path)) {
                    InstallFileDto.FileNode dirNode = buildDirectoryNode(path, rootPath, installRelativeBase, fileCount, totalSize);
                    children.add(dirNode);
                } else {
                    try {
                        long size = Files.size(path);
                        LocalDateTime modifiedAt = getLastModifiedTime(path);
                        String mimeType = FileContentUtil.getMimeType(path);
                        fileCount[0]++;
                        totalSize[0] += size;
                        children.add(InstallFileDto.FileNode.file(name, relativePath, filePathStr, size, modifiedAt, mimeType));
                    } catch (IOException e) {
                        log.warn("파일 정보 조회 실패: {}", path, e);
                        children.add(InstallFileDto.FileNode.file(name, relativePath, filePathStr, 0L, null, null));
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
                ? installRelativeBase
                : installRelativeBase + "/" + rootPath.relativize(currentPath).toString().replace("\\", "/");

        return InstallFileDto.FileNode.directory(name, relativePath, filePathStr, children);
    }

    /**
     * 파일의 수정 날짜 조회
     */
    private LocalDateTime getLastModifiedTime(Path path) throws IOException {
        FileTime fileTime = Files.getLastModifiedTime(path);
        return LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.systemDefault());
    }

    private List<InstallFileDto.UploadedFileInfo> extractZipFile(
            MultipartFile zipFile, Path targetDir, String targetSubPath) throws IOException {

        List<InstallFileDto.UploadedFileInfo> uploadedFiles = new ArrayList<>();

        ZipExtractUtil.extractWithCallback(zipFile.getInputStream(), targetDir, (entry, filePath) -> {
            try {
                String fileRelativePath = targetSubPath.isEmpty()
                        ? entry.getName()
                        : targetSubPath + "/" + entry.getName();

                long fileSize = Files.size(filePath);
                uploadedFiles.add(new InstallFileDto.UploadedFileInfo(
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

    private record FileTreeResult(
            InstallFileDto.FileNode rootNode,
            int fileCount,
            long totalSize
    ) {
    }
}
