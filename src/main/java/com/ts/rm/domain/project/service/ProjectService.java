package com.ts.rm.domain.project.service;

import com.ts.rm.domain.project.dto.ProjectDto;
import com.ts.rm.domain.project.entity.Project;
import com.ts.rm.domain.project.mapper.ProjectDtoMapper;
import com.ts.rm.domain.project.repository.ProjectRepository;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final ProjectDtoMapper mapper;

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

    // === Private Helper Methods ===

    /**
     * 프로젝트 조회 (존재하지 않으면 예외 발생)
     */
    private Project findProjectById(String projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
    }
}
