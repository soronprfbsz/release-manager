package com.ts.rm.domain.board.entity;

import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * BoardCommentLike 복합 키 클래스
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoardCommentLikeId implements Serializable {

    private Long commentId;
    private Long accountId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoardCommentLikeId that = (BoardCommentLikeId) o;
        return Objects.equals(commentId, that.commentId) && Objects.equals(accountId, that.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commentId, accountId);
    }
}
