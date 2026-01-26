package com.ts.rm.domain.board.mapper;

import com.ts.rm.domain.board.dto.BoardPostDto;
import com.ts.rm.domain.board.entity.BoardPost;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * BoardPost Entity ↔ DTO 변환 Mapper (MapStruct로 구현)
 */
@Mapper(componentModel = "spring")
public interface BoardPostDtoMapper {

    @Mapping(target = "postId", ignore = true)
    @Mapping(target = "topic", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "likeCount", ignore = true)
    @Mapping(target = "commentCount", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "createdByEmail", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BoardPost toEntity(BoardPostDto.CreateRequest request);

    @Mapping(target = "topicId", source = "topic.topicId")
    @Mapping(target = "topicName", source = "topic.topicName")
    @Mapping(target = "isLikedByMe", ignore = true)
    @Mapping(target = "createdById", source = "creator.accountId")
    @Mapping(target = "createdByEmail", source = "createdByEmail")
    @Mapping(target = "createdByName", source = "creator.accountName")
    @Mapping(target = "createdByAvatarStyle", source = "creator.avatarStyle")
    @Mapping(target = "createdByAvatarSeed", source = "creator.avatarSeed")
    BoardPostDto.Response toResponse(BoardPost post);

    @Mapping(target = "topicId", source = "topic.topicId")
    @Mapping(target = "contentPreview", expression = "java(truncateContent(post.getContent(), 200))")
    @Mapping(target = "createdByEmail", source = "createdByEmail")
    @Mapping(target = "createdByName", source = "creator.accountName")
    @Mapping(target = "createdByAvatarStyle", source = "creator.avatarStyle")
    @Mapping(target = "createdByAvatarSeed", source = "creator.avatarSeed")
    BoardPostDto.ListResponse toListResponse(BoardPost post);

    /**
     * 내용 미리보기 생성 (최대 길이로 자르기)
     *
     * @param content   원본 내용
     * @param maxLength 최대 길이
     * @return 잘린 내용 (초과 시 "..." 추가)
     */
    default String truncateContent(String content, int maxLength) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        // 줄바꿈을 공백으로 변환하여 한 줄로 만들기
        String singleLine = content.replaceAll("\\s+", " ").trim();
        if (singleLine.length() <= maxLength) {
            return singleLine;
        }
        return singleLine.substring(0, maxLength) + "...";
    }
}
