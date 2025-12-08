package com.ts.rm.domain.project.repository;

import com.ts.rm.domain.project.entity.Project;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Project Repository
 *
 * <p>프로젝트 정보 조회 및 관리를 위한 Repository
 */
public interface ProjectRepository extends JpaRepository<Project, String> {

    /**
     * 프로젝트명으로 검색 (부분 일치)
     *
     * @param keyword 검색 키워드
     * @return 프로젝트 목록
     */
    List<Project> findByProjectNameContaining(String keyword);

    /**
     * 프로젝트 ID 존재 여부 확인
     *
     * @param projectId 프로젝트 ID
     * @return 존재 여부
     */
    boolean existsByProjectId(String projectId);

    /**
     * 전체 프로젝트 목록 조회 (프로젝트명 오름차순)
     *
     * @return 프로젝트 목록
     */
    List<Project> findAllByOrderByProjectNameAsc();
}
