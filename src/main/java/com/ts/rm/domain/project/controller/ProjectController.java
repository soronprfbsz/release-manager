package com.ts.rm.domain.project.controller;

import com.ts.rm.domain.project.dto.ProjectDto;
import com.ts.rm.domain.project.service.ProjectService;
import com.ts.rm.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "프로젝트", description = "프로젝트 관리 API")
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /**
     * 프로젝트 생성
     *
     * @param request 프로젝트 생성 요청
     * @return 생성된 프로젝트 정보
     */
    @Operation(summary = "프로젝트 생성", description = "새로운 프로젝트를 생성합니다")
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
    @Operation(summary = "프로젝트 조회", description = "ID로 프로젝트 정보를 조회합니다")
    @GetMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectDto.DetailResponse>> getProjectById(
            @Parameter(description = "프로젝트 ID", required = true) @PathVariable String projectId) {
        ProjectDto.DetailResponse response = projectService.getProjectById(projectId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 프로젝트 목록 조회
     *
     * @return 프로젝트 목록
     */
    @Operation(summary = "프로젝트 목록 조회", description = "전체 프로젝트 목록을 조회합니다")
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
    @Operation(summary = "프로젝트 선택 목록", description = "드롭다운 선택용 프로젝트 목록을 조회합니다")
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
    @Operation(summary = "프로젝트 수정", description = "프로젝트 정보를 수정합니다 (프로젝트 ID는 변경 불가)")
    @PutMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectDto.DetailResponse>> updateProject(
            @Parameter(description = "프로젝트 ID", required = true) @PathVariable String projectId,
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
    @Operation(summary = "프로젝트 삭제", description = "프로젝트를 삭제합니다. 연관된 릴리즈 버전/패치가 있으면 삭제할 수 없습니다")
    @DeleteMapping("/{projectId}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @Parameter(description = "프로젝트 ID", required = true) @PathVariable String projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
