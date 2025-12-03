package com.ts.rm.domain.common.service;

import com.ts.rm.domain.common.dto.CodeDto;
import com.ts.rm.domain.common.entity.Code;
import com.ts.rm.domain.common.repository.CodeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 코드 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CodeService {

    private final CodeRepository codeRepository;

    /**
     * 특정 코드 타입의 코드 목록 조회
     *
     * @param codeTypeId 코드 타입 ID (예: RELEASE_CATEGORY, FILE_CATEGORY 등)
     * @return 코드 간단 응답 목록
     */
    public List<CodeDto.SimpleResponse> getCodesByType(String codeTypeId) {
        log.info("코드 목록 조회 - codeTypeId: {}", codeTypeId);

        List<Code> codes = codeRepository.findByCodeTypeIdAndIsEnabledTrueOrderBySortOrderAsc(codeTypeId);

        return codes.stream()
                .map(code -> new CodeDto.SimpleResponse(
                        code.getCodeId(),      // value
                        code.getCodeName(),    // name
                        code.getSortOrder()    // sortOrder
                ))
                .toList();
    }
}
