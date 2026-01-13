package com.ts.rm.domain.patch.service;

import com.ts.rm.domain.patch.dto.PatchHistoryDto;
import com.ts.rm.domain.patch.entity.Patch;
import com.ts.rm.domain.patch.entity.PatchHistory;
import com.ts.rm.domain.patch.repository.PatchHistoryRepository;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import com.ts.rm.global.pagination.PageRowNumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PatchHistory Service
 *
 * <p>패치 이력 관리 서비스 (조회 및 저장)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PatchHistoryService {

    private final PatchHistoryRepository patchHistoryRepository;

    /**
     * 패치 이력 저장
     *
     * <p>Patch 생성 시 호출되어 이력을 영구 보존
     *
     * @param patch 생성된 Patch 엔티티
     * @return 저장된 PatchHistory 엔티티
     */
    @Transactional
    public PatchHistory saveHistory(Patch patch) {
        PatchHistory history = PatchHistory.fromPatch(patch);
        PatchHistory savedHistory = patchHistoryRepository.save(history);
        log.info("패치 이력 저장 완료 - historyId: {}, patchName: {}",
                savedHistory.getHistoryId(), savedHistory.getPatchName());
        return savedHistory;
    }

    /**
     * 패치 이력 삭제
     *
     * @param historyId 이력 ID
     */
    @Transactional
    public void deleteHistory(Long historyId) {
        PatchHistory history = patchHistoryRepository.findById(historyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND,
                        "패치 이력을 찾을 수 없습니다. ID: " + historyId));

        patchHistoryRepository.delete(history);
        log.info("패치 이력 삭제 완료 - historyId: {}, patchName: {}",
                historyId, history.getPatchName());
    }

    /**
     * 패치 이력 목록 조회 (필터링 + 페이징)
     *
     * @param projectId  프로젝트 ID (null이면 전체)
     * @param customerId 고객사 ID (null이면 전체)
     * @param pageable   페이징 정보
     * @return 패치 이력 목록 페이지 (rowNumber 포함)
     */
    @Transactional(readOnly = true)
    public Page<PatchHistoryDto.ListResponse> listHistoriesWithPaging(
            String projectId, Long customerId, Pageable pageable) {
        Page<PatchHistory> histories = patchHistoryRepository.findAllWithFilters(
                projectId, customerId, pageable);

        // rowNumber 계산 (공통 유틸리티 사용)
        return PageRowNumberUtil.mapWithRowNumber(histories, this::toListResponse);
    }

    /**
     * PatchHistory 엔티티를 ListResponse DTO로 변환
     */
    private PatchHistoryDto.ListResponse toListResponse(PatchHistory history, Long rowNumber) {
        return new PatchHistoryDto.ListResponse(
                rowNumber,
                history.getHistoryId(),
                history.getProject().getProjectId(),
                history.getReleaseType(),
                history.getCustomer() != null ? history.getCustomer().getCustomerId() : null,
                history.getCustomer() != null ? history.getCustomer().getCustomerCode() : null,
                history.getCustomer() != null ? history.getCustomer().getCustomerName() : null,
                history.getFromVersion(),
                history.getToVersion(),
                history.getPatchName(),
                history.getDescription(),
                history.getAssignee() != null ? history.getAssignee().getAccountId() : null,
                history.getAssigneeName(),
                history.getAssigneeEmail(),
                history.getAssignee() != null ? history.getAssignee().getAvatarStyle() : null,
                history.getAssignee() != null ? history.getAssignee().getAvatarSeed() : null,
                history.getAssignee() == null && history.getAssigneeEmail() != null,
                history.getCreatedByEmail(),
                history.getCreator() != null ? history.getCreator().getAvatarStyle() : null,
                history.getCreator() != null ? history.getCreator().getAvatarSeed() : null,
                history.getCreator() == null,
                history.getCreatedAt()
        );
    }
}
