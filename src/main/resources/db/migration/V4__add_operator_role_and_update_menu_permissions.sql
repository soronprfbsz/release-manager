-- =========================================================
-- V4: OPERATOR 권한 추가 및 메뉴 권한 재구성
-- =========================================================
-- 1. code 테이블에 OPERATOR 역할 추가 (sort_order 재정렬)
-- 2. menu_role 테이블에 OPERATOR 권한 추가
-- 3. 기존 역할의 메뉴 권한 변경
--    - operation_filesync: DEVELOPER, USER 제거 (ADMIN만 접근)
--    - operation_history: DEVELOPER 제거, OPERATOR 추가
-- =========================================================

-- =========================================================
-- Step 1: code 테이블 - OPERATOR 역할 추가
-- =========================================================

-- 기존 sort_order 재정렬: DEVELOPER(2→3), USER(3→4), GUEST(4→5)
UPDATE code SET sort_order = 5 WHERE code_type_id = 'ACCOUNT_ROLE' AND code_id = 'GUEST';
UPDATE code SET sort_order = 4 WHERE code_type_id = 'ACCOUNT_ROLE' AND code_id = 'USER';
UPDATE code SET sort_order = 3 WHERE code_type_id = 'ACCOUNT_ROLE' AND code_id = 'DEVELOPER';

-- OPERATOR 추가 (sort_order: 2, ADMIN 다음)
INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled)
VALUES ('ACCOUNT_ROLE', 'OPERATOR', '운영자', '운영자 권한', 2, TRUE);

-- =========================================================
-- Step 2: 기존 역할의 메뉴 권한 변경
-- =========================================================

-- 파일 동기화: DEVELOPER, USER 권한 제거 (ADMIN만 접근 가능)
DELETE FROM menu_role WHERE menu_id = 'operation_filesync' AND role IN ('DEVELOPER', 'USER');

-- 운영 이력: DEVELOPER 권한 제거 (OPERATOR, ADMIN만 접근 가능)
DELETE FROM menu_role WHERE menu_id = 'operation_history' AND role = 'DEVELOPER';

-- =========================================================
-- Step 3: OPERATOR 메뉴 권한 추가
-- =========================================================

-- OPERATOR: DEVELOPER와 유사하나, 파일 동기화(X), 스케줄러(X) 제외
-- 운영 이력은 OPERATOR만 접근 가능 (DEVELOPER는 불가)
INSERT INTO menu_role (menu_id, role) VALUES
-- 1depth
('version_management', 'OPERATOR'),
('patch_management', 'OPERATOR'),
('operation_management', 'OPERATOR'),
('support', 'OPERATOR'),
-- 2depth - 운영 관리
('operation_projects', 'OPERATOR'),
('operation_customers', 'OPERATOR'),
('operation_department', 'OPERATOR'),
('operation_accounts', 'OPERATOR'),
('operation_history', 'OPERATOR'),
-- 2depth - 업무 지원
('remote_jobs', 'OPERATOR'),
('sharing', 'OPERATOR'),
-- 3depth - 원격 작업
('remote_mariadb', 'OPERATOR'),
('remote_terminal', 'OPERATOR'),
-- 3depth - 공유
('sharing_resources', 'OPERATOR'),
('sharing_cowork', 'OPERATOR');

-- =========================================================
-- Rollback (수동 실행 시 사용)
-- =========================================================
-- DELETE FROM menu_role WHERE role = 'OPERATOR';
-- INSERT INTO menu_role (menu_id, role) VALUES ('operation_filesync', 'DEVELOPER');
-- INSERT INTO menu_role (menu_id, role) VALUES ('operation_filesync', 'USER');
-- INSERT INTO menu_role (menu_id, role) VALUES ('operation_history', 'DEVELOPER');
-- DELETE FROM code WHERE code_type_id = 'ACCOUNT_ROLE' AND code_id = 'OPERATOR';
-- UPDATE code SET sort_order = 2 WHERE code_type_id = 'ACCOUNT_ROLE' AND code_id = 'DEVELOPER';
-- UPDATE code SET sort_order = 3 WHERE code_type_id = 'ACCOUNT_ROLE' AND code_id = 'USER';
-- UPDATE code SET sort_order = 4 WHERE code_type_id = 'ACCOUNT_ROLE' AND code_id = 'GUEST';
