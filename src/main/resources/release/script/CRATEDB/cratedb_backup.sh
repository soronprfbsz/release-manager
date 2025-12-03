#!/bin/bash

################################################################################
# CrateDB 백업 스크립트
#
# 사용법: ./cratedb_backup.sh
#
# CrateDB의 데이터를 JSON 형식으로 백업합니다.
# COPY TO 명령을 사용하여 테이블 데이터를 파일로 내보냅니다.
################################################################################

################################################################################
# <<<< 설정 영역 시작 - 백업 시 이 부분만 수정하세요 >>>>
################################################################################

# CrateDB 연결 기본값
DEFAULT_CRATE_HOST="localhost"
DEFAULT_CRATE_PORT="4200"
DEFAULT_CRATE_SCHEMA="doc"

# 백업 파일 설정
BACKUP_DIR="backup_files"
BACKUP_PREFIX="cratedb_backup"

################################################################################
# <<<< 설정 영역 끝 - 아래는 수정하지 마세요 >>>>
################################################################################


# 색상 코드
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
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

log_notice() {
    echo -e "${BLUE}[NOTICE]${NC} $1"
}

# 스크립트 시작
echo "=========================================="
echo "  CrateDB 백업 스크립트"
echo "=========================================="
echo ""

# CrateDB 연결 정보 입력
read -p "CrateDB 호스트 [$DEFAULT_CRATE_HOST]: " CRATE_HOST
CRATE_HOST=${CRATE_HOST:-$DEFAULT_CRATE_HOST}

read -p "CrateDB HTTP 포트 [$DEFAULT_CRATE_PORT]: " CRATE_PORT
CRATE_PORT=${CRATE_PORT:-$DEFAULT_CRATE_PORT}

read -p "스키마 이름 [$DEFAULT_CRATE_SCHEMA]: " CRATE_SCHEMA
CRATE_SCHEMA=${CRATE_SCHEMA:-$DEFAULT_CRATE_SCHEMA}

echo ""
log_info "호스트: $CRATE_HOST:$CRATE_PORT"
log_info "스키마: $CRATE_SCHEMA"
echo ""

# 연결 테스트
log_info "CrateDB 연결 테스트 중..."
CONN_TEST=$(curl -s -X POST "http://${CRATE_HOST}:${CRATE_PORT}/_sql" \
    -H "Content-Type: application/json" \
    -d '{"stmt": "SELECT 1"}' 2>&1)

if echo "$CONN_TEST" | grep -q '"rowcount":1'; then
    log_info "CrateDB 연결 성공!"
else
    log_error "CrateDB 연결에 실패했습니다."
    log_error "응답: $CONN_TEST"
    exit 1
fi

# 테이블 목록 조회
log_info "테이블 목록 조회 중..."
TABLES_RESPONSE=$(curl -s -X POST "http://${CRATE_HOST}:${CRATE_PORT}/_sql" \
    -H "Content-Type: application/json" \
    -d "{\"stmt\": \"SELECT table_name FROM information_schema.tables WHERE table_schema = '${CRATE_SCHEMA}' AND table_type = 'BASE TABLE' ORDER BY table_name\"}")

# jq가 없는 경우 대체 방법 사용
if command -v jq &> /dev/null; then
    TABLES=$(echo "$TABLES_RESPONSE" | jq -r '.rows[][0]' 2>/dev/null)
else
    # jq 없이 파싱 (간단한 패턴 매칭)
    TABLES=$(echo "$TABLES_RESPONSE" | grep -oP '"\K[^"]+(?=")' | grep -v 'rows\|cols\|rowcount\|table_name\|duration')
fi

if [ -z "$TABLES" ]; then
    log_error "백업할 테이블이 없습니다."
    exit 1
fi

echo ""
log_info "백업 대상 테이블:"
echo "$TABLES" | while read -r table; do
    echo "  - $table"
done
echo ""

# 백업 디렉토리 생성
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_SUBDIR="${BACKUP_DIR}/${BACKUP_PREFIX}_${TIMESTAMP}"

if [ ! -d "$BACKUP_SUBDIR" ]; then
    mkdir -p "$BACKUP_SUBDIR"
    log_info "백업 디렉토리 생성: $BACKUP_SUBDIR"
fi

# 로그 디렉토리 생성
LOG_DIR="logs"
if [ ! -d "$LOG_DIR" ]; then
    mkdir -p "$LOG_DIR"
fi

LOG_FILE="${LOG_DIR}/backup_cratedb_${TIMESTAMP}.log"
log_info "로그 파일: $LOG_FILE"
echo ""

