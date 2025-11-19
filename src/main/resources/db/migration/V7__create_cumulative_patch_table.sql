-- ============================================================================
-- Release Manager: cumulative_patch 테이블 생성
-- 설명: 누적 패치 생성 이력 관리
-- ============================================================================

CREATE TABLE cumulative_patch (
    cumulative_patch_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '누적 패치 ID',
    release_type_id BIGINT NOT NULL COMMENT '릴리즈 타입 ID',
    customer_id BIGINT NULL COMMENT '고객사 ID',
    from_version VARCHAR(50) NOT NULL COMMENT '시작 버전',
    to_version VARCHAR(50) NOT NULL COMMENT '종료 버전',
    patch_name VARCHAR(100) NOT NULL COMMENT '패치명 (from-1.0.0)',
    output_path VARCHAR(500) NOT NULL COMMENT '생성된 패치 경로',
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    generated_by VARCHAR(100) NOT NULL COMMENT '생성자',
    status VARCHAR(20) DEFAULT 'SUCCESS' COMMENT '생성 상태 (SUCCESS/FAILED/IN_PROGRESS)',
    error_message TEXT COMMENT '에러 메시지 (실패 시)',

    FOREIGN KEY (release_type_id) REFERENCES release_type(release_type_id) ON DELETE RESTRICT,
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE RESTRICT,

    INDEX idx_versions (from_version, to_version),
    INDEX idx_generated_at (generated_at),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='누적 패치 생성 이력';
