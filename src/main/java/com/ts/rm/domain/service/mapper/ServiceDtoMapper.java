package com.ts.rm.domain.service.mapper;

import com.ts.rm.domain.service.dto.ServiceDto;
import com.ts.rm.domain.service.entity.Service;
import com.ts.rm.domain.service.entity.ServiceComponent;
import com.ts.rm.domain.service.enums.ComponentType;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Service Entity ↔ DTO 변환 Mapper (MapStruct로 구현)
 */
@Mapper(componentModel = "spring")
public interface ServiceDtoMapper {

    // ========================================
    // Service Entity → DTO
    // ========================================

    @Mapping(target = "serviceTypeName", ignore = true)
    @Mapping(target = "components", ignore = true)
    ServiceDto.DetailResponse toDetailResponse(Service service);

    @Mapping(target = "serviceTypeName", ignore = true)
    @Mapping(target = "componentCount", ignore = true)
    ServiceDto.SimpleResponse toSimpleResponse(Service service);

    List<ServiceDto.DetailResponse> toDetailResponseList(List<Service> services);

    List<ServiceDto.SimpleResponse> toSimpleResponseList(List<Service> services);

    // ========================================
    // ServiceComponent Entity → DTO
    // ========================================

    @Mapping(target = "componentType", expression = "java(component.getComponentType().getCode())")
    @Mapping(target = "componentTypeName", ignore = true)
    ServiceDto.ComponentResponse toComponentResponse(ServiceComponent component);

    List<ServiceDto.ComponentResponse> toComponentResponseList(List<ServiceComponent> components);

    // ========================================
    // DTO → ServiceComponent Entity
    // ========================================

    @Mapping(target = "componentId", ignore = true)
    @Mapping(target = "service", ignore = true)
    @Mapping(target = "componentType", expression = "java(toComponentType(request.componentType()))")
    @Mapping(target = "sortOrder", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ServiceComponent toEntity(ServiceDto.ComponentRequest request);

    List<ServiceComponent> toEntityList(List<ServiceDto.ComponentRequest> requests);

    // ========================================
    // Helper Methods
    // ========================================

    /**
     * 문자열 → ComponentType Enum 변환
     */
    default ComponentType toComponentType(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return ComponentType.fromCode(code);
    }
}
