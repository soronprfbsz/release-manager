package com.ts.rm.domain.common.repository;

import com.ts.rm.domain.common.entity.Code;
import com.ts.rm.domain.common.entity.CodeId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CodeRepository extends JpaRepository<Code, CodeId> {

    /**
     * 특정 코드 타입의 활성화된 코드 목록 조회 (정렬 순서대로)
     *
     * @param codeTypeId 코드 타입 ID
     * @return 코드 목록
     */
    @Query("SELECT c FROM Code c WHERE c.codeTypeId = :codeTypeId AND c.isEnabled = true ORDER BY c.sortOrder ASC")
    List<Code> findByCodeTypeIdAndIsEnabledTrueOrderBySortOrderAsc(@Param("codeTypeId") String codeTypeId);
}
