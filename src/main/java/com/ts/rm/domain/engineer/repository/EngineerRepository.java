package com.ts.rm.domain.engineer.repository;

import com.ts.rm.domain.engineer.entity.Engineer;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Engineer Repository
 *
 * <p>Spring Data JPA 메서드 네이밍으로 CRUD 처리
 */
public interface EngineerRepository extends JpaRepository<Engineer, Long> {

    /**
     * 이메일로 엔지니어 조회
     */
    Optional<Engineer> findByEngineerEmail(String engineerEmail);

    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEngineerEmail(String engineerEmail);

    /**
     * 부서 ID로 엔지니어 조회 (페이징)
     */
    Page<Engineer> findByDepartmentDepartmentId(Long departmentId, Pageable pageable);

    /**
     * 이름으로 검색 (부분 일치, 페이징)
     */
    Page<Engineer> findByEngineerNameContaining(String keyword, Pageable pageable);

    /**
     * 부서 ID와 이름으로 검색 (페이징)
     */
    Page<Engineer> findByDepartmentDepartmentIdAndEngineerNameContaining(Long departmentId, String keyword, Pageable pageable);
}
