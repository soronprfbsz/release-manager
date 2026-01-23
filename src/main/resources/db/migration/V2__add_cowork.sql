-- =========================================================
-- V2: 인프라 메뉴 → 공유 메뉴로 변경 + 협업 메뉴 추가
-- =========================================================
-- 변경 전: support > infrastructure(인프라) > infrastructure_resources(리소스)
-- 변경 후: support > sharing(공유) > sharing_resources(리소스), sharing_cowork(협업)
-- =========================================================

-- =========================================================
-- Step 1: 새 메뉴 추가 (sharing, sharing_resources, sharing_cowork)
-- =========================================================

-- 2depth 메뉴 - 공유 (기존 infrastructure 대체)
INSERT INTO menu (menu_id, menu_name, menu_url, icon, is_icon_visible, description, is_description_visible, is_line_break, menu_order) VALUES
('sharing', '공유', NULL, NULL, FALSE, '공유 리소스 및 협업 등의 기능을 제공합니다.', FALSE, FALSE, 2);

-- 3depth 메뉴 - 리소스 (기존 infrastructure_resources 대체)
INSERT INTO menu (menu_id, menu_name, menu_url, icon, is_icon_visible, description, is_description_visible, is_line_break, menu_order) VALUES
('sharing_resources', '리소스', 'support/sharing/resources', 'layers', TRUE, '리소스 정보를 관리 및 제공합니다.', TRUE, FALSE, 1);

-- 3depth 메뉴 - 협업 (신규)
INSERT INTO menu (menu_id, menu_name, menu_url, icon, is_icon_visible, description, is_description_visible, is_line_break, menu_order) VALUES
('sharing_cowork', '협업', 'support/sharing/cowork', 'message-circle', TRUE, '협업을 위한 커뮤니케이션 기능을 제공합니다.', TRUE, FALSE, 2);

-- =========================================================
-- Step 2: menu_hierarchy - 새 관계 추가
-- =========================================================

-- 자기 자신 관계 (depth=0)
INSERT INTO menu_hierarchy (ancestor, descendant, depth) VALUES
('sharing', 'sharing', 0),
('sharing_resources', 'sharing_resources', 0),
('sharing_cowork', 'sharing_cowork', 0);

-- 부모-자식 관계 (depth=1) - support > sharing
INSERT INTO menu_hierarchy (ancestor, descendant, depth) VALUES
('support', 'sharing', 1);

-- 부모-자식 관계 (depth=1) - sharing > sharing_resources, cowork
INSERT INTO menu_hierarchy (ancestor, descendant, depth) VALUES
('sharing', 'sharing_resources', 1),
('sharing', 'sharing_cowork', 1);

-- 조상-손자 관계 (depth=2) - support > sharing > sharing_resources, cowork
INSERT INTO menu_hierarchy (ancestor, descendant, depth) VALUES
('support', 'sharing_resources', 2),
('support', 'sharing_cowork', 2);

-- =========================================================
-- Step 3: menu_role - 새 권한 추가
-- =========================================================

-- ADMIN
INSERT INTO menu_role (menu_id, role) VALUES
('sharing', 'ADMIN'),
('sharing_resources', 'ADMIN'),
('sharing_cowork', 'ADMIN');

-- DEVELOPER
INSERT INTO menu_role (menu_id, role) VALUES
('sharing', 'DEVELOPER'),
('sharing_resources', 'DEVELOPER'),
('sharing_cowork', 'DEVELOPER');

-- USER
INSERT INTO menu_role (menu_id, role) VALUES
('sharing', 'USER'),
('sharing_resources', 'USER'),
('sharing_cowork', 'USER');

-- GUEST: 접근 불가 (support 하위 메뉴에 권한 없음)

-- =========================================================
-- Step 4: 기존 infrastructure 관련 데이터 삭제
-- =========================================================

-- menu_role 삭제
DELETE FROM menu_role WHERE menu_id IN ('infrastructure', 'infrastructure_resources');

-- menu_hierarchy 삭제
DELETE FROM menu_hierarchy WHERE ancestor IN ('infrastructure', 'infrastructure_resources')
                              OR descendant IN ('infrastructure', 'infrastructure_resources');

-- menu 삭제
DELETE FROM menu WHERE menu_id IN ('infrastructure', 'infrastructure_resources');

