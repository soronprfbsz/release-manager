package com.ts.rm.domain.common.service;

import com.ts.rm.domain.common.dto.CodeDto;
import com.ts.rm.domain.common.entity.Code;
import com.ts.rm.domain.common.entity.CodeType;
import com.ts.rm.domain.common.repository.CodeRepository;
import com.ts.rm.domain.common.repository.CodeTypeRepository;
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
    private final CodeTypeRepository codeTypeRepository;

    /**
     * 코드 타입(분류) 목록 조회
     *
     * @return 코드 타입 응답 목록
     */
    public List<CodeDto.CodeTypeResponse> getCodeTypes() {
        log.info("코드 타입 목록 조회");

        List<CodeType> codeTypes = codeTypeRepository.findByIsEnabledTrueOrderByCodeTypeIdAsc();

        return codeTypes.stream()
                .map(codeType -> new CodeDto.CodeTypeResponse(
                        codeType.getCodeTypeId(),
                        codeType.getCodeTypeName(),
                        codeType.getDescription()
                ))
                .toList();
    }

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

    /**
     * 특정 코드의 description 조회
     *
     * @param codeTypeId 코드 타입 ID
     * @param codeId     코드 ID
     * @return description (없으면 codeId 반환)
     */
    public String getCodeDescription(String codeTypeId, String codeId) {
        return codeRepository.findByCodeTypeIdAndCodeId(codeTypeId, codeId)
                .map(Code::getDescription)
                .orElse(codeId);
    }

    /**
     * 특정 코드 타입의 모든 코드 description을 Map으로 조회
     *
     * @param codeTypeId 코드 타입 ID
     * @return codeId → description Map
     */
    public java.util.Map<String, String> getCodeDescriptionMap(String codeTypeId) {
        List<Code> codes = codeRepository.findByCodeTypeIdAndIsEnabledTrueOrderBySortOrderAsc(codeTypeId);

        return codes.stream()
                .collect(java.util.stream.Collectors.toMap(
                        Code::getCodeId,
                        code -> code.getDescription() != null ? code.getDescription() : code.getCodeName()
                ));
    }

    /**
     * 특정 코드 타입의 모든 코드 이름(codeName)을 Map으로 조회
     *
     * @param codeTypeId 코드 타입 ID
     * @return codeId → codeName Map
     */
    public java.util.Map<String, String> getCodeNameMap(String codeTypeId) {
        List<Code> codes = codeRepository.findByCodeTypeIdAndIsEnabledTrueOrderBySortOrderAsc(codeTypeId);

        return codes.stream()
                .collect(java.util.stream.Collectors.toMap(
                        Code::getCodeId,
                        Code::getCodeName
                ));
    }
}
