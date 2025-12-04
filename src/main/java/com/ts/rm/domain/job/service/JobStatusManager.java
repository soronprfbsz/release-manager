package com.ts.rm.domain.job.service;

import com.ts.rm.domain.job.dto.JobResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 작업 상태 관리자
 *
 * 비동기 작업의 상태를 메모리에서 관리합니다.
 */
@Slf4j
@Component
public class JobStatusManager {

    private final Map<String, JobResponse> jobStatusMap = new ConcurrentHashMap<>();

    /**
     * 작업 상태 저장
     */
    public void saveJobStatus(String jobId, JobResponse response) {
        log.debug("작업 상태 저장 - jobId: {}, status: {}", jobId, response.getStatus());
        jobStatusMap.put(jobId, response);
    }

    /**
     * 작업 상태 조회
     */
    public JobResponse getJobStatus(String jobId) {
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
    public Map<String, JobResponse> getAllJobStatuses() {
        return new ConcurrentHashMap<>(jobStatusMap);
    }
}
