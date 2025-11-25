package com.ts.rm.domain.releaseversion.service;

import com.ts.rm.domain.releaseversion.entity.ReleaseVersionHistory;
import com.ts.rm.domain.releaseversion.repository.ReleaseVersionHistoryRepository;
import com.ts.rm.global.common.exception.BusinessException;
import com.ts.rm.global.common.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ReleaseVersionHistory Service
 *
 * <p>릴리즈 버전 이력 관리 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReleaseVersionHistoryService {

    private final ReleaseVersionHistoryRepository releaseVersionHistoryRepository;

    /**
     * 릴리즈 버전 이력 조회 (ID)
     *
     * @param releaseVersionId 릴리즈 버전 ID
     * @return 릴리즈 버전 이력
     */
    public ReleaseVersionHistory getReleaseVersionHistoryById(String releaseVersionId) {
        return releaseVersionHistoryRepository.findById(releaseVersionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND,
                        "릴리즈 버전 이력을 찾을 수 없습니다: " + releaseVersionId));
    }

    /**
     * 전체 릴리즈 버전 이력 조회
     *
     * @return 릴리즈 버전 이력 목록
     */
    public List<ReleaseVersionHistory> getAllReleaseVersionHistory() {
        return releaseVersionHistoryRepository.findAll();
    }

    /**
     * 적용된 릴리즈 버전 이력 조회 (시스템 적용일시 내림차순)
     *
     * @return 적용된 릴리즈 버전 이력 목록
     */
    public List<ReleaseVersionHistory> getAppliedReleaseVersionHistory() {
        return releaseVersionHistoryRepository.findBySystemAppliedAtIsNotNullOrderBySystemAppliedAtDesc();
    }

    /**
     * 표준 버전으로 조회
     *
     * @param standardVersion 표준 버전
     * @return 릴리즈 버전 이력 목록
     */
    public List<ReleaseVersionHistory> getReleaseVersionHistoryByStandardVersion(String standardVersion) {
        return releaseVersionHistoryRepository.findByStandardVersionOrderBySystemAppliedAtDesc(
                standardVersion);
    }

    /**
     * 릴리즈 버전 이력 생성 또는 업데이트
     *
     * @param releaseVersionHistory 릴리즈 버전 이력
     * @return 저장된 릴리즈 버전 이력
     */
    @Transactional
    public ReleaseVersionHistory saveOrUpdate(ReleaseVersionHistory releaseVersionHistory) {
        log.info("릴리즈 버전 이력 저장: {}", releaseVersionHistory.getReleaseVersionId());
        return releaseVersionHistoryRepository.save(releaseVersionHistory);
    }

    /**
     * 시스템 적용 정보 업데이트
     *
     * @param releaseVersionId 릴리즈 버전 ID
     * @param appliedBy 적용자
     * @return 업데이트된 릴리즈 버전 이력
     */
    @Transactional
    public ReleaseVersionHistory updateSystemApplied(String releaseVersionId, String appliedBy) {
        ReleaseVersionHistory releaseVersionHistory = getReleaseVersionHistoryById(releaseVersionId);
        releaseVersionHistory.updateSystemApplied(appliedBy);

        log.info("릴리즈 버전 이력 시스템 적용 업데이트: {} by {}", releaseVersionId, appliedBy);
        return releaseVersionHistoryRepository.save(releaseVersionHistory);
    }
}
