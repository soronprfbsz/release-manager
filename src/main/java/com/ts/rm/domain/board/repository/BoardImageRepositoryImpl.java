package com.ts.rm.domain.board.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ts.rm.domain.board.entity.QBoardImage;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * BoardImage Repository Custom Implementation
 *
 * <p>QueryDSL을 사용한 커스텀 쿼리 구현
 */
@Repository
@RequiredArgsConstructor
public class BoardImageRepositoryImpl implements BoardImageRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QBoardImage image = QBoardImage.boardImage;

    @Override
    public long deleteOrphanedImagesBefore(LocalDateTime threshold) {
        return queryFactory
                .delete(image)
                .where(
                        image.post.isNull(),
                        image.uploadedAt.lt(threshold)
                )
                .execute();
    }

    @Override
    public long unlinkImagesFromPost(Long postId) {
        return queryFactory
                .update(image)
                .setNull(image.post)
                .where(image.post.postId.eq(postId))
                .execute();
    }
}
