package com.ts.rm.domain.project.controller;

import com.ts.rm.domain.project.dto.ProjectDto;
import com.ts.rm.domain.project.service.ProjectService;
import com.ts.rm.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * @param projectId 프로젝트 ID
     * @return 프로젝트 상세 정보
     */
    @Override
    @GetMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectDto.DetailResponse>> getProjectById(@PathVariable String projectId) {
        ProjectDto.DetailResponse response = projectService.getProjectById(projectId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 프로젝트 목록 조회
     *
     * @return 프로젝트 목록
     */
    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectDto.DetailResponse>>> getAllProjects() {
        List<ProjectDto.DetailResponse> response = projectService.getAllProjects();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 프로젝트 간단 목록 조회 (선택용)
     *
     * @return 프로젝트 간단 목록
     */
    @Override
    @GetMapping("/select")
    public ResponseEntity<ApiResponse<List<ProjectDto.SimpleResponse>>> getProjectsForSelect() {
        List<ProjectDto.SimpleResponse> response = projectService.getProjectsForSelect();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 프로젝트 정보 수정
     *
     * @param projectId 프로젝트 ID
     * @param request   수정 요청
     * @return 수정된 프로젝트 정보
     */
    @Override
    @PutMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectDto.DetailResponse>> updateProject(
            @PathVariable String projectId,
            @Valid @RequestBody ProjectDto.UpdateRequest request) {

        log.info("프로젝트 수정 요청 - projectId: {}", projectId);

        ProjectDto.DetailResponse response = projectService.updateProject(projectId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 프로젝트 삭제
     *
     * @param projectId 프로젝트 ID
     * @return 성공 응답
     */
    @Override
    @DeleteMapping("/{projectId}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable String projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
