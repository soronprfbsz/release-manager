package com.ts.rm.domain.remote.service;

import com.ts.rm.domain.remote.dto.response.BackupJobResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 백업 작업 상태 관리자
 *
 * 비동기 작업의 상태를 메모리에서 관리합니다.
 */
@Slf4j
@Component
public class BackupJobStatusManager {

    private final Map<String, BackupJobResponse> jobStatusMap = new ConcurrentHashMap<>();

    /**
     * 작업 상태 저장
     */
    public void saveJobStatus(String jobId, BackupJobResponse response) {
        log.debug("작업 상태 저장 - jobId: {}, status: {}", jobId, response.getStatus());
        jobStatusMap.put(jobId, response);
    }

    /**
     * 작업 상태 조회
     */
    public BackupJobResponse getJobStatus(String jobId) {
        return jobStatusMap.get(jobId);
    }

    /**
     * 작업 상태 존재 여부 확인
     */
    public boolean hasJobStatus(String jobId) {
        return jobStatusMap.containsKey(jobId);
    }

    /**
     * 작업 상태 삭제
     */
    public void removeJobStatus(String jobId) {
        log.debug("작업 상태 삭제 - jobId: {}", jobId);
        jobStatusMap.remove(jobId);
    }

    /**
     * 모든 작업 ID 조회
     */
    public Map<String, BackupJobResponse> getAllJobStatuses() {
        return new ConcurrentHashMap<>(jobStatusMap);
    }
}
