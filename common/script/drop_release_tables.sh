#!/bin/bash

# =========================================================
# Release Manager 테이블 강제 삭제 스크립트
# 작성일: 2025-11-20
# 설명: MariaDB release_manager 데이터베이스의 릴리즈 테이블 삭제
# =========================================================

# 환경 변수 로드 (.env 파일에서)
if [ -f "../../.env" ]; then
    export $(cat ../../.env | grep -v '^#' | xargs)
else
    echo "❌ .env 파일을 찾을 수 없습니다."
    exit 1
fi

# MariaDB 접속 정보
DB_HOST="${MARIADB_HOST:-localhost}"
DB_PORT="${MARIADB_PORT:-13306}"
DB_NAME="${MARIADB_DATABASE:-release_manager}"
DB_USER="${MARIADB_USERNAME:-root}"
DB_PASS="${MARIADB_PASSWORD}"

echo "==========================================================="
echo "Release Manager 테이블 삭제 시작"
echo "==========================================================="
echo "Host: $DB_HOST:$DB_PORT"
echo "Database: $DB_NAME"
echo "User: $DB_USER"
echo ""

# 사용자 확인
read -p "⚠️  모든 릴리즈 관련 테이블을 삭제하시겠습니까? (yes/no): " confirm
if [ "$confirm" != "yes" ]; then
    echo "❌ 작업이 취소되었습니다."
    exit 0
fi

# SQL 스크립트 실행
echo ""
echo "📋 테이블 삭제 중..."
mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME <<EOF
-- 외래키 제약조건이 있는 테이블을 역순으로 삭제
DROP TABLE IF EXISTS release_file;
DROP TABLE IF EXISTS cumulative_patch_history;
DROP TABLE IF EXISTS release_version;
DROP TABLE IF EXISTS customer;

-- Code 테이블의 RELEASE_TYPE, DATABASE_TYPE 데이터 삭제
DELETE FROM code WHERE code_type_id IN ('RELEASE_TYPE', 'DATABASE_TYPE');
DELETE FROM code_type WHERE code_type_id IN ('RELEASE_TYPE', 'DATABASE_TYPE');

-- Flyway 히스토리에서 V2 마이그레이션 기록 삭제
DELETE FROM flyway_schema_history WHERE version = '2';
EOF

if [ $? -eq 0 ]; then
    echo "✅ 테이블 삭제 완료"
    echo ""
    echo "📋 남아있는 테이블 확인:"
    mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME -e "SHOW TABLES;"
else
    echo "❌ 테이블 삭제 실패"
    exit 1
fi

echo ""
echo "==========================================================="
echo "작업 완료"
echo "==========================================================="
