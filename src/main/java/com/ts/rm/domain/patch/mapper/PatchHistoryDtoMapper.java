package com.ts.rm.domain.patch.mapper;

import com.ts.rm.domain.patch.dto.PatchHistoryDto;
import com.ts.rm.domain.patch.entity.PatchHistory;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * PatchHistory Entity ↔ DTO 변환 Mapper (MapStruct로 구현)
 */
@Mapper(componentModel = "spring")
public interface PatchHistoryDtoMapper {

    @Mapping(target = "releaseType", source = "releaseType")
    @Mapping(target = "customerCode", source = "customer.customerCode")
    @Mapping(target = "customerName", source = "customer.customerName")
    PatchHistoryDto.SimpleResponse toSimpleResponse(PatchHistory patchHistory);

    List<PatchHistoryDto.SimpleResponse> toSimpleResponseList(
            List<PatchHistory> patchHistories);

    @Mapping(target = "releaseType", source = "releaseType")
    @Mapping(target = "customerCode", source = "customer.customerCode")
    @Mapping(target = "customerName", source = "customer.customerName")
    PatchHistoryDto.DetailResponse toDetailResponse(PatchHistory patchHistory);

    List<PatchHistoryDto.DetailResponse> toDetailResponseList(
            List<PatchHistory> patchHistories);
}
