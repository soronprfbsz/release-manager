#!/bin/bash

################################################################################
# CrateDB 누적 패치 실행 스크립트
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

# 스크립트 디렉토리
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 로그 디렉토리 및 파일 설정
LOG_DIR="$SCRIPT_DIR/logs/cratedb"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
LOG_FILE="$LOG_DIR/patch_{{FROM_VERSION}}_to_{{TO_VERSION}}_${TIMESTAMP}.log"

# 로그 디렉토리 생성
mkdir -p "$LOG_DIR"

# 로그 함수 (화면 + 파일 동시 출력)
log_to_file() {
    local message="$1"
    local timestamp=$(date +"%Y-%m-%d %H:%M:%S")
    echo "[$timestamp] $message" >> "$LOG_FILE"
}

log_info() {
    local message="$1"
    echo -e "${GREEN}[INFO]${NC} $message"
    log_to_file "[INFO] $message"
}

log_error() {
    local message="$1"
    echo -e "${RED}[ERROR]${NC} $message"
    log_to_file "[ERROR] $message"
}

log_warning() {
    local message="$1"
    echo -e "${YELLOW}[WARNING]${NC} $message"
    log_to_file "[WARNING] $message"
}

log_step() {
    local message="$1"
    echo -e "${CYAN}[STEP]${NC} $message"
    log_to_file "[STEP] $message"
}

log_success() {
    local message="$1"
    echo -e "${GREEN}[SUCCESS]${NC} $message"
    log_to_file "[SUCCESS] $message"
}

# 기본값
DEFAULT_CRATEDB_HOST="localhost"
DEFAULT_CRATEDB_PORT="4200"
DEFAULT_CRATEDB_USER="crate"

# Crash CLI 경로 (필요시 수정)
CRASH_CMD="crash"

# 버전 메타데이터 배열
declare -a VERSION_METADATA=(
{{VERSION_METADATA}}
)

# 스크립트 시작
START_TIME=$(date +"%Y-%m-%d %H:%M:%S")
log_to_file "=========================================="
log_to_file "  CrateDB 누적 패치 실행 스크립트"
log_to_file "=========================================="
log_to_file "실행 시작 시간: $START_TIME"
log_to_file "패치 버전 범위: {{FROM_VERSION}} → {{TO_VERSION}}"
log_to_file "포함된 버전 개수: {{VERSION_COUNT}}"
log_to_file "로그 파일: $LOG_FILE"
log_to_file ""

echo "=========================================="
echo "  CrateDB 누적 패치 실행 스크립트"
echo "=========================================="
echo ""
echo "패치 버전 범위: {{FROM_VERSION}} → {{TO_VERSION}}"
echo "포함된 버전 개수: {{VERSION_COUNT}}"
echo "로그 파일: $LOG_FILE"
echo ""

# CrateDB 접속 정보 입력
echo "=========================================="
echo "CrateDB 접속 정보 입력:"
echo "=========================================="
echo ""
echo "  crash CLI를 통해 SQL을 실행합니다."
echo "  기본 포트: 4200"
echo "  기본 사용자: crate (비밀번호 없음)"
echo ""
echo "  crash CLI가 설치되어 있어야 합니다."
echo "  설치 방법: pip install crash"
echo ""

read -p "CrateDB 호스트 주소 (예: localhost, 192.168.1.100) [$DEFAULT_CRATEDB_HOST]: " CRATEDB_HOST
CRATEDB_HOST=${CRATEDB_HOST:-$DEFAULT_CRATEDB_HOST}

read -p "CrateDB HTTP 포트 [$DEFAULT_CRATEDB_PORT]: " CRATEDB_PORT
CRATEDB_PORT=${CRATEDB_PORT:-$DEFAULT_CRATEDB_PORT}

read -p "CrateDB 사용자명 [$DEFAULT_CRATEDB_USER]: " CRATEDB_USER
CRATEDB_USER=${CRATEDB_USER:-$DEFAULT_CRATEDB_USER}

read -sp "CrateDB 비밀번호 (없으면 Enter): " CRATEDB_PASSWORD
echo ""

echo ""
log_info "CrateDB 호스트: $CRATEDB_HOST:$CRATEDB_PORT"
log_info "사용자: $CRATEDB_USER"
log_to_file "접속 정보 - 호스트: $CRATEDB_HOST:$CRATEDB_PORT, 사용자: $CRATEDB_USER"
log_info "crash CLI를 통해 CrateDB 연결 테스트 중..."

# crash 명령어 존재 확인
if ! command -v $CRASH_CMD &> /dev/null; then
    log_error "crash CLI를 찾을 수 없습니다."
    log_error "crash를 설치하거나 CRASH_CMD 변수를 올바른 경로로 설정하세요."
    log_error "설치 방법: pip install crash"
    exit 1
fi

# CrateDB 연결 테스트 (crash CLI 사용)
TEST_SQL="SELECT 1;"

