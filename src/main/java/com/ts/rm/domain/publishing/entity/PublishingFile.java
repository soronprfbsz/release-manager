package com.ts.rm.domain.publishing.entity;

import com.ts.rm.domain.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * PublishingFile Entity
 *
 * <p>퍼블리싱에 포함된 개별 파일 정보를 관리하는 엔티티
 * ZIP 파일 업로드 시 해제된 각 파일의 메타데이터를 저장
 */
@Entity
@Table(name = "publishing_file")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublishingFile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "publishing_file_id")
    private Long publishingFileId;

    /**
     * 소속 퍼블리싱
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publishing_id", nullable = false)
    private Publishing publishing;

    /**
     * 파일 타입 (확장자 대문자: HTML, CSS, JS, PNG 등)
     */
    @Column(name = "file_type", nullable = false, length = 20)
    private String fileType;

    /**
     * 파일명
     */
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    /**
     * 파일 경로 (publishing/ 하위 상대경로)
     * ZIP 해제 시 폴더 구조가 유지됨
     * 예: "dashboard/css/style.css"
     */
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    /**
     * 파일 크기 (bytes)
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * 파일 체크섬 (SHA-256)
     */
    @Column(length = 64)
    private String checksum;

    /**
     * 정렬 순서 (publishing 내에서)
     */
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
}
