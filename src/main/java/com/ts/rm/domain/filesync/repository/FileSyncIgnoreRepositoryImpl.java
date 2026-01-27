package com.ts.rm.domain.filesync.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ts.rm.domain.filesync.entity.QFileSyncIgnore;
import com.ts.rm.domain.filesync.enums.FileSyncTarget;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * FileSyncIgnore Repository Custom Implementation
 *
 * <p>QueryDSL을 사용한 파일 동기화 무시 목록 쿼리 구현
 */
@Repository
@RequiredArgsConstructor
public class FileSyncIgnoreRepositoryImpl implements FileSyncIgnoreRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QFileSyncIgnore fileSyncIgnore = QFileSyncIgnore.fileSyncIgnore;

    @Override
    public Set<String> findIgnoredPathsByTargetType(FileSyncTarget targetType) {
        List<String> paths = queryFactory
                .select(fileSyncIgnore.filePath)
                .from(fileSyncIgnore)
                .where(fileSyncIgnore.targetType.eq(targetType))
                .fetch();
        return new HashSet<>(paths);
    }

    @Override
    public Set<String> findIgnoredPathsByTargetTypes(List<FileSyncTarget> targetTypes) {
        List<String> paths = queryFactory
                .select(fileSyncIgnore.filePath)
                .from(fileSyncIgnore)
                .where(fileSyncIgnore.targetType.in(targetTypes))
                .fetch();
        return new HashSet<>(paths);
    }
}