if [ -z "$CRATEDB_PASSWORD" ]; then
    # 비밀번호 없음
    TEST_RESULT=$($CRASH_CMD --hosts "$CRATEDB_HOST:$CRATEDB_PORT" --username "$CRATEDB_USER" --command "$TEST_SQL" 2>&1)
else
    # 비밀번호 있음
    TEST_RESULT=$(CRATEPW="$CRATEDB_PASSWORD" $CRASH_CMD --hosts "$CRATEDB_HOST:$CRATEDB_PORT" --username "$CRATEDB_USER" --command "$TEST_SQL" 2>&1)
fi

log_to_file "연결 테스트 결과:"
log_to_file "$TEST_RESULT"

# 에러 확인 (crash는 오류 시 stderr에 메시지 출력)
if echo "$TEST_RESULT" | grep -qi "error\|failed\|exception\|unable to connect"; then
    log_error "CrateDB 접속에 실패했습니다."
    log_error "연결 테스트 출력:"
    log_error "$TEST_RESULT"
    log_warning "호스트 주소, 포트, 사용자명, 비밀번호를 확인해주세요."
    log_warning "CrateDB 서버가 실행 중인지 확인하세요."
    exit 1
fi

log_success "CrateDB 접속 성공!"

# SQL 실행 함수 (crash CLI 사용)
execute_sql() {
    local sql_file=$1
    log_step "SQL 파일 실행: $sql_file"
    log_to_file "--- SQL 파일 실행 시작: $sql_file ---"

    # SQL 파일 존재 여부 확인
    if [ ! -f "$sql_file" ]; then
        log_error "SQL 파일을 찾을 수 없습니다: $sql_file"
        log_to_file "--- SQL 파일을 찾을 수 없음: $sql_file ---"
        log_to_file "현재 디렉토리: $(pwd)"
        log_to_file "디렉토리 내용:"
        log_to_file "$(ls -la)"
        return 1
    fi

    # SQL 파일 크기 확인
    local file_size=$(wc -c < "$sql_file")
    if [ "$file_size" -eq 0 ]; then
        log_warning "SQL 파일이 비어있습니다: $sql_file"
        log_to_file "--- SQL 파일이 비어있음: $sql_file ---"
        return 0
    fi

    log_to_file "SQL 파일 크기: $file_size bytes"
    log_to_file "SQL 파일 내용 미리보기 (처음 500자):"
    log_to_file "$(head -c 500 "$sql_file")"
    log_to_file "..."

    # crash CLI로 SQL 파일 실행
    if [ -z "$CRATEDB_PASSWORD" ]; then
        # 비밀번호 없음
        SQL_RESULT=$($CRASH_CMD --hosts "$CRATEDB_HOST:$CRATEDB_PORT" --username "$CRATEDB_USER" < "$sql_file" 2>&1)
        EXIT_CODE=$?
    else
        # 비밀번호 있음 (환경변수 CRATEPW 사용)
        SQL_RESULT=$(CRATEPW="$CRATEDB_PASSWORD" $CRASH_CMD --hosts "$CRATEDB_HOST:$CRATEDB_PORT" --username "$CRATEDB_USER" < "$sql_file" 2>&1)
        EXIT_CODE=$?
    fi

    log_to_file "crash CLI 종료 코드: $EXIT_CODE"
    log_to_file "crash CLI 출력:"
    log_to_file "$SQL_RESULT"

    # 에러 확인
    if [ $EXIT_CODE -ne 0 ]; then
        log_error "SQL 파일 실행 실패: $sql_file (종료 코드: $EXIT_CODE)"
        log_error "crash CLI 출력:"
        log_error "$SQL_RESULT"
        log_to_file "--- SQL 파일 실행 실패: $sql_file ---"
        return 1
    fi

    # 출력 내용에서 에러 메시지 확인
    if echo "$SQL_RESULT" | grep -qi "error\|exception\|failed"; then
        log_error "SQL 실행 중 에러 발생: $sql_file"
        log_error "에러 내용:"
        log_error "$SQL_RESULT"
        log_to_file "--- SQL 실행 에러: $sql_file ---"
        return 1
    fi

    log_success "SQL 파일 실행 성공: $sql_file"
    log_to_file "--- SQL 파일 실행 성공: $sql_file ---"
    return 0
}

