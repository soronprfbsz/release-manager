package com.ts.rm.domain.job.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ts.rm.domain.job.entity.BackupFile;
import com.ts.rm.domain.job.entity.QBackupFile;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

/**
 * BackupFile Custom Repository Implementation
 */
@Repository
@RequiredArgsConstructor
public class BackupFileRepositoryImpl implements BackupFileRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<BackupFile> searchBackupFiles(
            String fileCategory,
            String fileType,
            String fileName,
            Pageable pageable) {

        QBackupFile backupFile = QBackupFile.backupFile;

        // 전체 개수 조회 (별도 count 쿼리)
        Long total = queryFactory
                .select(backupFile.count())
                .from(backupFile)
                .where(
                        fileCategoryEq(fileCategory),
                        fileTypeEq(fileType),
                        fileNameContains(fileName)
                )
                .fetchOne();

        if (total == null || total == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // 페이징 적용된 데이터 조회
        List<BackupFile> content = queryFactory
                .selectFrom(backupFile)
                .where(
                        fileCategoryEq(fileCategory),
                        fileTypeEq(fileType),
                        fileNameContains(fileName)
                )
                .orderBy(backupFile.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression fileCategoryEq(String fileCategory) {
        return StringUtils.hasText(fileCategory)
                ? QBackupFile.backupFile.fileCategory.eq(fileCategory.toUpperCase())
                : null;
    }

    private BooleanExpression fileTypeEq(String fileType) {
        return StringUtils.hasText(fileType)
                ? QBackupFile.backupFile.fileType.eq(fileType.toUpperCase())
                : null;
    }

    private BooleanExpression fileNameContains(String fileName) {
        return StringUtils.hasText(fileName)
                ? QBackupFile.backupFile.fileName.contains(fileName)
                : null;
    }
}
