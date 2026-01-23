package com.ts.rm.domain.board.entity;

import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * BoardPostLike 복합 키 클래스
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoardPostLikeId implements Serializable {

    private Long postId;
    private Long accountId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoardPostLikeId that = (BoardPostLikeId) o;
        return Objects.equals(postId, that.postId) && Objects.equals(accountId, that.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postId, accountId);
    }
}
