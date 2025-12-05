package com.ts.rm.domain.department.repository;

import com.ts.rm.domain.department.entity.Department;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Department Repository
 */
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /**
     * 부서명으로 정렬하여 전체 조회
     */
    List<Department> findAllByOrderByDepartmentNameAsc();
}
