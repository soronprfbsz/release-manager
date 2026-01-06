package com.ts.rm.domain.publishing.repository;

import com.ts.rm.domain.publishing.entity.PublishingFile;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * PublishingFile Repository
 */
@Repository
public interface PublishingFileRepository extends JpaRepository<PublishingFile, Long> {

    /**
     * 퍼블리싱 ID로 파일 목록 조회 (정렬순서 오름차순)
     */
    List<PublishingFile> findByPublishing_PublishingIdOrderBySortOrderAsc(Long publishingId);

    /**
     * 퍼블리싱 ID로 파일 개수 조회
     */
    long countByPublishing_PublishingId(Long publishingId);

    /**
     * 퍼블리싱 ID로 전체 삭제
     */
    void deleteAllByPublishing_PublishingId(Long publishingId);

    /**
     * 파일 경로로 존재 여부 확인
     */
    boolean existsByFilePath(String filePath);

    /**
     * 파일 타입별 조회
     */
    List<PublishingFile> findByPublishing_PublishingIdAndFileTypeOrderBySortOrderAsc(
            Long publishingId, String fileType);
}
