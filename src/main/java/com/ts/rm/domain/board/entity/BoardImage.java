package com.ts.rm.domain.board.entity;

import com.ts.rm.domain.account.entity.Account;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * BoardImage Entity
 *
 * <p>게시판 이미지 메타데이터 테이블 (유령 파일 관리용)
 */
@Entity
@Table(name = "board_image", indexes = {
        @Index(name = "idx_bi_post_id", columnList = "post_id"),
        @Index(name = "idx_bi_uploaded_by", columnList = "uploaded_by"),
        @Index(name = "idx_bi_uploaded_at", columnList = "uploaded_at"),
        @Index(name = "idx_bi_file_path", columnList = "file_path")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private BoardPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private Account uploader;

    @Column(name = "uploaded_by_email", length = 100)
    private String uploadedByEmail;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    /**
     * 게시글 연결
     */
    public void linkToPost(BoardPost post) {
        this.post = post;
    }

    /**
     * 게시글 연결 해제
     */
    public void unlinkFromPost() {
        this.post = null;
    }

    /**
     * 미사용 이미지 여부 (게시글에 연결되지 않음)
     */
    public boolean isOrphaned() {
        return this.post == null;
    }
}
