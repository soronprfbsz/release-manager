package com.ts.rm.domain.board.entity;

import com.ts.rm.domain.account.entity.Account;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
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
 * BoardPostView Entity
 *
 * <p>게시글 조회 이력 테이블 (복합 키: post_id + account_id)
 * <p>동일 계정의 중복 조회수 증가를 방지하기 위한 테이블
 */
@Entity
@Table(name = "board_post_view", indexes = {
        @Index(name = "idx_bpv_account_id", columnList = "account_id")
})
@IdClass(BoardPostViewId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardPostView {

    @Id
    @Column(name = "post_id")
    private Long postId;

    @Id
    @Column(name = "account_id")
    private Long accountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private BoardPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    private Account account;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
