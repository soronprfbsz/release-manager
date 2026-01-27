package com.ts.rm.global.logging.service;

import com.ts.rm.domain.account.entity.Account;
import com.ts.rm.domain.account.repository.AccountRepository;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import com.ts.rm.global.logging.dto.ApiLogDto;
import com.ts.rm.global.logging.entity.ApiLog;
import com.ts.rm.global.logging.repository.ApiLogRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ApiLog Service
 *
 * <p>API 로그 저장 및 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApiLogService {

    private final ApiLogRepository apiLogRepository;
    private final AccountRepository accountRepository;

    /**
     * API 로그 비동기 저장
     *
     * <p>별도 스레드에서 실행되어 API 응답 시간에 영향을 주지 않음
     *
     * @param apiLog 저장할 로그
     */
    @Async
    @Transactional
    public void saveAsync(ApiLog apiLog) {
        try {
            apiLogRepository.save(apiLog);
            log.debug("API 로그 저장 완료 - requestId: {}, uri: {}",
                    apiLog.getRequestId(), apiLog.getRequestUri());
        } catch (Exception e) {
            log.error("API 로그 저장 실패 - requestId: {}, error: {}",
                    apiLog.getRequestId(), e.getMessage());
        }
    }

    /**
     * 오래된 로그 삭제
     *
     * @param retentionDays 보관 기간 (일)
     * @return 삭제된 건수
     */
    @Transactional
    public long deleteOldLogs(int retentionDays) {
        log.info("오래된 API 로그 삭제 시작 - retentionDays: {}", retentionDays);
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        long deletedCount = apiLogRepository.deleteByCreatedAtBefore(cutoffDate);
        log.info("오래된 API 로그 삭제 완료 - deletedCount: {}", deletedCount);
        return deletedCount;
    }

    /**
     * API 로그 검색 (페이징)
     *
     * @param condition 검색 조건
     * @param pageable  페이징 정보
     * @return 검색 결과 페이지
     */
    public Page<ApiLogDto.ListResponse> searchLogs(ApiLogDto.SearchCondition condition, Pageable pageable) {
        log.debug("API 로그 검색 - condition: {}", condition);
        Page<ApiLog> logs = apiLogRepository.searchWithFilters(condition, pageable);

        // accountId가 있는 로그들의 Account 정보를 한번에 조회 (N+1 방지)
        List<Long> accountIds = logs.getContent().stream()
                .map(ApiLog::getAccountId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, Account> accountMap = accountIds.isEmpty()
                ? Map.of()
                : accountRepository.findAllById(accountIds).stream()
                        .collect(Collectors.toMap(Account::getAccountId, Function.identity()));

        return logs.map(apiLog -> toListResponse(apiLog, accountMap.get(apiLog.getAccountId())));
    }

    /**
     * API 로그 상세 조회
     *
     * @param logId 로그 ID
     * @return API 로그 상세 정보
     */
    public ApiLogDto.Response getLog(Long logId) {
        log.debug("API 로그 상세 조회 - logId: {}", logId);
        ApiLog apiLog = apiLogRepository.findById(logId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND,
                        "API 로그를 찾을 수 없습니다: " + logId));

        // Account 정보 조회
        Account account = apiLog.getAccountId() != null
                ? accountRepository.findById(apiLog.getAccountId()).orElse(null)
                : null;

        return toResponse(apiLog, account);
    }

    /**
     * ApiLog -> ListResponse 변환 (Account 정보 포함)
     */
    private ApiLogDto.ListResponse toListResponse(ApiLog apiLog, Account account) {
        return new ApiLogDto.ListResponse(
                apiLog.getLogId(),
                apiLog.getRequestId(),
                apiLog.getHttpMethod(),
                apiLog.getRequestUri(),
                apiLog.getResponseStatus(),
                apiLog.getClientIp(),
                apiLog.getAccountId(),
                apiLog.getAccountEmail(),
                account != null ? account.getAccountName() : null,
                account != null ? account.getAvatarStyle() : null,
                account != null ? account.getAvatarSeed() : null,
                apiLog.getExecutionTimeMs(),
                apiLog.getCreatedAt()
        );
    }

    /**
     * ApiLog -> Response 변환 (Account 정보 포함)
     */
    private ApiLogDto.Response toResponse(ApiLog apiLog, Account account) {
        return new ApiLogDto.Response(
                apiLog.getLogId(),
                apiLog.getRequestId(),
                apiLog.getHttpMethod(),
                apiLog.getRequestUri(),
                apiLog.getQueryString(),
                apiLog.getRequestBody(),
                apiLog.getRequestContentType(),
                apiLog.getResponseStatus(),
                apiLog.getResponseBody(),
                apiLog.getResponseContentType(),
                apiLog.getClientIp(),
                apiLog.getUserAgent(),
                apiLog.getAccountId(),
                apiLog.getAccountEmail(),
                account != null ? account.getAccountName() : null,
                account != null ? account.getAvatarStyle() : null,
                account != null ? account.getAvatarSeed() : null,
                apiLog.getExecutionTimeMs(),
                apiLog.getCreatedAt()
        );
    }
}