-- =========================================================
-- Step 5: 협업 게시판 테이블 생성
-- =========================================================

-- ---------------------------------------------------------
-- 게시판 토픽 (카테고리)
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS board_topic (
    topic_id VARCHAR(50) PRIMARY KEY COMMENT '토픽 ID (NOTICE, QNA, SUGGESTION 등)',
    topic_name VARCHAR(100) NOT NULL COMMENT '토픽명',
    description VARCHAR(500) COMMENT '토픽 설명',
    icon VARCHAR(50) COMMENT '아이콘 (Lucide React 아이콘명)',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '정렬 순서',
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성 여부',
    created_by BIGINT COMMENT '생성자 계정 ID',
    created_by_email VARCHAR(100) COMMENT '생성자 이메일',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    INDEX idx_bt_sort_order (sort_order),
    INDEX idx_bt_is_enabled (is_enabled),

    CONSTRAINT fk_board_topic_created_by FOREIGN KEY (created_by)
        REFERENCES account(account_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시판 토픽 (카테고리)';

-- ---------------------------------------------------------
-- 게시글
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS board_post (
    post_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '게시글 ID',
    topic_id VARCHAR(50) NOT NULL COMMENT '토픽 ID',
    title VARCHAR(200) NOT NULL COMMENT '제목',
    content MEDIUMTEXT NOT NULL COMMENT '본문 (마크다운)',
    thumbnail_url VARCHAR(500) COMMENT '썸네일 이미지 URL',
    view_count INT NOT NULL DEFAULT 0 COMMENT '조회수',
    like_count INT NOT NULL DEFAULT 0 COMMENT '좋아요 수 (캐시)',
    comment_count INT NOT NULL DEFAULT 0 COMMENT '댓글 수 (캐시)',
    is_pinned BOOLEAN NOT NULL DEFAULT FALSE COMMENT '상단 고정 여부',
    is_published BOOLEAN NOT NULL DEFAULT TRUE COMMENT '발행 여부 (임시저장=FALSE)',
    created_by BIGINT COMMENT '작성자 계정 ID',
    created_by_email VARCHAR(100) COMMENT '작성자 이메일',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    INDEX idx_bp_topic_id (topic_id),
    INDEX idx_bp_is_published (is_published),
    INDEX idx_bp_is_pinned (is_pinned),
    INDEX idx_bp_created_at (created_at),
    INDEX idx_bp_like_count (like_count),
    INDEX idx_bp_created_by (created_by),

    CONSTRAINT fk_board_post_topic FOREIGN KEY (topic_id)
        REFERENCES board_topic(topic_id) ON DELETE RESTRICT,
    CONSTRAINT fk_board_post_created_by FOREIGN KEY (created_by)
        REFERENCES account(account_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시글';

-- ---------------------------------------------------------
-- 댓글 (대댓글 지원 - parent_comment_id)
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS board_comment (
    comment_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '댓글 ID',
    post_id BIGINT NOT NULL COMMENT '게시글 ID',
    parent_comment_id BIGINT COMMENT '부모 댓글 ID (NULL이면 최상위 댓글)',
    content TEXT NOT NULL COMMENT '댓글 내용',
    like_count INT NOT NULL DEFAULT 0 COMMENT '좋아요 수 (캐시)',
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '삭제 여부 (soft delete)',
    created_by BIGINT COMMENT '작성자 계정 ID',
    created_by_email VARCHAR(100) COMMENT '작성자 이메일',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    INDEX idx_bc_post_id (post_id),
    INDEX idx_bc_parent_comment_id (parent_comment_id),
    INDEX idx_bc_created_at (created_at),
    INDEX idx_bc_created_by (created_by),
    INDEX idx_bc_is_deleted (is_deleted),

    CONSTRAINT fk_board_comment_post FOREIGN KEY (post_id)
        REFERENCES board_post(post_id) ON DELETE CASCADE,
    CONSTRAINT fk_board_comment_parent FOREIGN KEY (parent_comment_id)
        REFERENCES board_comment(comment_id) ON DELETE CASCADE,
    CONSTRAINT fk_board_comment_created_by FOREIGN KEY (created_by)
        REFERENCES account(account_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시글 댓글';

-- ---------------------------------------------------------
-- 게시글 좋아요
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS board_post_like (
    post_id BIGINT NOT NULL COMMENT '게시글 ID',
    account_id BIGINT NOT NULL COMMENT '계정 ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '좋아요 일시',

    PRIMARY KEY (post_id, account_id),

    INDEX idx_bpl_account_id (account_id),

    CONSTRAINT fk_board_post_like_post FOREIGN KEY (post_id)
        REFERENCES board_post(post_id) ON DELETE CASCADE,
    CONSTRAINT fk_board_post_like_account FOREIGN KEY (account_id)
        REFERENCES account(account_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시글 좋아요';

-- ---------------------------------------------------------
-- 댓글 좋아요
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS board_comment_like (
    comment_id BIGINT NOT NULL COMMENT '댓글 ID',
    account_id BIGINT NOT NULL COMMENT '계정 ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '좋아요 일시',

    PRIMARY KEY (comment_id, account_id),

    INDEX idx_bcl_account_id (account_id),

    CONSTRAINT fk_board_comment_like_comment FOREIGN KEY (comment_id)
        REFERENCES board_comment(comment_id) ON DELETE CASCADE,
    CONSTRAINT fk_board_comment_like_account FOREIGN KEY (account_id)
        REFERENCES account(account_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='댓글 좋아요';

-- ---------------------------------------------------------
-- 게시판 이미지 메타데이터 (유령 파일 관리용)
-- ---------------------------------------------------------
-- 사용 방식:
--   - 이미지 업로드 시 post_id = NULL로 생성
--   - 게시글 저장 시 content 파싱하여 post_id 연결
--   - 배치 작업: post_id IS NULL AND uploaded_at < 24시간 전 → 삭제
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS board_image (
    image_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '이미지 ID',
    file_name VARCHAR(255) NOT NULL COMMENT '저장된 파일명 (UUID_원본파일명)',
    original_file_name VARCHAR(255) NOT NULL COMMENT '원본 파일명',
    file_path VARCHAR(500) NOT NULL COMMENT '상대 경로 (board/images/2026/01/xxx.png)',
    file_size BIGINT NOT NULL COMMENT '파일 크기 (bytes)',
    mime_type VARCHAR(100) COMMENT 'MIME 타입',
    post_id BIGINT COMMENT '연결된 게시글 ID (NULL이면 미사용 상태)',
    uploaded_by BIGINT COMMENT '업로드한 계정 ID',
    uploaded_by_email VARCHAR(100) COMMENT '업로드한 이메일',
    uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '업로드 일시',

    INDEX idx_bi_post_id (post_id),
    INDEX idx_bi_uploaded_by (uploaded_by),
    INDEX idx_bi_uploaded_at (uploaded_at),
    INDEX idx_bi_file_path (file_path),

    CONSTRAINT fk_board_image_post FOREIGN KEY (post_id)
        REFERENCES board_post(post_id) ON DELETE SET NULL,
    CONSTRAINT fk_board_image_uploaded_by FOREIGN KEY (uploaded_by)
        REFERENCES account(account_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시판 이미지 메타데이터';

-- ---------------------------------------------------------
-- 게시글 조회 이력 (동일 계정 조회수 중복 증가 방지)
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS board_post_view (
    post_id BIGINT NOT NULL COMMENT '게시글 ID',
    account_id BIGINT NOT NULL COMMENT '계정 ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '최초 조회 일시',

    PRIMARY KEY (post_id, account_id),

    INDEX idx_bpv_account_id (account_id),

    CONSTRAINT fk_board_post_view_post FOREIGN KEY (post_id)
        REFERENCES board_post(post_id) ON DELETE CASCADE,
    CONSTRAINT fk_board_post_view_account FOREIGN KEY (account_id)
        REFERENCES account(account_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시글 조회 이력';

-- =========================================================
-- Step 6: 초기 토픽 데이터 삽입
-- =========================================================

INSERT INTO board_topic (topic_id, topic_name, description, icon, sort_order, is_enabled) VALUES
('NOTICE', '공지사항', '공지 및 안내사항', 'megaphone', 1, TRUE),
('QNA', 'QnA', '프로젝트 관련 질문과 답변', 'message-circle-question', 2, TRUE);