execute_sql_string() {
    local sql_string=$1
    log_to_file "--- SQL 문자열 실행 시작 ---"
    log_to_file "$sql_string"

    # crash CLI로 SQL 문자열 실행
    if [ -z "$CRATEDB_PASSWORD" ]; then
        # 비밀번호 없음
        SQL_RESULT=$(echo "$sql_string" | $CRASH_CMD --hosts "$CRATEDB_HOST:$CRATEDB_PORT" --username "$CRATEDB_USER" 2>&1)
        EXIT_CODE=$?
    else
        # 비밀번호 있음
        SQL_RESULT=$(echo "$sql_string" | CRATEPW="$CRATEDB_PASSWORD" $CRASH_CMD --hosts "$CRATEDB_HOST:$CRATEDB_PORT" --username "$CRATEDB_USER" 2>&1)
        EXIT_CODE=$?
    fi

    log_to_file "crash CLI 종료 코드: $EXIT_CODE"
    log_to_file "crash CLI 출력:"
    log_to_file "$SQL_RESULT"

    # 에러 확인
    if [ $EXIT_CODE -ne 0 ]; then
        log_error "SQL 문자열 실행 실패 (종료 코드: $EXIT_CODE)"
        log_error "crash CLI 출력:"
        log_error "$SQL_RESULT"
        log_to_file "--- SQL 문자열 실행 실패 ---"
        return 1
    fi

    # 출력 내용에서 에러 메시지 확인
    if echo "$SQL_RESULT" | grep -qi "error\|exception\|failed"; then
        log_error "SQL 실행 중 에러 발생"
        log_error "에러 내용:"
        log_error "$SQL_RESULT"
        log_to_file "--- SQL 실행 에러 ---"
        return 1
    fi

    log_to_file "--- SQL 문자열 실행 성공 ---"
    return 0
}

# 에러 핸들러 함수
error_handler() {
    local line_number=$1
    local error_code=$2

    log_error "스크립트 실행 중 에러 발생 (라인: $line_number, 에러 코드: $error_code)"

    END_TIME=$(date +"%Y-%m-%d %H:%M:%S")
    START_TIMESTAMP=$(date -d "$START_TIME" +%s 2>/dev/null || date -j -f "%Y-%m-%d %H:%M:%S" "$START_TIME" +%s 2>/dev/null || echo "0")
    END_TIMESTAMP=$(date +%s)
    DURATION=$((END_TIMESTAMP - START_TIMESTAMP))
    DURATION_MIN=$((DURATION / 60))
    DURATION_SEC=$((DURATION % 60))

    log_to_file "=========================================="
    log_to_file "스크립트 실행 실패"
    log_to_file "=========================================="
    log_to_file "에러 발생 시간: $END_TIME"
    log_to_file "에러 발생 라인: $line_number"
    log_to_file "에러 코드: $error_code"
    log_to_file "실행 시간: ${DURATION_MIN}분 ${DURATION_SEC}초"
    log_to_file "=========================================="

    echo ""
    echo "=========================================="
    log_error "패치 실행 실패!"
    echo "=========================================="
    echo "상세 정보는 로그 파일을 확인하세요: $LOG_FILE"
    echo ""

    exit $error_code
}

# 에러 발생 시 핸들러 실행
trap 'error_handler ${LINENO} $?' ERR

# 에러 발생 시 스크립트 중단
set -e

echo ""
echo "=========================================="
echo "  패치 실행 시작"
echo "=========================================="
echo ""

# SQL 파일 디렉토리로 이동
SQL_DIR="$SCRIPT_DIR/database/cratedb"
if [ -d "$SQL_DIR" ]; then
    cd "$SQL_DIR"
fi

{{SQL_EXECUTION_COMMANDS}}

echo ""
echo "=========================================="
log_success "누적 패치 실행 완료!"
echo "=========================================="
echo ""

# 실행 종료 시간 및 소요 시간 계산
END_TIME=$(date +"%Y-%m-%d %H:%M:%S")
START_TIMESTAMP=$(date -d "$START_TIME" +%s 2>/dev/null || date -j -f "%Y-%m-%d %H:%M:%S" "$START_TIME" +%s 2>/dev/null || echo "0")
END_TIMESTAMP=$(date +%s)
DURATION=$((END_TIMESTAMP - START_TIMESTAMP))
DURATION_MIN=$((DURATION / 60))
DURATION_SEC=$((DURATION % 60))

echo "실행 요약:"
echo "  - 적용된 버전 개수: {{VERSION_COUNT}}"
echo "  - 버전 범위: {{FROM_VERSION}} → {{TO_VERSION}}"
echo "  - 시작 시간: $START_TIME"
echo "  - 종료 시간: $END_TIME"
echo "  - 실행 시간: ${DURATION_MIN}분 ${DURATION_SEC}초"
echo ""
log_info "로그 파일: $LOG_FILE"
echo ""

# 최종 로그 기록
log_to_file "=========================================="
log_to_file "누적 패치 실행 완료"
log_to_file "=========================================="
log_to_file "실행 종료 시간: $END_TIME"
log_to_file "총 실행 시간: ${DURATION_MIN}분 ${DURATION_SEC}초"
log_to_file "적용된 버전 개수: {{VERSION_COUNT}}"
log_to_file "버전 범위: {{FROM_VERSION}} → {{TO_VERSION}}"
log_to_file "=========================================="
