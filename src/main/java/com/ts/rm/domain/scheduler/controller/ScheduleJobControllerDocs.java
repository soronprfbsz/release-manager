package com.ts.rm.domain.scheduler.controller;

import com.ts.rm.domain.scheduler.dto.ScheduleJobDto;
import com.ts.rm.domain.scheduler.dto.ScheduleJobHistoryDto;
import com.ts.rm.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

/**
 * ScheduleJob Controller Swagger Documentation
 */
@Tag(name = "Schedule Jobs", description = "스케줄 작업 관리 API")
public interface ScheduleJobControllerDocs {

    @Operation(summary = "스케줄 작업 목록 조회", description = "등록된 스케줄 작업 목록을 조회합니다.")
    ResponseEntity<ApiResponse<List<ScheduleJobDto.ListResponse>>> getJobs(
            @Parameter(description = "작업 그룹 (선택)") String jobGroup);

    @Operation(summary = "스케줄 작업 상세 조회", description = "스케줄 작업의 상세 정보를 조회합니다.")
    ResponseEntity<ApiResponse<ScheduleJobDto.Response>> getJob(
            @Parameter(description = "작업 ID") Long jobId);

    @Operation(summary = "스케줄 작업 생성", description = "새로운 스케줄 작업을 등록합니다.")
    ResponseEntity<ApiResponse<ScheduleJobDto.Response>> createJob(ScheduleJobDto.CreateRequest request);

    @Operation(summary = "스케줄 작업 수정", description = "스케줄 작업을 수정합니다.")
    ResponseEntity<ApiResponse<ScheduleJobDto.Response>> updateJob(
            @Parameter(description = "작업 ID") Long jobId,
            ScheduleJobDto.UpdateRequest request);

    @Operation(summary = "스케줄 작업 삭제", description = "스케줄 작업을 삭제합니다.")
    ResponseEntity<ApiResponse<Void>> deleteJob(
            @Parameter(description = "작업 ID") Long jobId);

    @Operation(summary = "스케줄 작업 활성화/비활성화", description = "스케줄 작업의 활성화 상태를 토글합니다.")
    ResponseEntity<ApiResponse<ScheduleJobDto.Response>> toggleJobEnabled(
            @Parameter(description = "작업 ID") Long jobId);

    @Operation(summary = "스케줄 작업 즉시 실행", description = "스케줄 작업을 즉시 실행합니다.")
    ResponseEntity<ApiResponse<String>> executeJobNow(
            @Parameter(description = "작업 ID") Long jobId);

    @Operation(summary = "모든 스케줄 새로고침", description = "모든 스케줄을 데이터베이스에서 다시 로드합니다.")
    ResponseEntity<ApiResponse<String>> refreshAllJobs();

    @Operation(summary = "작업별 실행 이력 조회", description = "특정 작업의 실행 이력을 조회합니다.")
    ResponseEntity<ApiResponse<Page<ScheduleJobHistoryDto.ListResponse>>> getJobHistories(
            @Parameter(description = "작업 ID") Long jobId,
            Pageable pageable);

    @Operation(summary = "실행 이력 상세 조회", description = "실행 이력의 상세 정보를 조회합니다.")
    ResponseEntity<ApiResponse<ScheduleJobHistoryDto.Response>> getHistory(
            @Parameter(description = "이력 ID") Long historyId);
}
