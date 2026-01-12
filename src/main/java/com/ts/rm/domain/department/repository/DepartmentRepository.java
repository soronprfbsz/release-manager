package com.ts.rm.domain.department.repository;

import com.ts.rm.domain.department.entity.Department;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Department Repository
 */
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /**
     * 부서명으로 정렬하여 전체 조회
     */
    List<Department> findAllByOrderByDepartmentNameAsc();

    /**
     * 부서명으로 조회
     */
    Optional<Department> findByDepartmentName(String departmentName);

    /**
     * 부서명 존재 여부 확인
     */
    boolean existsByDepartmentName(String departmentName);

    /**
     * 특정 부서 제외하고 부서명 존재 여부 확인 (수정 시 중복 체크용)
     */
    boolean existsByDepartmentNameAndDepartmentIdNot(String departmentName, Long departmentId);
}
