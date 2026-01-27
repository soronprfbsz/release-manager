package com.ts.rm.domain.board.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ts.rm.domain.board.entity.QBoardPostView;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * BoardPostView Repository Custom Implementation
 *
 * <p>QueryDSL을 사용한 커스텀 쿼리 구현
 */
@Repository
@RequiredArgsConstructor
public class BoardPostViewRepositoryImpl implements BoardPostViewRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QBoardPostView view = QBoardPostView.boardPostView;

    @Override
    public long deleteByCreatedAtBefore(LocalDateTime cutoffDate) {
        return queryFactory
                .delete(view)
                .where(view.createdAt.lt(cutoffDate))
                .execute();
    }
}
