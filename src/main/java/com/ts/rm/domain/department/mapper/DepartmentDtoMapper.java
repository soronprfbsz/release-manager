package com.ts.rm.domain.department.mapper;

import com.ts.rm.domain.department.dto.DepartmentDto;
import com.ts.rm.domain.department.entity.Department;
import java.util.List;
import org.mapstruct.Mapper;

/**
 * Department Entity ↔ DTO 변환 Mapper
 */
@Mapper(componentModel = "spring")
public interface DepartmentDtoMapper {

    DepartmentDto.Response toResponse(Department department);

    List<DepartmentDto.Response> toResponseList(List<Department> departments);
}
