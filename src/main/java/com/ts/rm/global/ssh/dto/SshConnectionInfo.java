package com.ts.rm.global.ssh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SSH 연결 정보
 * <p>
 * 범용 SSH 연결 정보입니다.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SshConnectionInfo {
    /**
     * 호스트 주소
     */
    private String host;

    /**
     * SSH 포트
     */
    private Integer port;

    /**
     * 사용자명
     */
    private String username;

    /**
     * 비밀번호
     */
    private String password;

    /**
     * 연결 타임아웃 (밀리초)
     */
    @Builder.Default
    private Integer timeout = 30000;

    /**
     * 호스트 키 검증 여부
     */
    @Builder.Default
    private Boolean strictHostKeyChecking = false;
}
