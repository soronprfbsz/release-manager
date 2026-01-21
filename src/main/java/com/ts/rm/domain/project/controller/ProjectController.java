package com.ts.rm.domain.project.controller;

import com.ts.rm.domain.project.dto.ProjectDto;
import com.ts.rm.domain.project.service.ProjectService;
import com.ts.rm.global.file.HttpFileDownloadUtil;
import com.ts.rm.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Project Controller
 *
 * <p>프로젝트 관리 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController implements ProjectControllerDocs {

    private final ProjectService projectService;

    /**
     * 프로젝트 생성
     *
     * @param request 프로젝트 생성 요청
     * @return 생성된 프로젝트 정보
     */
    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<ProjectDto.DetailResponse>> createProject(
            @Valid @RequestBody ProjectDto.CreateRequest request) {

        log.info("프로젝트 생성 요청 - projectId: {}, projectName: {}",
                request.projectId(), request.projectName());

        ProjectDto.DetailResponse response = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 프로젝트 조회 (ID)
     *
     * @param id 프로젝트 ID
     * @return 프로젝트 상세 정보
     */
    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectDto.DetailResponse>> getProjectById(@PathVariable String id) {
        ProjectDto.DetailResponse response = projectService.getProjectById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 프로젝트 목록 조회
     *
     * @param isEnabled 활성 여부 필터 (null이면 전체 조회)
     * @return 프로젝트 목록
     */
    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectDto.DetailResponse>>> getAllProjects(
            @RequestParam(required = false) Boolean isEnabled) {
        List<ProjectDto.DetailResponse> response = projectService.getAllProjects(isEnabled);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 프로젝트 정보 수정
     *
     * @param id 프로젝트 ID
     * @param request   수정 요청
     * @return 수정된 프로젝트 정보
     */
    @Override
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectDto.DetailResponse>> updateProject(
            @PathVariable String id,
            @Valid @RequestBody ProjectDto.UpdateRequest request) {

        log.info("프로젝트 수정 요청 - projectId: {}", id);

        ProjectDto.DetailResponse response = projectService.updateProject(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 프로젝트 삭제
     *
     * @param id 프로젝트 ID
     * @return 성공 응답
     */
    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable String id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 프로젝트별 온보딩 파일 트리 조회 (파일시스템 기반)
     *
     * @param id 프로젝트 ID
     * @return 온보딩 파일 트리 응답
     */
    @Override
    @GetMapping("/{id}/onboardings/files")
    public ResponseEntity<ApiResponse<ProjectDto.OnboardingFilesResponse>> getOnboardingFiles(
            @PathVariable String id) {

        log.info("온보딩 파일 트리 조회 API 호출 - projectId: {}", id);

        ProjectDto.OnboardingFilesResponse response = projectService.getOnboardingFiles(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 온보딩 디렉토리 생성
     *
     * @param id   프로젝트 ID
     * @param path 생성할 디렉토리 경로
     * @return 생성 결과 응답
     */
    @Override
    @PostMapping("/{id}/onboardings/files/directory")
    public ResponseEntity<ApiResponse<ProjectDto.OnboardingDirectoryResponse>> createOnboardingDirectory(
            @PathVariable String id,
            @RequestParam String path) {

        log.info("온보딩 디렉토리 생성 API 호출 - projectId: {}, path: {}", id, path);

        ProjectDto.OnboardingDirectoryResponse response = projectService.createOnboardingDirectory(id, path);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 온보딩 파일 업로드
     *
     * @param id          프로젝트 ID
     * @param file        업로드할 파일 (ZIP 또는 단일 파일)
     * @param targetPath  대상 경로 (선택)
     * @param description 파일 설명 (선택)
     * @param extractZip  ZIP 파일 압축 해제 여부 (기본값: true)
     * @return 업로드 결과 응답
     */
    @Override
    @PostMapping(value = "/{id}/onboardings/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProjectDto.OnboardingUploadResponse>> uploadOnboardingFile(
            @PathVariable String id,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String targetPath,
            @RequestParam(required = false) String description,
            @RequestParam(required = false, defaultValue = "true") Boolean extractZip) {

        log.info("온보딩 파일 업로드 API 호출 - projectId: {}, fileName: {}, targetPath: {}, extractZip: {}",
                id, file.getOriginalFilename(), targetPath, extractZip);

        ProjectDto.OnboardingUploadResponse response =
                projectService.uploadOnboardingFile(id, file, targetPath, description, extractZip);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 온보딩 전체 파일 ZIP 다운로드
     *
     * @param id       프로젝트 ID
     * @param response HTTP 응답
     */
    @Override
    @GetMapping("/{id}/onboardings/files/zip-download")
    public void downloadAllOnboardingFiles(
            @PathVariable String id,
            HttpServletResponse response) throws IOException {

        log.info("온보딩 전체 파일 다운로드 API 호출 - projectId: {}", id);

        String zipFileName = id + "_onboarding_files.zip";
        long totalSize = projectService.getOnboardingTotalSize(id);

        response.setContentType("application/zip");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                HttpFileDownloadUtil.buildContentDisposition(zipFileName));
        response.setHeader("X-Uncompressed-Size", String.valueOf(totalSize));

        projectService.downloadAllOnboardingFiles(id, response.getOutputStream());

        log.info("온보딩 전체 파일 다운로드 완료 - projectId: {}", id);
    }

    /**
     * 온보딩 파일 삭제 (경로 기반)
     *
     * @param id       프로젝트 ID
     * @param filePath 삭제할 파일 경로 (예: /mariadb/init.sql)
     * @return 삭제 결과 응답
     */
    @Override
    @DeleteMapping("/{id}/onboardings/files")
    public ResponseEntity<ApiResponse<ProjectDto.OnboardingDeleteResponse>> deleteOnboardingFile(
            @PathVariable String id,
            @RequestParam String filePath) {

        log.info("온보딩 파일 삭제 API 호출 - projectId: {}, filePath: {}", id, filePath);

        ProjectDto.OnboardingDeleteResponse response = projectService.deleteOnboardingFile(id, filePath);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
