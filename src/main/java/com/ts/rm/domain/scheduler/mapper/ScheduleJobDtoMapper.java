package com.ts.rm.domain.scheduler.mapper;

import com.ts.rm.domain.scheduler.dto.ScheduleJobDto;
import com.ts.rm.domain.scheduler.entity.ScheduleJob;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * ScheduleJob Entity ↔ DTO 변환 Mapper
 */
@Mapper(componentModel = "spring")
public interface ScheduleJobDtoMapper {

    @Mapping(target = "jobId", ignore = true)
    @Mapping(target = "lastExecutedAt", ignore = true)
    @Mapping(target = "nextExecutionAt", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ScheduleJob toEntity(ScheduleJobDto.CreateRequest request);

    ScheduleJobDto.Response toResponse(ScheduleJob entity);

    ScheduleJobDto.ListResponse toListResponse(ScheduleJob entity);

    List<ScheduleJobDto.ListResponse> toListResponseList(List<ScheduleJob> entities);
}
