package com.ts.rm.domain.board.mapper;

import com.ts.rm.domain.board.dto.BoardTopicDto;
import com.ts.rm.domain.board.entity.BoardTopic;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * BoardTopic Entity ↔ DTO 변환 Mapper (MapStruct로 구현)
 */
@Mapper(componentModel = "spring")
public interface BoardTopicDtoMapper {

    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "createdByEmail", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BoardTopic toEntity(BoardTopicDto.CreateRequest request);

    @Mapping(target = "postCount", ignore = true)
    BoardTopicDto.Response toResponse(BoardTopic topic);

    BoardTopicDto.ListResponse toListResponse(BoardTopic topic);

    List<BoardTopicDto.ListResponse> toListResponseList(List<BoardTopic> topics);
}
