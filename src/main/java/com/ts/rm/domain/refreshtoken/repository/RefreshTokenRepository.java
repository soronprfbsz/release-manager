package com.ts.rm.domain.refreshtoken.repository;

import com.ts.rm.domain.refreshtoken.entity.RefreshToken;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Refresh Token Redis Repository
 */
@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {

    /**
     * 토큰으로 Refresh Token 조회
     *
     * @param token Refresh Token 문자열
     * @return RefreshToken 엔티티
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * 계정 ID로 Refresh Token 조회
     *
     * @param accountId 계정 ID
     * @return RefreshToken 엔티티
     */
    Optional<RefreshToken> findByAccountId(Long accountId);

    /**
     * 계정 ID로 Refresh Token 삭제
     *
     * @param accountId 계정 ID
     */
    void deleteByAccountId(Long accountId);

    /**
     * 토큰 존재 여부 확인
     *
     * @param token Refresh Token 문자열
     * @return 존재하면 true
     */
    boolean existsByToken(String token);
}
