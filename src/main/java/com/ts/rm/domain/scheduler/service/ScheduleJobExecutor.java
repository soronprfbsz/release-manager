package com.ts.rm.domain.scheduler.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ts.rm.domain.scheduler.entity.ScheduleJob;
import com.ts.rm.domain.scheduler.entity.ScheduleJobHistory;
import com.ts.rm.domain.scheduler.enums.JobExecutionStatus;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * ScheduleJob Executor
 *
 * <p>스케줄 작업 실행 (API 호출)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleJobExecutor {

    private final ScheduleJobService jobService;
    private final ScheduleJobHistoryService historyService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${server.port:8081}")
    private int serverPort;

    /**
     * 작업 실행
     *
     * @param job 실행할 작업
     */
    public void execute(ScheduleJob job) {
        log.info("스케줄 작업 실행 시작 - jobId: {}, jobName: {}", job.getJobId(), job.getJobName());

        int maxAttempts = job.getRetryCount() + 1;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            ScheduleJobHistory history = historyService.createHistory(job, attempt);

            try {
                // API 호출
                ResponseEntity<String> response = callApi(job);

                // 성공 처리
                historyService.completeHistory(history, JobExecutionStatus.SUCCESS,
                        response.getStatusCode().value(),
                        response.getBody(),
                        null);

                // 마지막 실행 시각 업데이트
                jobService.updateLastExecutedAt(job.getJobId());

                log.info("스케줄 작업 실행 완료 - jobId: {}, status: {}, responseCode: {}",
                        job.getJobId(), JobExecutionStatus.SUCCESS, response.getStatusCode().value());
                return;

            } catch (ResourceAccessException e) {
                // 타임아웃 또는 연결 실패
                log.warn("스케줄 작업 실행 실패 (연결 오류) - jobId: {}, attempt: {}/{}, error: {}",
                        job.getJobId(), attempt, maxAttempts, e.getMessage());

                JobExecutionStatus status = e.getMessage().contains("timeout")
                        ? JobExecutionStatus.TIMEOUT
                        : JobExecutionStatus.FAILED;

                historyService.completeHistory(history, status, null, null, e.getMessage());

                if (attempt < maxAttempts) {
                    sleep(job.getRetryDelaySeconds());
                }

            } catch (HttpStatusCodeException e) {
                // HTTP 에러 응답
                log.warn("스케줄 작업 실행 실패 (HTTP 에러) - jobId: {}, attempt: {}/{}, status: {}",
                        job.getJobId(), attempt, maxAttempts, e.getStatusCode());

                historyService.completeHistory(history, JobExecutionStatus.FAILED,
                        e.getStatusCode().value(),
                        e.getResponseBodyAsString(),
                        e.getMessage());

                if (attempt < maxAttempts) {
                    sleep(job.getRetryDelaySeconds());
                }

            } catch (Exception e) {
                // 기타 예외
                log.error("스케줄 작업 실행 실패 (예외) - jobId: {}, attempt: {}/{}, error: {}",
                        job.getJobId(), attempt, maxAttempts, e.getMessage(), e);

                historyService.completeHistory(history, JobExecutionStatus.FAILED,
                        null, null, e.getMessage());

                if (attempt < maxAttempts) {
                    sleep(job.getRetryDelaySeconds());
                }
            }
        }

        // 모든 재시도 실패
        jobService.updateLastExecutedAt(job.getJobId());
        log.error("스케줄 작업 실행 최종 실패 - jobId: {}, jobName: {}", job.getJobId(), job.getJobName());
    }

    /**
     * API 호출
     */
    private ResponseEntity<String> callApi(ScheduleJob job) throws Exception {
        // URL 처리 ({port} 치환)
        String apiUrl = job.getApiUrl().replace("{port}", String.valueOf(serverPort));
        URI uri = new URI(apiUrl);

        // HTTP 메서드
        HttpMethod method = HttpMethod.valueOf(job.getHttpMethod().toUpperCase());

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 커스텀 헤더 추가
        if (job.getRequestHeaders() != null && !job.getRequestHeaders().isBlank()) {
            try {
                Map<String, String> customHeaders = objectMapper.readValue(
                        job.getRequestHeaders(),
                        new TypeReference<Map<String, String>>() {});
                customHeaders.forEach(headers::set);
            } catch (Exception e) {
                log.warn("요청 헤더 파싱 실패: {}", e.getMessage());
            }
        }

        // 내부 스케줄러 호출 표시 헤더 추가
        headers.set("X-Schedule-Job", job.getJobName());
        headers.set("X-Schedule-Job-Id", String.valueOf(job.getJobId()));

        // 요청 엔티티 생성
        RequestEntity<String> requestEntity;
        if (job.getRequestBody() != null && !job.getRequestBody().isBlank()) {
            requestEntity = new RequestEntity<>(job.getRequestBody(), headers, method, uri);
        } else {
            requestEntity = new RequestEntity<>(headers, method, uri);
        }

        log.debug("API 호출 - method: {}, url: {}", method, apiUrl);

        // API 호출 (타임아웃은 RestTemplate 설정에서 처리)
        return restTemplate.exchange(requestEntity, String.class);
    }

    /**
     * 재시도 대기
     */
    private void sleep(int seconds) {
        try {
            Thread.sleep(Duration.ofSeconds(seconds).toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
