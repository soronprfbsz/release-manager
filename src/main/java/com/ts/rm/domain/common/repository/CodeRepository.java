package com.ts.rm.domain.common.repository;

import com.ts.rm.domain.common.entity.Code;
import com.ts.rm.domain.common.entity.CodeId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Code Repository
 *
 * <p>Spring Data JPA 메서드 네이밍으로 CRUD 처리
 */
public interface CodeRepository extends JpaRepository<Code, CodeId> {

    /**
     * 특정 코드 타입의 활성화된 코드 목록 조회 (정렬 순서대로)
     *
     * @param codeTypeId 코드 타입 ID
     * @return 코드 목록
     */
    List<Code> findByCodeTypeIdAndIsEnabledTrueOrderBySortOrderAsc(String codeTypeId);

    /**
     * 코드 타입과 코드 ID로 코드 조회
     *
     * @param codeTypeId 코드 타입 ID
     * @param codeId     코드 ID
     * @return 코드 (Optional)
     */
    Optional<Code> findByCodeTypeIdAndCodeId(String codeTypeId, String codeId);

    /**
     * 코드 타입과 코드 ID로 존재 여부 확인
     *
     * @param codeTypeId 코드 타입 ID
     * @param codeId     코드 ID
     * @return 존재 여부
     */
    boolean existsByCodeTypeIdAndCodeId(String codeTypeId, String codeId);
}
