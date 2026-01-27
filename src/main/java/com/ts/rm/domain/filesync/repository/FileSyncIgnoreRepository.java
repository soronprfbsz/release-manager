package com.ts.rm.domain.filesync.repository;

import com.ts.rm.domain.filesync.entity.FileSyncIgnore;
import com.ts.rm.domain.filesync.enums.FileSyncTarget;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 파일 동기화 무시 목록 Repository
 */
@Repository
public interface FileSyncIgnoreRepository extends JpaRepository<FileSyncIgnore, Long>,
        FileSyncIgnoreRepositoryCustom {

    /**
     * 파일 경로와 대상 유형으로 조회
     */
    Optional<FileSyncIgnore> findByFilePathAndTargetType(String filePath, FileSyncTarget targetType);

    /**
     * 파일 경로와 대상 유형 존재 여부 확인
     */
    boolean existsByFilePathAndTargetType(String filePath, FileSyncTarget targetType);

    /**
     * 대상 유형별 무시 목록 조회
     */
    List<FileSyncIgnore> findByTargetTypeOrderByCreatedAtDesc(FileSyncTarget targetType);

    /**
     * 파일 경로와 대상 유형으로 삭제
     */
    void deleteByFilePathAndTargetType(String filePath, FileSyncTarget targetType);

    /**
     * 전체 조회 (생성일시 내림차순)
     */
    List<FileSyncIgnore> findAllByOrderByCreatedAtDesc();
}
