package com.ts.rm.domain.board.mapper;

import com.ts.rm.domain.board.dto.BoardCommentDto;
import com.ts.rm.domain.board.entity.BoardComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * BoardComment Entity ↔ DTO 변환 Mapper (MapStruct로 구현)
 */
@Mapper(componentModel = "spring")
public interface BoardCommentDtoMapper {

    @Mapping(target = "commentId", ignore = true)
    @Mapping(target = "post", ignore = true)
    @Mapping(target = "parentComment", ignore = true)
    @Mapping(target = "likeCount", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "createdByEmail", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BoardComment toEntity(BoardCommentDto.CreateRequest request);

    @Mapping(target = "postId", source = "post.postId")
    @Mapping(target = "parentCommentId", source = "parentComment.commentId")
    @Mapping(target = "isLikedByMe", ignore = true)
    @Mapping(target = "createdById", source = "creator.accountId")
    @Mapping(target = "createdByEmail", source = "createdByEmail")
    @Mapping(target = "createdByName", source = "creator.accountName")
    @Mapping(target = "createdByAvatarStyle", source = "creator.avatarStyle")
    @Mapping(target = "createdByAvatarSeed", source = "creator.avatarSeed")
    @Mapping(target = "replies", ignore = true)
    BoardCommentDto.Response toResponse(BoardComment comment);

    @Mapping(target = "isLikedByMe", ignore = true)
    @Mapping(target = "replyCount", ignore = true)
    @Mapping(target = "createdByName", source = "creator.accountName")
    @Mapping(target = "createdByAvatarStyle", source = "creator.avatarStyle")
    @Mapping(target = "createdByAvatarSeed", source = "creator.avatarSeed")
    BoardCommentDto.ListResponse toListResponse(BoardComment comment);
}
