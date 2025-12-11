package com.ts.rm.domain.terminal.repository;

import com.ts.rm.domain.terminal.entity.Terminal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 터미널 Repository
 */
@Repository
public interface TerminalRepository extends JpaRepository<Terminal, String> {

    /**
     * 터미널 ID로 터미널 조회
     *
     * @param terminalId 터미널 ID
     * @return 터미널 정보
     */
    Optional<Terminal> findByTerminalId(String terminalId);
}
