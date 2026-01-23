package com.ts.rm.domain.board.repository;

import com.ts.rm.domain.board.entity.BoardImage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * BoardImage Repository
 */
@Repository
public interface BoardImageRepository extends JpaRepository<BoardImage, Long> {

    /**
     * 파일 경로로 이미지 조회
     */
    Optional<BoardImage> findByFilePath(String filePath);

    /**
     * 파일명으로 이미지 조회
     */
    Optional<BoardImage> findByFileName(String fileName);

    /**
     * 게시글 ID로 연결된 이미지 목록 조회
     */
    List<BoardImage> findByPostPostId(Long postId);

    /**
     * 미사용 이미지 목록 조회 (post_id IS NULL AND 특정 시간 이전 업로드)
     */
    List<BoardImage> findByPostIsNullAndUploadedAtBefore(LocalDateTime threshold);

    /**
     * 파일 경로 목록으로 이미지 조회
     */
    List<BoardImage> findByFilePathIn(List<String> filePaths);

    /**
     * 게시글에 연결된 이미지 수 조회
     */
    long countByPostPostId(Long postId);

    /**
     * 미사용 이미지 삭제 (배치 작업용)
     */
    @Modifying
    @Query("DELETE FROM BoardImage bi WHERE bi.post IS NULL AND bi.uploadedAt < :threshold")
    int deleteOrphanedImagesBefore(@Param("threshold") LocalDateTime threshold);

    /**
     * 게시글의 이미지 연결 해제 (게시글 삭제 전 호출)
     */
    @Modifying
    @Query("UPDATE BoardImage bi SET bi.post = NULL WHERE bi.post.postId = :postId")
    int unlinkImagesFromPost(@Param("postId") Long postId);
}