# 로그 파일에 정보 기록
echo "=========================================" > "$LOG_FILE"
echo "CrateDB 백업" >> "$LOG_FILE"
echo "=========================================" >> "$LOG_FILE"
echo "호스트: $CRATE_HOST:$CRATE_PORT" >> "$LOG_FILE"
echo "스키마: $CRATE_SCHEMA" >> "$LOG_FILE"
echo "백업 디렉토리: $BACKUP_SUBDIR" >> "$LOG_FILE"
echo "시작 시간: $(date '+%Y-%m-%d %H:%M:%S')" >> "$LOG_FILE"
echo "=========================================" >> "$LOG_FILE"
echo "" >> "$LOG_FILE"

# 백업 시작
echo "=========================================="
log_notice "데이터베이스 백업을 시작합니다..."
echo "=========================================="
echo ""

START_TIME=$(date +%s)
TOTAL_TABLES=0
SUCCESS_TABLES=0
FAILED_TABLES=0

# 각 테이블 백업
echo "$TABLES" | while read -r TABLE_NAME; do
    if [ -z "$TABLE_NAME" ]; then
        continue
    fi

    TOTAL_TABLES=$((TOTAL_TABLES + 1))
    log_info "테이블 백업 중: $TABLE_NAME"

    # 테이블 데이터를 JSON으로 조회
    BACKUP_FILE="${BACKUP_SUBDIR}/${TABLE_NAME}.json"

    # COPY TO 문 실행 (파일 시스템에 직접 저장)
    # CrateDB COPY TO는 서버 측 파일 시스템에 저장하므로, SELECT로 데이터 조회 후 저장
    RESULT=$(curl -s -X POST "http://${CRATE_HOST}:${CRATE_PORT}/_sql" \
        -H "Content-Type: application/json" \
        -d "{\"stmt\": \"SELECT * FROM \\\"${CRATE_SCHEMA}\\\".\\\"${TABLE_NAME}\\\"\"}")

    if echo "$RESULT" | grep -q '"error"'; then
        log_error "  실패: $TABLE_NAME"
        echo "테이블 $TABLE_NAME 백업 실패: $RESULT" >> "$LOG_FILE"
        FAILED_TABLES=$((FAILED_TABLES + 1))
    else
        echo "$RESULT" > "$BACKUP_FILE"
        ROW_COUNT=$(echo "$RESULT" | grep -oP '"rowcount":\K[0-9]+')
        log_info "  완료: $TABLE_NAME ($ROW_COUNT rows)"
        echo "테이블 $TABLE_NAME 백업 완료: $ROW_COUNT rows" >> "$LOG_FILE"
        SUCCESS_TABLES=$((SUCCESS_TABLES + 1))
    fi
done

END_TIME=$(date +%s)
ELAPSED=$((END_TIME - START_TIME))
ELAPSED_MIN=$((ELAPSED / 60))
ELAPSED_SEC=$((ELAPSED % 60))

# 스키마 정보 백업
log_info "스키마 정보 백업 중..."
SCHEMA_FILE="${BACKUP_SUBDIR}/_schema.json"
curl -s -X POST "http://${CRATE_HOST}:${CRATE_PORT}/_sql" \
    -H "Content-Type: application/json" \
    -d "{\"stmt\": \"SELECT table_name, column_name, data_type FROM information_schema.columns WHERE table_schema = '${CRATE_SCHEMA}' ORDER BY table_name, ordinal_position\"}" > "$SCHEMA_FILE"

# 결과 기록
echo "" >> "$LOG_FILE"
echo "=========================================" >> "$LOG_FILE"
echo "실행 시간: ${ELAPSED}초 (${ELAPSED_MIN}분 ${ELAPSED_SEC}초)" >> "$LOG_FILE"
echo "종료 시간: $(date '+%Y-%m-%d %H:%M:%S')" >> "$LOG_FILE"
echo "=========================================" >> "$LOG_FILE"

# 백업 디렉토리 크기
BACKUP_SIZE=$(du -sh "$BACKUP_SUBDIR" | cut -f1)

# 결과 출력
echo ""
echo "=========================================="
log_info "백업 완료!"
echo ""
log_info "백업 디렉토리: $BACKUP_SUBDIR"
log_info "총 크기: $BACKUP_SIZE"
log_info "소요 시간: ${ELAPSED_MIN}분 ${ELAPSED_SEC}초"
log_info "로그 파일: $LOG_FILE"
echo "=========================================="
echo ""
log_notice "CrateDB 백업이 완료되었습니다."
echo ""

# 백업 파일 목록 표시
log_info "백업된 파일 목록:"
ls -lh "$BACKUP_SUBDIR" | tail -n +2 | while read -r line; do
    echo "  $line"
done

exit 0
