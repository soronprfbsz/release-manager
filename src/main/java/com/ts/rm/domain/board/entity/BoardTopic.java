package com.ts.rm.domain.board.entity;

import com.ts.rm.domain.account.entity.Account;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
import org.hibernate.annotations.UpdateTimestamp;

/**
 * BoardTopic Entity
 *
 * <p>게시판 토픽 (카테고리) 테이블 - 공지사항, QnA, 개선제안 등
 */
@Entity
@Table(name = "board_topic", indexes = {
        @Index(name = "idx_bt_sort_order", columnList = "sort_order"),
        @Index(name = "idx_bt_is_enabled", columnList = "is_enabled")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardTopic {

    @Id
    @Column(name = "topic_id", length = 50)
    private String topicId;

    @Column(name = "topic_name", nullable = false, length = 100)
    private String topicName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private Boolean isEnabled = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Account creator;

    @Column(name = "created_by_email", length = 100)
    private String createdByEmail;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
