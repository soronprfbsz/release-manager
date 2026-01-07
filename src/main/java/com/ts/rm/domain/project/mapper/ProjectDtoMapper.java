package com.ts.rm.domain.project.mapper;

import com.ts.rm.domain.project.dto.ProjectDto;
import com.ts.rm.domain.project.entity.Project;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Project Entity ↔ DTO 변환 Mapper (MapStruct로 구현)
 */
@Mapper(componentModel = "spring")
public interface ProjectDtoMapper {

    @Mapping(target = "isEnabled", ignore = true)
    @Mapping(target = "createdBy", constant = "SYSTEM")
    Project toEntity(ProjectDto.CreateRequest request);

    ProjectDto.DetailResponse toDetailResponse(Project project);

    List<ProjectDto.DetailResponse> toDetailResponseList(List<Project> projects);
}
