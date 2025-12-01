#!/bin/bash

################################################################################
# CrateDB 누적 패치 실행 스크립트
# 생성일: {{GENERATED_DATE}}
# 버전 범위: {{FROM_VERSION}} → {{TO_VERSION}}
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
DEFAULT_CRATEDB_HOST="localhost"
DEFAULT_CRATEDB_PORT="4200"

# 버전 메타데이터 배열
declare -a VERSION_METADATA=(
{{VERSION_METADATA}}
)

# 스크립트 시작
echo "=========================================="
echo "  CrateDB 누적 패치 실행 스크립트"
echo "=========================================="
echo ""
echo "패치 버전 범위: {{FROM_VERSION}} → {{TO_VERSION}}"
echo "포함된 버전 개수: {{VERSION_COUNT}}"
echo ""

# CrateDB 접속 정보 입력
read -p "CrateDB 호스트 [$DEFAULT_CRATEDB_HOST]: " CRATEDB_HOST
CRATEDB_HOST=${CRATEDB_HOST:-$DEFAULT_CRATEDB_HOST}

read -p "CrateDB 포트 [$DEFAULT_CRATEDB_PORT]: " CRATEDB_PORT
CRATEDB_PORT=${CRATEDB_PORT:-$DEFAULT_CRATEDB_PORT}

echo ""
log_info "CrateDB 호스트: $CRATEDB_HOST:$CRATEDB_PORT"
log_info "연결 테스트 중..."

# CrateDB 연결 테스트 (HTTP API 사용)
curl -s -X POST "http://$CRATEDB_HOST:$CRATEDB_PORT/_sql" \
    -H "Content-Type: application/json" \
    -d '{"stmt": "SELECT 1"}' > /dev/null 2>&1

if [ $? -ne 0 ]; then
    log_error "CrateDB 접속에 실패했습니다."
    exit 1
fi

log_info "CrateDB 접속 성공!"

# SQL 실행 함수
execute_sql_file() {
    local sql_file=$1
    log_info "실행 중: $sql_file"

    # SQL 파일 읽기
    local sql_content=$(cat "$sql_file")

    # CrateDB HTTP API로 실행
    curl -s -X POST "http://$CRATEDB_HOST:$CRATEDB_PORT/_sql" \
        -H "Content-Type: application/json" \
        -d "{\"stmt\": $(echo "$sql_content" | jq -Rs .)}" > /dev/null

    if [ $? -ne 0 ]; then
        log_error "SQL 실행 실패: $sql_file"
        exit 1
    fi
}

execute_sql_string() {
    local sql_string=$1

    curl -s -X POST "http://$CRATEDB_HOST:$CRATEDB_PORT/_sql" \
        -H "Content-Type: application/json" \
        -d "{\"stmt\": $(echo "$sql_string" | jq -Rs .)}" > /dev/null

    if [ $? -ne 0 ]; then
        log_error "SQL 실행 실패"
        return 1
    fi
}

echo ""
echo "=========================================="
echo "  패치 실행 시작"
echo "=========================================="
echo ""

# SQL 파일 실행
cd "$SCRIPT_DIR/database/cratedb/source_files"

{{SQL_EXECUTION_COMMANDS}}

echo ""
echo "=========================================="
log_success "누적 패치 실행 완료!"
echo "=========================================="
echo ""
echo "실행 요약:"
echo "  - 적용된 버전 개수: {{VERSION_COUNT}}"
echo "  - 버전 범위: {{FROM_VERSION}} → {{TO_VERSION}}"
echo ""
