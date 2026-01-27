package com.ts.rm.domain.scheduler.mapper;

import com.ts.rm.domain.scheduler.dto.ScheduleJobHistoryDto;
import com.ts.rm.domain.scheduler.entity.ScheduleJobHistory;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * ScheduleJobHistory Entity ↔ DTO 변환 Mapper
 */
@Mapper(componentModel = "spring")
public interface ScheduleJobHistoryDtoMapper {

    @Mapping(target = "jobId", source = "job.jobId")
    ScheduleJobHistoryDto.Response toResponse(ScheduleJobHistory entity);

    ScheduleJobHistoryDto.ListResponse toListResponse(ScheduleJobHistory entity);

    List<ScheduleJobHistoryDto.ListResponse> toListResponseList(List<ScheduleJobHistory> entities);
}
