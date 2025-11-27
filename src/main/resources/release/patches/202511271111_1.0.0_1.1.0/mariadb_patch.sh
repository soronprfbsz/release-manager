#!/bin/bash

################################################################################
# MariaDB 누적 패치 실행 스크립트
# 생성일: 2025-11-27 11:11:21
# 버전 범위: 1.0.0 → 1.1.0
################################################################################

set -e

# 색상 코드
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 로그 함수
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_step() {
    echo -e "${CYAN}[STEP]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

# 스크립트 디렉토리
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 기본값
DEFAULT_DOCKER_CONTAINER_NAME="mariadb"
DEFAULT_DB_USER="root"
DEFAULT_DB_NAME="infraeye2"

# 버전 메타데이터 배열
declare -a VERSION_METADATA=(
    "1.1.0:2025-10-31:jhlee@tscientific:데이터코드, 이벤트코드, 메뉴코드 추가 / SMS 기능 추가 / VERSION_HISTORY 테이블 추가 / V_INFO_MCH 관련 뷰 변경"
)

# 스크립트 시작
echo "=========================================="
echo "  MariaDB 누적 패치 실행 스크립트"
echo "=========================================="
echo ""
echo "패치 버전 범위: 1.0.0 → 1.1.0"
echo "포함된 버전 개수: 1"
echo ""

# 실행 방식 선택
echo "패치 실행 방식을 선택하세요:"
echo "  1) 로컬 Docker 컨테이너"
echo "  2) 원격 MariaDB 서버"
echo ""
read -p "선택 (1 또는 2) [1]: " EXECUTION_MODE

EXECUTION_MODE=$(echo "${EXECUTION_MODE:-1}" | tr -d '[:space:]')

if [ "$EXECUTION_MODE" != "1" ] && [ "$EXECUTION_MODE" != "2" ]; then
    log_error "잘못된 선택입니다. 1 또는 2를 입력하세요."
    exit 1
fi

echo ""

# 실행 방식에 따른 설정
if [ "$EXECUTION_MODE" = "1" ]; then
    log_info "실행 모드: 로컬 Docker 컨테이너"
    echo ""

    read -p "Docker 컨테이너 이름 [$DEFAULT_DOCKER_CONTAINER_NAME]: " DOCKER_CONTAINER_NAME
    DOCKER_CONTAINER_NAME=${DOCKER_CONTAINER_NAME:-$DEFAULT_DOCKER_CONTAINER_NAME}

    read -p "MariaDB 사용자명 [$DEFAULT_DB_USER]: " DB_USER
    DB_USER=${DB_USER:-$DEFAULT_DB_USER}

    read -sp "MariaDB 비밀번호: " DB_PASSWORD
    echo ""

    read -p "데이터베이스 이름 [$DEFAULT_DB_NAME]: " DB_NAME
    DB_NAME=${DB_NAME:-$DEFAULT_DB_NAME}

    echo ""
    log_info "Docker 컨테이너: $DOCKER_CONTAINER_NAME"
    log_info "데이터베이스: $DB_NAME"
    log_info "사용자: $DB_USER"
    log_info "컨테이너 확인 중..."

    docker ps --format "{{.Names}}" | grep -q "^${DOCKER_CONTAINER_NAME}$"
    if [ $? -ne 0 ]; then
        log_error "Docker 컨테이너 '$DOCKER_CONTAINER_NAME'를 찾을 수 없거나 실행 중이 아닙니다."
        exit 1
    fi

    log_info "컨테이너 확인 완료!"

    log_info "MariaDB 연결 테스트 중..."
    docker exec "$DOCKER_CONTAINER_NAME" mariadb -u"$DB_USER" -p"$DB_PASSWORD" -e "SELECT 1;" > /dev/null 2>&1
    if [ $? -ne 0 ]; then
        log_error "MariaDB 접속에 실패했습니다."
        exit 1
    fi

    log_info "MariaDB 접속 성공!"

    execute_sql() {
        local sql_file=$1
        docker exec -i "$DOCKER_CONTAINER_NAME" mariadb -u"$DB_USER" -p"$DB_PASSWORD" \
            --verbose --show-warnings \
            "$DB_NAME" < "$sql_file"
    }

    execute_sql_string() {
        local sql_string=$1
        echo "$sql_string" | docker exec -i "$DOCKER_CONTAINER_NAME" mariadb -u"$DB_USER" -p"$DB_PASSWORD" \
            --verbose --show-warnings \
            "$DB_NAME"
    }

else
    log_info "실행 모드: 원격 MariaDB 서버"
    echo ""

    read -p "MariaDB 호스트 [localhost]: " DB_HOST
    DB_HOST=${DB_HOST:-localhost}

    read -p "MariaDB 포트 [3306]: " DB_PORT
    DB_PORT=${DB_PORT:-3306}

    read -p "MariaDB 사용자명 [$DEFAULT_DB_USER]: " DB_USER
    DB_USER=${DB_USER:-$DEFAULT_DB_USER}

    read -sp "MariaDB 비밀번호: " DB_PASSWORD
    echo ""

    read -p "데이터베이스 이름 [$DEFAULT_DB_NAME]: " DB_NAME
    DB_NAME=${DB_NAME:-$DEFAULT_DB_NAME}

    echo ""
    log_info "MariaDB 호스트: $DB_HOST:$DB_PORT"
    log_info "데이터베이스: $DB_NAME"
    log_info "사용자: $DB_USER"
    log_info "연결 테스트 중..."

    mariadb -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -e "SELECT 1;" > /dev/null 2>&1
    if [ $? -ne 0 ]; then
        log_error "MariaDB 접속에 실패했습니다."
        exit 1
    fi

    log_info "MariaDB 접속 성공!"

    execute_sql() {
        local sql_file=$1
        mariadb -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" \
            --verbose --show-warnings \
            "$DB_NAME" < "$sql_file"
    }

    execute_sql_string() {
        local sql_string=$1
        echo "$sql_string" | mariadb -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" \
            --verbose --show-warnings \
            "$DB_NAME"
    }
fi

echo ""
echo "=========================================="
echo "  패치 실행 시작"
echo "=========================================="
echo ""

# SQL 파일 실행
cd "$SCRIPT_DIR/mariadb/source_files"

log_step "버전 1.1.0 패치 적용 중..."
cd "1.1.0"
log_info "실행: 1.patch_mariadb_ddl.sql"
execute_sql "1.patch_mariadb_ddl.sql"
log_info "실행: 2.patch_mariadb_view.sql"
execute_sql "2.patch_mariadb_view.sql"
log_info "실행: 3.patch_mariadb_데이터코드.sql"
execute_sql "3.patch_mariadb_데이터코드.sql"
log_info "실행: 4.patch_mariadb_이벤트코드.sql"
execute_sql "4.patch_mariadb_이벤트코드.sql"
log_info "실행: 5.patch_mariadb_메뉴코드.sql"
execute_sql "5.patch_mariadb_메뉴코드.sql"
log_info "실행: 6.patch_mariadb_procedure.sql"
execute_sql "6.patch_mariadb_procedure.sql"
log_info "실행: 7.patch_mariadb_dml.sql"
execute_sql "7.patch_mariadb_dml.sql"
cd ..
log_success "버전 1.1.0 패치 완료!"



echo ""
echo "=========================================="
log_success "누적 패치 실행 완료!"
echo "=========================================="
echo ""
echo "실행 요약:"
echo "  - 적용된 버전 개수: 1"
echo "  - 버전 범위: 1.0.0 → 1.1.0"
echo ""
