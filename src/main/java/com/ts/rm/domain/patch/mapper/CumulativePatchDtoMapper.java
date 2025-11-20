package com.ts.rm.domain.patch.mapper;

import com.ts.rm.domain.patch.dto.CumulativePatchDto;
import com.ts.rm.domain.patch.entity.CumulativePatch;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * CumulativePatch Entity ↔ DTO 변환 Mapper (MapStruct로 구현)
 */
@Mapper(componentModel = "spring")
public interface CumulativePatchDtoMapper {

    @Mapping(target = "releaseType", source = "releaseType")
    @Mapping(target = "customerCode", source = "customer.customerCode")
    CumulativePatchDto.SimpleResponse toSimpleResponse(CumulativePatch cumulativePatch);

    List<CumulativePatchDto.SimpleResponse> toSimpleResponseList(
            List<CumulativePatch> cumulativePatches);

    @Mapping(target = "releaseType", source = "releaseType")
    @Mapping(target = "customerCode", source = "customer.customerCode")
    CumulativePatchDto.DetailResponse toDetailResponse(CumulativePatch cumulativePatch);

    List<CumulativePatchDto.DetailResponse> toDetailResponseList(
            List<CumulativePatch> cumulativePatches);
}
