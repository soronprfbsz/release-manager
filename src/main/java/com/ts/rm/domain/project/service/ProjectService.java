package com.ts.rm.domain.project.service;

import com.ts.rm.domain.account.entity.Account;
import com.ts.rm.domain.project.dto.ProjectDto;
import com.ts.rm.domain.project.entity.OnboardingFile;
import com.ts.rm.domain.project.entity.Project;
import com.ts.rm.domain.project.mapper.ProjectDtoMapper;
import com.ts.rm.domain.project.repository.OnboardingFileRepository;
import com.ts.rm.domain.project.repository.ProjectRepository;
import com.ts.rm.global.account.AccountLookupService;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import com.ts.rm.global.file.FileChecksumUtil;
import com.ts.rm.global.file.StreamingZipUtil;
import com.ts.rm.global.security.SecurityUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Project Service
 *
 * <p>프로젝트 관리 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final OnboardingFileRepository onboardingFileRepository;
    private final AccountLookupService accountLookupService;
    private final ProjectDtoMapper mapper;

    @Value("${app.release.base-path:data/release-manager}")
    private String baseReleasePath;

    /**
     * 프로젝트 생성
     *
     * @param request 프로젝트 생성 요청
     * @return 생성된 프로젝트 상세 정보
     */
    @Transactional
    public ProjectDto.DetailResponse createProject(ProjectDto.CreateRequest request) {
        log.info("Creating project with id: {}", request.projectId());

        // 중복 검증
        if (projectRepository.existsByProjectId(request.projectId())) {
            throw new BusinessException(ErrorCode.PROJECT_ID_CONFLICT);
        }

        Project project = mapper.toEntity(request);

        // isEnabled 기본값 처리 (null이면 true)
        project.setIsEnabled(request.isEnabled() != null ? request.isEnabled() : true);

        Project savedProject = projectRepository.save(project);

        log.info("Project created successfully with id: {}", savedProject.getProjectId());
        return mapper.toDetailResponse(savedProject);
    }

    /**
     * 프로젝트 조회 (ID)
     *
     * @param projectId 프로젝트 ID
     * @return 프로젝트 상세 정보
     */
    public ProjectDto.DetailResponse getProjectById(String projectId) {
        Project project = findProjectById(projectId);
        return mapper.toDetailResponse(project);
    }

    /**
     * 프로젝트 목록 조회
     *
     * @param isEnabled 활성 여부 필터 (null이면 전체 조회)
     * @return 프로젝트 목록
     */
    public List<ProjectDto.DetailResponse> getAllProjects(Boolean isEnabled) {
        List<Project> projects;
        if (isEnabled != null) {
            projects = projectRepository.findAllByIsEnabledOrderByProjectNameAsc(isEnabled);
        } else {
            projects = projectRepository.findAllByOrderByProjectNameAsc();
        }
        return mapper.toDetailResponseList(projects);
    }

    /**
     * 프로젝트 정보 수정
     *
     * @param projectId 프로젝트 ID
     * @param request   수정 요청
     * @return 수정된 프로젝트 상세 정보
     */
    @Transactional
    public ProjectDto.DetailResponse updateProject(String projectId,
            ProjectDto.UpdateRequest request) {
        log.info("Updating project with id: {}", projectId);

        // 엔티티 조회
        Project project = findProjectById(projectId);

        // Setter를 통한 수정 (JPA Dirty Checking)
        if (request.projectName() != null) {
            project.setProjectName(request.projectName());
        }
        if (request.description() != null) {
            project.setDescription(request.description());
        }
        if (request.isEnabled() != null) {
            project.setIsEnabled(request.isEnabled());
        }

        // 트랜잭션 커밋 시 자동으로 UPDATE 쿼리 실행 (Dirty Checking)
        log.info("Project updated successfully with id: {}", projectId);
        return mapper.toDetailResponse(project);
    }

    /**
     * 프로젝트 삭제
     *
     * @param projectId 프로젝트 ID
     */
    @Transactional
    public void deleteProject(String projectId) {
        log.info("Deleting project with id: {}", projectId);

        // 프로젝트 존재 검증
        Project project = findProjectById(projectId);
        projectRepository.delete(project);

        log.info("Project deleted successfully with id: {}", projectId);
    }

    /**
     * 프로젝트 존재 여부 확인
     *
     * @param projectId 프로젝트 ID
     * @return 존재 여부
     */
    public boolean existsById(String projectId) {
        return projectRepository.existsByProjectId(projectId);
    }

    // === Onboarding File Methods ===

    /**
     * 프로젝트별 온보딩 파일 트리 조회 (파일시스템 기반)
     *
     * @param projectId 프로젝트 ID
     * @return 온보딩 파일 트리 응답
     */
    public ProjectDto.OnboardingFilesResponse getOnboardingFiles(String projectId) {
        log.info("온보딩 파일 조회 요청 - projectId: {}", projectId);

        // 프로젝트 조회 및 검증
        Project project = findProjectById(projectId);

        // 온보딩 파일 경로: {baseReleasePath}/onboardings/{projectId}
        Path onboardingPath = Paths.get(baseReleasePath, "onboardings", projectId);

        // 디렉토리가 존재하지 않는 경우 빈 응답 반환
        if (!Files.exists(onboardingPath) || !Files.isDirectory(onboardingPath)) {
            log.info("온보딩 디렉토리 미존재 - path: {}", onboardingPath);
            return new ProjectDto.OnboardingFilesResponse(
                    projectId,
                    project.getProjectName(),
                    false,
                    0,
                    0L,
                    ProjectDto.OnboardingFileNode.directory("root", "/", "onboardings/" + projectId, List.of())
            );
        }

        // 파일 트리 빌드
        FileTreeResult result = buildOnboardingFileTree(onboardingPath, projectId);

        log.info("온보딩 파일 조회 완료 - projectId: {}, fileCount: {}, totalSize: {}",
                projectId, result.fileCount, result.totalSize);

        return new ProjectDto.OnboardingFilesResponse(
                projectId,
                project.getProjectName(),
                result.fileCount > 0,
                result.fileCount,
                result.totalSize,
                result.rootNode
        );
    }

    /**
     * 프로젝트별 온보딩 파일 목록 조회 (DB 기반)
     *
     * @param projectId 프로젝트 ID
     * @return 온보딩 파일 목록 응답
     */
    public ProjectDto.OnboardingFileListResponse getOnboardingFileList(String projectId) {
        log.info("온보딩 파일 목록 조회 요청 (DB 기반) - projectId: {}", projectId);

        // 프로젝트 조회 및 검증
        Project project = findProjectById(projectId);

        // DB에서 파일 목록 조회
        List<OnboardingFile> onboardingFiles =
                onboardingFileRepository.findAllByProject_ProjectIdOrderBySortOrderAscCreatedAtDesc(projectId);

        // 총 파일 크기 계산
        Long totalSize = onboardingFileRepository.sumFileSizeByProjectId(projectId);

        // DTO 변환
        List<ProjectDto.OnboardingFileDetailResponse> fileResponses = onboardingFiles.stream()
                .map(this::toOnboardingFileDetailResponse)
                .toList();

        log.info("온보딩 파일 목록 조회 완료 - projectId: {}, fileCount: {}, totalSize: {}",
                projectId, fileResponses.size(), totalSize);

        return new ProjectDto.OnboardingFileListResponse(
                projectId,
                project.getProjectName(),
                fileResponses.size(),
                totalSize != null ? totalSize : 0L,
                fileResponses
        );
    }

    /**
     * OnboardingFile 엔티티를 DTO로 변환
     */
    private ProjectDto.OnboardingFileDetailResponse toOnboardingFileDetailResponse(OnboardingFile file) {
        return new ProjectDto.OnboardingFileDetailResponse(
                file.getOnboardingFileId(),
                file.getProject().getProjectId(),
                file.getFileType(),
                file.getFileCategory(),
                file.getFileName(),
                file.getFilePath(),
                file.getFileSize(),
                file.getChecksum(),
                file.getDescription(),
                file.getSortOrder(),
                file.getCreatedByEmail(),
                file.getCreatedByName(),
                file.getCreatedByAvatarStyle(),
                file.getCreatedByAvatarSeed(),
                file.isDeletedCreator(),
                file.getCreatedAt()
        );
    }

    /**
     * 온보딩 파일 트리 빌드
     */
    private FileTreeResult buildOnboardingFileTree(Path rootPath, String projectId) {
        int[] fileCount = {0};
        long[] totalSize = {0L};

        String onboardingRelativeBase = "onboardings/" + projectId;
        ProjectDto.OnboardingFileNode rootNode = buildDirectoryNode(rootPath, rootPath, onboardingRelativeBase, fileCount, totalSize);

        return new FileTreeResult(rootNode, fileCount[0], totalSize[0]);
    }

    /**
     * 디렉토리 노드 재귀 빌드
     */
    private ProjectDto.OnboardingFileNode buildDirectoryNode(Path currentPath, Path rootPath,
            String onboardingRelativeBase, int[] fileCount, long[] totalSize) {
        List<ProjectDto.OnboardingFileNode> children = new ArrayList<>();

        try (Stream<Path> stream = Files.list(currentPath)) {
            List<Path> sortedPaths = stream
                    .sorted(Comparator
                            .comparing((Path p) -> Files.isDirectory(p) ? 0 : 1)  // 디렉토리 우선
                            .thenComparing(p -> p.getFileName().toString().toLowerCase()))  // 이름순
                    .toList();

            for (Path path : sortedPaths) {
                String relativePath = "/" + rootPath.relativize(path).toString().replace("\\", "/");
                String name = path.getFileName().toString();

                // filePath 생성 (onboardings/{projectId}/ 포함)
                String filePathStr = onboardingRelativeBase + "/" + rootPath.relativize(path).toString().replace("\\", "/");

                if (Files.isDirectory(path)) {
                    // 디렉토리인 경우 재귀 호출
                    ProjectDto.OnboardingFileNode dirNode = buildDirectoryNode(path, rootPath, onboardingRelativeBase, fileCount, totalSize);
                    children.add(dirNode);
                } else {
                    // 파일인 경우
                    try {
                        long size = Files.size(path);
                        fileCount[0]++;
                        totalSize[0] += size;
                        children.add(ProjectDto.OnboardingFileNode.file(name, relativePath, filePathStr, size));
                    } catch (IOException e) {
                        log.warn("파일 크기 조회 실패: {}", path, e);
                        children.add(ProjectDto.OnboardingFileNode.file(name, relativePath, filePathStr, 0L));
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
        
        // filePath 생성 (onboardings/{projectId}/ 포함)
        String filePathStr = currentPath.equals(rootPath)
                ? onboardingRelativeBase
                : onboardingRelativeBase + "/" + rootPath.relativize(currentPath).toString().replace("\\", "/");

        return ProjectDto.OnboardingFileNode.directory(name, relativePath, filePathStr, children);
    }

    /**
     * 파일 트리 빌드 결과
     */
    private record FileTreeResult(
            ProjectDto.OnboardingFileNode rootNode,
            int fileCount,
            long totalSize
    ) {
    }

    // === Onboarding File Upload/Download/Delete Methods ===

    /**
     * 온보딩 파일 업로드 (ZIP 또는 단일 파일) - DB 연동
     *
     * @param projectId   프로젝트 ID
     * @param file        업로드할 파일 (ZIP인 경우 압축 해제)
     * @param targetPath  대상 경로 (null이면 루트에 저장)
     * @param description 파일 설명 (선택)
     * @return 업로드 결과 응답
     */
    @Transactional
    public ProjectDto.OnboardingUploadResponse uploadOnboardingFile(
            String projectId, MultipartFile file, String targetPath, String description) {

        log.info("온보딩 파일 업로드 요청 - projectId: {}, fileName: {}, targetPath: {}",
                projectId, file.getOriginalFilename(), targetPath);

        // 프로젝트 조회 및 검증
        Project project = findProjectById(projectId);

        // 현재 로그인 사용자 정보 조회
        String currentEmail = SecurityUtil.getCurrentEmail();
        Account creator = accountLookupService.findByEmail(currentEmail);

        // 온보딩 기본 경로 (상대경로)
        String onboardingRelativeBase = "onboardings/" + projectId;
        Path onboardingBasePath = Paths.get(baseReleasePath, "onboardings", projectId);

        // 대상 경로 설정
        Path uploadTargetPath = onboardingBasePath;
        String targetSubPath = "";
        if (targetPath != null && !targetPath.isBlank()) {
            // 경로 정규화 (앞뒤 슬래시 제거)
            String normalizedPath = targetPath.replaceAll("^/+|/+$", "");
            if (!normalizedPath.isEmpty()) {
                uploadTargetPath = onboardingBasePath.resolve(normalizedPath);
                targetSubPath = normalizedPath;
            }
        }

        List<ProjectDto.UploadedFileInfo> uploadedFiles = new ArrayList<>();

        try {
            // 디렉토리 생성
            Files.createDirectories(uploadTargetPath);

            String originalFileName = file.getOriginalFilename();
            if (originalFileName == null) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "파일명이 없습니다");
            }

            // ZIP 파일인 경우 압축 해제
            if (originalFileName.toLowerCase().endsWith(".zip")) {
                uploadedFiles = extractZipFileWithDb(file, uploadTargetPath, onboardingRelativeBase,
                        targetSubPath, project, creator, currentEmail);
            } else {
                // 단일 파일 저장 및 DB 등록
                Path targetFilePath = uploadTargetPath.resolve(originalFileName);
                Files.copy(file.getInputStream(), targetFilePath, StandardCopyOption.REPLACE_EXISTING);

                // DB에 메타데이터 저장
                OnboardingFile savedFile = saveOnboardingFileMetadata(
                        project, targetFilePath, onboardingRelativeBase, targetSubPath,
                        originalFileName, description, creator, currentEmail);

                String relativePath = "/" + targetSubPath + (targetSubPath.isEmpty() ? "" : "/") + originalFileName;
                uploadedFiles.add(new ProjectDto.UploadedFileInfo(
                        originalFileName,
                        relativePath,
                        savedFile.getFileSize()
                ));
            }

            log.info("온보딩 파일 업로드 완료 - projectId: {}, uploadedCount: {}", projectId, uploadedFiles.size());

            return new ProjectDto.OnboardingUploadResponse(
                    projectId,
                    uploadedFiles.size(),
                    uploadedFiles,
                    uploadedFiles.size() + "개 파일이 업로드되었습니다."
            );

        } catch (IOException e) {
            log.error("온보딩 파일 업로드 실패 - projectId: {}, error: {}", projectId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "파일 업로드에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * ZIP 파일 압축 해제 및 DB 저장
     */
    private List<ProjectDto.UploadedFileInfo> extractZipFileWithDb(
            MultipartFile zipFile, Path targetDir, String onboardingRelativeBase,
            String targetSubPath, Project project, Account creator, String creatorEmail) throws IOException {

        List<ProjectDto.UploadedFileInfo> uploadedFiles = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();

                // 디렉토리인 경우 생성만
                if (entry.isDirectory()) {
                    Files.createDirectories(targetDir.resolve(entryName));
                    continue;
                }

                // 파일인 경우 저장
                Path filePath = targetDir.resolve(entryName);
                Files.createDirectories(filePath.getParent());
                Files.copy(zis, filePath, StandardCopyOption.REPLACE_EXISTING);

                // 상대 경로 계산 (targetSubPath를 기준으로)
                String fileRelativePath = targetSubPath.isEmpty()
                        ? entryName
                        : targetSubPath + "/" + entryName;

                // DB에 메타데이터 저장
                OnboardingFile savedFile = saveOnboardingFileMetadata(
                        project, filePath, onboardingRelativeBase, fileRelativePath.replace("/" + filePath.getFileName().toString(), ""),
                        filePath.getFileName().toString(), null, creator, creatorEmail);

                uploadedFiles.add(new ProjectDto.UploadedFileInfo(
                        filePath.getFileName().toString(),
                        "/" + fileRelativePath,
                        savedFile.getFileSize()
                ));

                zis.closeEntry();
            }
        }

        return uploadedFiles;
    }

    /**
     * 온보딩 파일 메타데이터 DB 저장
     */
    private OnboardingFile saveOnboardingFileMetadata(
            Project project, Path filePath, String onboardingRelativeBase,
            String subPath, String fileName, String description,
            Account creator, String creatorEmail) throws IOException {

        // 파일 타입 추출 (확장자 대문자)
        String fileType = extractFileType(fileName);

        // 파일 카테고리 추출 (subPath의 첫 번째 디렉토리 또는 ETC)
        String fileCategory = extractFileCategory(subPath);

        // 상대 경로 생성 (DB 저장용)
        String dbFilePath = onboardingRelativeBase + "/" +
                (subPath.isEmpty() ? fileName : subPath + "/" + fileName);

        // 중복 파일 검사
        if (onboardingFileRepository.existsByFilePath(dbFilePath)) {
            // 기존 파일 삭제 후 재등록 (덮어쓰기)
            onboardingFileRepository.findByFilePath(dbFilePath)
                    .ifPresent(onboardingFileRepository::delete);
            log.info("기존 온보딩 파일 덮어쓰기 - path: {}", dbFilePath);
        }

        // 파일 크기 및 체크섬
        long fileSize = Files.size(filePath);
        String checksum = FileChecksumUtil.calculateChecksum(filePath);

        // sortOrder 자동 채번
        Integer maxSortOrder = onboardingFileRepository.findMaxSortOrderByProjectId(project.getProjectId());
        Integer sortOrder = maxSortOrder + 1;

        // 엔티티 생성 및 저장
        OnboardingFile onboardingFile = OnboardingFile.builder()
                .project(project)
                .fileType(fileType)
                .fileCategory(fileCategory)
                .fileName(fileName)
                .filePath(dbFilePath)
                .fileSize(fileSize)
                .checksum(checksum)
                .description(description)
                .sortOrder(sortOrder)
                .creator(creator)
                .createdByEmail(creatorEmail)
                .build();

        OnboardingFile saved = onboardingFileRepository.save(onboardingFile);
        log.debug("온보딩 파일 DB 저장 완료 - id: {}, path: {}", saved.getOnboardingFileId(), dbFilePath);

        return saved;
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
     * 경로에서 파일 카테고리 추출
     */
    private String extractFileCategory(String subPath) {
        if (subPath == null || subPath.isBlank()) {
            return "ETC";
        }
        // 첫 번째 디렉토리를 카테고리로 사용
        String[] parts = subPath.split("/");
        if (parts.length > 0 && !parts[0].isBlank()) {
            return parts[0].toUpperCase();
        }
        return "ETC";
    }

    /**
     * 온보딩 단일 파일 다운로드
     *
     * @param projectId    프로젝트 ID
     * @param filePath     파일 경로 (예: /mariadb/init.sql)
     * @param outputStream 출력 스트림
     */
    public void downloadOnboardingFile(String projectId, String filePath, OutputStream outputStream) {
        log.info("온보딩 파일 다운로드 요청 - projectId: {}, filePath: {}", projectId, filePath);

        // 프로젝트 조회 및 검증
        findProjectById(projectId);

        // 경로 정규화
        String normalizedPath = filePath.replaceAll("^/+", "");
        Path fullPath = Paths.get(baseReleasePath, "onboardings", projectId, normalizedPath);

        if (!Files.exists(fullPath) || Files.isDirectory(fullPath)) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND, "파일을 찾을 수 없습니다: " + filePath);
        }

        try (InputStream is = Files.newInputStream(fullPath)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            log.info("온보딩 파일 다운로드 완료 - projectId: {}, filePath: {}", projectId, filePath);
        } catch (IOException e) {
            log.error("온보딩 파일 다운로드 실패 - projectId: {}, error: {}", projectId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_DOWNLOAD_FAILED, "파일 다운로드에 실패했습니다");
        }
    }

    /**
     * 온보딩 파일 크기 조회
     */
    public long getOnboardingFileSize(String projectId, String filePath) {
        String normalizedPath = filePath.replaceAll("^/+", "");
        Path fullPath = Paths.get(baseReleasePath, "onboardings", projectId, normalizedPath);

        if (!Files.exists(fullPath) || Files.isDirectory(fullPath)) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND, "파일을 찾을 수 없습니다: " + filePath);
        }

        try {
            return Files.size(fullPath);
        } catch (IOException e) {
            return 0L;
        }
    }

    /**
     * 온보딩 파일명 조회
     */
    public String getOnboardingFileName(String projectId, String filePath) {
        String normalizedPath = filePath.replaceAll("^/+", "");
        return Paths.get(normalizedPath).getFileName().toString();
    }

    /**
     * 온보딩 전체 파일 ZIP 다운로드
     *
     * @param projectId    프로젝트 ID
     * @param outputStream 출력 스트림
     */
    public void downloadAllOnboardingFiles(String projectId, OutputStream outputStream) {
        log.info("온보딩 전체 파일 다운로드 요청 - projectId: {}", projectId);

        // 프로젝트 조회 및 검증
        Project project = findProjectById(projectId);

        Path onboardingPath = Paths.get(baseReleasePath, "onboardings", projectId);

        if (!Files.exists(onboardingPath) || !Files.isDirectory(onboardingPath)) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND, "온보딩 파일이 없습니다");
        }

        try {
            // 파일 목록 수집
            List<StreamingZipUtil.ZipFileEntry> fileEntries = new ArrayList<>();
            try (Stream<Path> stream = Files.walk(onboardingPath)) {
                stream.filter(Files::isRegularFile)
                        .forEach(path -> {
                            String relativePath = onboardingPath.relativize(path).toString().replace("\\", "/");
                            fileEntries.add(new StreamingZipUtil.ZipFileEntry(path, relativePath));
                        });
            }

            if (fileEntries.isEmpty()) {
                throw new BusinessException(ErrorCode.FILE_NOT_FOUND, "다운로드할 파일이 없습니다");
            }

            // ZIP 스트리밍
            StreamingZipUtil.compressFilesToStream(outputStream, fileEntries);
            log.info("온보딩 전체 파일 다운로드 완료 - projectId: {}, fileCount: {}", projectId, fileEntries.size());

        } catch (IOException e) {
            log.error("온보딩 전체 파일 다운로드 실패 - projectId: {}, error: {}", projectId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_DOWNLOAD_FAILED, "파일 다운로드에 실패했습니다");
        }
    }

    /**
     * 온보딩 전체 파일 크기 조회 (압축 전)
     */
    public long getOnboardingTotalSize(String projectId) {
        Path onboardingPath = Paths.get(baseReleasePath, "onboardings", projectId);

        if (!Files.exists(onboardingPath)) {
            return 0L;
        }

        try (Stream<Path> stream = Files.walk(onboardingPath)) {
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

    /**
     * 온보딩 파일 삭제 (파일 경로 기반) - DB 및 파일시스템 동시 삭제
     *
     * @param projectId 프로젝트 ID
     * @param filePath  파일 경로 (예: /mariadb/init.sql)
     * @return 삭제 결과 응답
     */
    @Transactional
    public ProjectDto.OnboardingDeleteResponse deleteOnboardingFile(String projectId, String filePath) {
        log.info("온보딩 파일 삭제 요청 - projectId: {}, filePath: {}", projectId, filePath);

        // 프로젝트 조회 및 검증
        findProjectById(projectId);

        // 경로 정규화
        String normalizedPath = filePath.replaceAll("^/+", "");
        Path fullPath = Paths.get(baseReleasePath, "onboardings", projectId, normalizedPath);

        // DB 경로 생성
        String dbFilePath = "onboardings/" + projectId + "/" + normalizedPath;

        // DB에서 파일 정보 조회 및 삭제
        onboardingFileRepository.findByFilePath(dbFilePath)
                .ifPresent(onboardingFile -> {
                    onboardingFileRepository.delete(onboardingFile);
                    log.info("온보딩 파일 DB 삭제 완료 - id: {}, path: {}",
                            onboardingFile.getOnboardingFileId(), dbFilePath);
                });

        // 파일시스템에서 삭제
        if (Files.exists(fullPath)) {
            try {
                if (Files.isDirectory(fullPath)) {
                    // 디렉토리인 경우 하위 파일까지 전체 삭제
                    try (Stream<Path> stream = Files.walk(fullPath)) {
                        stream.sorted(Comparator.reverseOrder())
                                .forEach(path -> {
                                    try {
                                        Files.delete(path);
                                    } catch (IOException e) {
                                        log.warn("파일 삭제 실패: {}", path, e);
                                    }
                                });
                    }
                } else {
                    // 단일 파일 삭제
                    Files.delete(fullPath);
                }
                log.info("온보딩 파일 파일시스템 삭제 완료 - path: {}", fullPath);

            } catch (IOException e) {
                log.error("온보딩 파일 파일시스템 삭제 실패 - projectId: {}, error: {}", projectId, e.getMessage(), e);
                // DB 삭제는 완료되었으므로 파일시스템 삭제 실패는 경고로 처리
            }
        }

        return new ProjectDto.OnboardingDeleteResponse(
                projectId,
                filePath,
                "파일이 삭제되었습니다."
        );
    }

    /**
     * 온보딩 파일 삭제 (ID 기반) - DB 및 파일시스템 동시 삭제
     *
     * @param onboardingFileId 온보딩 파일 ID
     * @return 삭제 결과 응답
     */
    @Transactional
    public ProjectDto.OnboardingDeleteResponse deleteOnboardingFileById(Long onboardingFileId) {
        log.info("온보딩 파일 삭제 요청 (ID 기반) - onboardingFileId: {}", onboardingFileId);

        // DB에서 파일 정보 조회
        OnboardingFile onboardingFile = onboardingFileRepository.findById(onboardingFileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND,
                        "온보딩 파일을 찾을 수 없습니다: " + onboardingFileId));

        String projectId = onboardingFile.getProject().getProjectId();
        String filePath = onboardingFile.getFilePath();

        // 파일시스템에서 삭제
        Path fullPath = Paths.get(baseReleasePath, filePath);
        if (Files.exists(fullPath)) {
            try {
                Files.delete(fullPath);
                log.info("온보딩 파일 파일시스템 삭제 완료 - path: {}", fullPath);
            } catch (IOException e) {
                log.warn("온보딩 파일 파일시스템 삭제 실패 (계속 진행) - path: {}, error: {}", fullPath, e.getMessage());
            }
        }

        // DB에서 삭제
        onboardingFileRepository.delete(onboardingFile);
        log.info("온보딩 파일 DB 삭제 완료 - id: {}", onboardingFileId);

        // 응답용 상대 경로 생성
        String responseFilePath = "/" + filePath.replace("onboardings/" + projectId + "/", "");

        return new ProjectDto.OnboardingDeleteResponse(
                projectId,
                responseFilePath,
                "파일이 삭제되었습니다."
        );
    }

    /**
     * 온보딩 파일 단건 조회 (ID 기반)
     */
    public OnboardingFile getOnboardingFileById(Long onboardingFileId) {
        return onboardingFileRepository.findById(onboardingFileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND,
                        "온보딩 파일을 찾을 수 없습니다: " + onboardingFileId));
    }

    /**
     * 온보딩 파일 다운로드 (ID 기반)
     *
     * @param onboardingFileId 온보딩 파일 ID
     * @param outputStream     출력 스트림
     */
    public void downloadOnboardingFileById(Long onboardingFileId, OutputStream outputStream) {
        log.info("온보딩 파일 다운로드 요청 (ID 기반) - onboardingFileId: {}", onboardingFileId);

        OnboardingFile onboardingFile = getOnboardingFileById(onboardingFileId);
        Path filePath = Paths.get(baseReleasePath, onboardingFile.getFilePath());

        if (!Files.exists(filePath)) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND,
                    "파일을 찾을 수 없습니다: " + onboardingFile.getFileName());
        }

        try (InputStream is = Files.newInputStream(filePath)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            log.info("온보딩 파일 다운로드 완료 - id: {}, fileName: {}",
                    onboardingFileId, onboardingFile.getFileName());
        } catch (IOException e) {
            log.error("온보딩 파일 다운로드 실패 - id: {}, error: {}", onboardingFileId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_DOWNLOAD_FAILED, "파일 다운로드에 실패했습니다");
        }
    }

    // === Private Helper Methods ===

    /**
     * 프로젝트 조회 (존재하지 않으면 예외 발생)
     */
    private Project findProjectById(String projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
    }
}
