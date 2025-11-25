package com.ts.rm.domain.refreshtoken.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

/**
 * Refresh Token Redis 엔티티
 * - TTL 기반 자동 만료
 * - accountId로 인덱싱하여 빠른 조회
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash(value = "refreshToken")
public class RefreshToken implements Serializable {

    @Id
    private String token;  // Refresh Token 값을 ID로 사용

    @Indexed
    private Long accountId;

    private String email;

    private String accountName;

    private String role;

    private LocalDateTime createdAt;

    @TimeToLive
    private Long ttl;  // 초 단위 TTL (자동 만료)

    /**
     * 토큰 만료 여부 확인
     * Redis TTL이 자동 관리하지만, 명시적 체크용
     */
    public boolean isExpired() {
        return ttl != null && ttl <= 0;
    }

    /**
     * 만료 시간 계산
     */
    public LocalDateTime getExpiresAt() {
        if (createdAt == null || ttl == null) {
            return null;
        }
        return createdAt.plusSeconds(ttl);
    }
}
