package com.ts.rm.domain.releaseversion.service;

import com.ts.rm.domain.releaseversion.entity.VersionHistory;
import com.ts.rm.domain.releaseversion.repository.VersionHistoryRepository;
import com.ts.rm.global.common.exception.BusinessException;
import com.ts.rm.global.common.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * VersionHistory Service
 *
 * <p>버전 이력 관리 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VersionHistoryService {

    private final VersionHistoryRepository versionHistoryRepository;

    /**
     * 버전 이력 조회 (ID)
     *
     * @param versionId 버전 ID
     * @return 버전 이력
     */
    public VersionHistory getVersionHistoryById(String versionId) {
        return versionHistoryRepository.findById(versionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND,
                        "버전 이력을 찾을 수 없습니다: " + versionId));
    }

    /**
     * 전체 버전 이력 조회
     *
     * @return 버전 이력 목록
     */
    public List<VersionHistory> getAllVersionHistory() {
        return versionHistoryRepository.findAll();
    }

    /**
     * 적용된 버전 이력 조회 (시스템 적용일시 내림차순)
     *
     * @return 적용된 버전 이력 목록
     */
    public List<VersionHistory> getAppliedVersionHistory() {
        return versionHistoryRepository.findBySystemAppliedAtIsNotNullOrderBySystemAppliedAtDesc();
    }

    /**
     * 표준 버전으로 조회
     *
     * @param standardVersion 표준 버전
     * @return 버전 이력 목록
     */
    public List<VersionHistory> getVersionHistoryByStandardVersion(String standardVersion) {
        return versionHistoryRepository.findByStandardVersionOrderBySystemAppliedAtDesc(
                standardVersion);
    }

    /**
     * 버전 이력 생성 또는 업데이트
     *
     * @param versionHistory 버전 이력
     * @return 저장된 버전 이력
     */
    @Transactional
    public VersionHistory saveOrUpdate(VersionHistory versionHistory) {
        log.info("버전 이력 저장: {}", versionHistory.getVersionId());
        return versionHistoryRepository.save(versionHistory);
    }

    /**
     * 시스템 적용 정보 업데이트
     *
     * @param versionId 버전 ID
     * @param appliedBy 적용자
     * @return 업데이트된 버전 이력
     */
    @Transactional
    public VersionHistory updateSystemApplied(String versionId, String appliedBy) {
        VersionHistory versionHistory = getVersionHistoryById(versionId);
        versionHistory.updateSystemApplied(appliedBy);

        log.info("버전 이력 시스템 적용 업데이트: {} by {}", versionId, appliedBy);
        return versionHistoryRepository.save(versionHistory);
    }
}
