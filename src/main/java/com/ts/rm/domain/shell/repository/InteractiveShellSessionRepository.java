package com.ts.rm.domain.shell.repository;

import com.ts.rm.domain.shell.entity.InteractiveShellSession;
import com.ts.rm.domain.shell.enums.ShellStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 대화형 셸 세션 Repository
 */
@Repository
public interface InteractiveShellSessionRepository extends JpaRepository<InteractiveShellSession, Long> {

    /**
     * 셸 세션 식별자로 세션 조회
     *
     * @param shellSessionIdentifier 셸 세션 식별자
     * @return 세션 정보
     */
    Optional<InteractiveShellSession> findByShellSessionIdentifier(String shellSessionIdentifier);

    /**
     * 소유자 이메일로 세션 목록 조회
     *
     * @param ownerEmail 소유자 이메일
     * @return 세션 목록
     */
    List<InteractiveShellSession> findByOwnerEmailOrderByCreatedAtDesc(String ownerEmail);

    /**
     * 특정 시각 이전에 만료된 세션 조회 (만료된 세션 정리용)
     *
     * @param expiresAt 만료 시각
     * @return 만료된 세션 목록
     */
    List<InteractiveShellSession> findByExpiresAtBefore(LocalDateTime expiresAt);

    /**
     * 특정 상태의 세션 조회
     *
     * @param status 셸 상태
     * @return 세션 목록
     */
    List<InteractiveShellSession> findByStatus(ShellStatus status);
}
