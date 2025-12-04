package com.ts.rm.domain.common.repository;

import com.ts.rm.domain.common.entity.CodeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CodeType Repository
 */
public interface CodeTypeRepository extends JpaRepository<CodeType, String> {

    /**
     * 활성화된 코드 타입 목록 조회 (코드 타입 ID 오름차순)
     *
     * @return 코드 타입 목록
     */
    List<CodeType> findByIsEnabledTrueOrderByCodeTypeIdAsc();
}
