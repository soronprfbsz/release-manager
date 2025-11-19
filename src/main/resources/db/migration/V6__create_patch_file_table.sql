-- ============================================================================
-- Release Manager: patch_file 테이블 생성
-- 설명: 패치 파일 정보 관리
-- ============================================================================

CREATE TABLE patch_file (
    patch_file_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '패치 파일 ID',
    release_version_id BIGINT NOT NULL COMMENT '릴리즈 버전 ID',
    database_type_id BIGINT NOT NULL COMMENT 'DB 타입 ID',
    file_name VARCHAR(255) NOT NULL COMMENT '파일명 (1.patch_mariadb_ddl.sql)',
    file_order INT NOT NULL COMMENT '실행 순서',
    file_path VARCHAR(500) NOT NULL COMMENT '물리적 파일 경로',
    file_size BIGINT COMMENT '파일 크기 (bytes)',
    checksum VARCHAR(64) COMMENT 'SHA-256 체크섬',
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '업로드 일시',
    uploaded_by VARCHAR(100) COMMENT '업로드 사용자',

    FOREIGN KEY (release_version_id) REFERENCES release_version(release_version_id) ON DELETE CASCADE,
    FOREIGN KEY (database_type_id) REFERENCES database_type(database_type_id) ON DELETE RESTRICT,

    UNIQUE KEY uk_patch_file (release_version_id, database_type_id, file_name),
    INDEX idx_file_order (release_version_id, database_type_id, file_order),
    INDEX idx_uploaded_at (uploaded_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='패치 파일 정보';
