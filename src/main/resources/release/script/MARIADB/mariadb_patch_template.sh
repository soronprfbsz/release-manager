#!/bin/bash

################################################################################
# MariaDB 누적 패치 실행 스크립트
# 생성일: {{GENERATED_DATE}}
# 버전 범위: {{FROM_VERSION}} → {{TO_VERSION}}
################################################################################

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
DEFAULT_PATCHED_BY="{{DEFAULT_PATCHED_BY}}"

# 버전 메타데이터 배열
declare -a VERSION_METADATA=(
{{VERSION_METADATA}}
)

# 스크립트 시작
echo "=========================================="
echo "  MariaDB 누적 패치 실행 스크립트"
echo "=========================================="
echo ""
echo "패치 버전 범위: {{FROM_VERSION}} → {{TO_VERSION}}"
echo "포함된 버전 개수: {{VERSION_COUNT}}"
echo ""

# 패치 적용 담당자 입력
echo "=========================================="
echo "버전 이력 관리를 위한 정보 입력"
echo "=========================================="
# 기본값이 있으면 표시
if [ -n "$DEFAULT_PATCHED_BY" ]; then
    read -p "패치 적용 담당자 [$DEFAULT_PATCHED_BY]: " APPLIED_BY
    APPLIED_BY=${APPLIED_BY:-$DEFAULT_PATCHED_BY}
else
    read -p "패치 적용 담당자 (예: jhlee@company.com): " APPLIED_BY
fi
if [ -z "$APPLIED_BY" ]; then
    log_error "패치 적용 담당자는 필수 입력값입니다."
    exit 1
fi
echo ""

# 실행 방식 선택
echo "=========================================="
echo "MariaDB 접속 방식을 선택하세요:"
echo ""
echo "  1) Docker 컨테이너 방식"
echo "     → Docker 컨테이너 내부의 MariaDB에 접속"
echo "     → 'docker exec' 명령으로 컨테이너 내부에서 실행"
echo ""
echo "  2) 네트워크 직접 연결 방식"
echo "     → 호스트:포트로 MariaDB에 직접 연결"
echo "     → 로컬/원격 서버 모두 지원 (IP 또는 localhost)"
echo "=========================================="
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
    log_info "선택된 방식: Docker 컨테이너 방식"
    echo ""

    read -p "Docker 컨테이너 이름 [$DEFAULT_DOCKER_CONTAINER_NAME]: " DOCKER_CONTAINER_NAME
    DOCKER_CONTAINER_NAME=${DOCKER_CONTAINER_NAME:-$DEFAULT_DOCKER_CONTAINER_NAME}

    read -p "MariaDB 사용자명 [$DEFAULT_DB_USER]: " DB_USER
    DB_USER=${DB_USER:-$DEFAULT_DB_USER}

    read -sp "MariaDB 비밀번호: " DB_PASSWORD
    echo ""

    echo ""
    log_info "Docker 컨테이너: $DOCKER_CONTAINER_NAME"
    log_info "사용자: $DB_USER"
    log_info "컨테이너 확인 중..."

    if ! docker ps --format "{{.Names}}" | grep -q "^${DOCKER_CONTAINER_NAME}$"; then
        log_error "Docker 컨테이너 '$DOCKER_CONTAINER_NAME'를 찾을 수 없거나 실행 중이 아닙니다."
        exit 1
    fi

    log_info "컨테이너 확인 완료!"

    log_info "MariaDB 연결 테스트 중..."
    if ! docker exec "$DOCKER_CONTAINER_NAME" mariadb -u"$DB_USER" -p"$DB_PASSWORD" -e "SELECT 1;" > /dev/null 2>&1; then
        log_error "MariaDB 접속에 실패했습니다. 사용자명/비밀번호를 확인해주세요."
        exit 1
    fi

    log_success "MariaDB 접속 성공!"

    execute_sql() {
        local sql_file=$1
        docker exec -i "$DOCKER_CONTAINER_NAME" mariadb -u"$DB_USER" -p"$DB_PASSWORD" \
            --verbose --show-warnings < "$sql_file"
    }

    execute_sql_string() {
        local sql_string=$1
        echo "$sql_string" | docker exec -i "$DOCKER_CONTAINER_NAME" mariadb -u"$DB_USER" -p"$DB_PASSWORD" \
            --verbose --show-warnings
    }

else
    log_info "선택된 방식: 네트워크 직접 연결 방식"
    echo ""

    read -p "MariaDB 호스트 주소 (예: localhost, 192.168.1.100) [localhost]: " DB_HOST
    DB_HOST=${DB_HOST:-localhost}

    read -p "MariaDB 포트 [3306]: " DB_PORT
    DB_PORT=${DB_PORT:-3306}

    read -p "MariaDB 사용자명 [$DEFAULT_DB_USER]: " DB_USER
    DB_USER=${DB_USER:-$DEFAULT_DB_USER}

    read -sp "MariaDB 비밀번호: " DB_PASSWORD
    echo ""

    echo ""
    log_info "MariaDB 호스트: $DB_HOST:$DB_PORT"
    log_info "사용자: $DB_USER"
    log_info "네트워크를 통해 MariaDB 연결 테스트 중..."

    if ! mariadb -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -e "SELECT 1;" > /dev/null 2>&1; then
        log_error "MariaDB 접속에 실패했습니다. 접속 정보를 확인해주세요."
        log_warning "mariadb 클라이언트가 설치되어 있는지 확인하세요."
        log_warning "방화벽 설정 및 포트가 열려있는지 확인하세요."
        exit 1
    fi

    log_success "MariaDB 접속 성공!"

    execute_sql() {
        local sql_file=$1
        mariadb -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" \
            --verbose --show-warnings < "$sql_file"
    }

    execute_sql_string() {
        local sql_string=$1
        echo "$sql_string" | mariadb -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" \
            --verbose --show-warnings
    }
fi

# 에러 발생 시 스크립트 중단
set -e

echo ""
echo "=========================================="
echo "  패치 실행 시작"
echo "=========================================="
echo ""

# SQL 파일 디렉토리로 이동 (존재하는 경우에만)
SQL_DIR="$SCRIPT_DIR/database/mariadb"
if [ -d "$SQL_DIR" ]; then
    cd "$SQL_DIR"
fi

{{SQL_EXECUTION_COMMANDS}}

echo ""
echo "=========================================="
log_success "누적 패치 실행 완료!"
echo "=========================================="
echo ""
echo "실행 요약:"
echo "  - 적용된 버전 개수: {{VERSION_COUNT}}"
echo "  - 버전 범위: {{FROM_VERSION}} → {{TO_VERSION}}"
echo "  - 적용 담당자: $APPLIED_BY"
echo ""
log_info "각 버전 정보가 CM_DB.VERSION_HISTORY 테이블에 기록되었습니다."
echo ""
