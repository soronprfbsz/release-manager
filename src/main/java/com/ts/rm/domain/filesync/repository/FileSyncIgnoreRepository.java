package com.ts.rm.domain.filesync.repository;

import com.ts.rm.domain.filesync.entity.FileSyncIgnore;
import com.ts.rm.domain.filesync.enums.FileSyncTarget;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 파일 동기화 무시 목록 Repository
 */
@Repository
public interface FileSyncIgnoreRepository extends JpaRepository<FileSyncIgnore, Long> {

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
     * 대상 유형별 무시된 파일 경로 목록 조회 (분석 시 필터링용)
     */
    @Query("SELECT f.filePath FROM FileSyncIgnore f WHERE f.targetType = :targetType")
    Set<String> findIgnoredPathsByTargetType(@Param("targetType") FileSyncTarget targetType);

    /**
     * 여러 대상 유형의 무시된 파일 경로 목록 조회
     */
    @Query("SELECT f.filePath FROM FileSyncIgnore f WHERE f.targetType IN :targetTypes")
    Set<String> findIgnoredPathsByTargetTypes(@Param("targetTypes") List<FileSyncTarget> targetTypes);

    /**
     * 파일 경로와 대상 유형으로 삭제
     */
    void deleteByFilePathAndTargetType(String filePath, FileSyncTarget targetType);

    /**
     * 전체 조회 (생성일시 내림차순)
     */
    List<FileSyncIgnore> findAllByOrderByCreatedAtDesc();
}
