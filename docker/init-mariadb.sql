-- MariaDB 초기화 스크립트
-- MARIADB_DATABASE 환경변수로 이미 'infraeye' 데이터베이스가 생성됨 (utf8mb3_general_ci)
-- MARIADB_USER, MARIADB_PASSWORD 환경변수로 이미 'infraeye' 사용자가 생성되고
-- 'infraeye' 데이터베이스에 대한 모든 권한이 부여됨

-- 수동으로 데이터베이스 생성이 필요한 경우를 대비한 명령어 (주석 처리)
-- CREATE DATABASE IF NOT EXISTS infraeye CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci;

-- root@'%' 계정에 모든 권한 부여 (기존 데이터 restore 시 권한 손상 문제 해결)
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;

-- ts_dev 사용자에게 모든 데이터베이스에 대한 모든 권한 부여
GRANT ALL PRIVILEGES ON *.* TO 'ts_dev'@'%' WITH GRANT OPTION;
-- 권한 적용
FLUSH PRIVILEGES;

