#!/bin/bash

################################################################################
# CrateDB 복원 스크립트
#
# 사용법: ./cratedb_restore.sh
#
# JSON 형식으로 백업된 CrateDB 데이터를 복원합니다.
################################################################################

################################################################################
# <<<< 설정 영역 시작 - 복원 시 이 부분만 수정하세요 >>>>
################################################################################

# CrateDB 연결 기본값
DEFAULT_CRATE_HOST="localhost"
DEFAULT_CRATE_PORT="4200"
DEFAULT_CRATE_SCHEMA="doc"

# 백업 파일 설정
BACKUP_DIR="backup_files"

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
echo "  CrateDB 복원 스크립트"
echo "=========================================="
echo ""

# 백업 디렉토리 확인
if [ ! -d "$BACKUP_DIR" ]; then
    log_error "백업 디렉토리를 찾을 수 없습니다: $BACKUP_DIR"
    exit 1
fi

# 백업 폴더 목록 표시
log_info "사용 가능한 백업 목록:"
echo "=========================================="

BACKUP_FOLDERS=$(ls -dt ${BACKUP_DIR}/cratedb_backup_* 2>/dev/null)
if [ -z "$BACKUP_FOLDERS" ]; then
    echo "  (백업 폴더 없음)"
    echo "=========================================="
    log_error "복원할 백업이 없습니다."
    exit 1
fi

BACKUP_COUNT=0
echo "$BACKUP_FOLDERS" | while read -r folder; do
    BACKUP_COUNT=$((BACKUP_COUNT + 1))
    SIZE=$(du -sh "$folder" | cut -f1)
    NAME=$(basename "$folder")
    echo "  $NAME ($SIZE)"
done

BACKUP_COUNT=$(echo "$BACKUP_FOLDERS" | wc -l)
echo "=========================================="
log_info "총 ${BACKUP_COUNT}개의 백업이 있습니다."
echo ""

# 기본값으로 최신 백업 설정
DEFAULT_BACKUP=$(echo "$BACKUP_FOLDERS" | head -1 | xargs basename)

# 복원할 백업 선택
read -p "복원할 백업 폴더명 [$DEFAULT_BACKUP]: " SELECTED_BACKUP
SELECTED_BACKUP=${SELECTED_BACKUP:-$DEFAULT_BACKUP}

BACKUP_PATH="${BACKUP_DIR}/${SELECTED_BACKUP}"

if [ ! -d "$BACKUP_PATH" ]; then
    log_error "백업 폴더를 찾을 수 없습니다: $BACKUP_PATH"
    exit 1
fi

# 백업 정보 표시
BACKUP_SIZE=$(du -sh "$BACKUP_PATH" | cut -f1)
FILE_COUNT=$(ls -1 "$BACKUP_PATH"/*.json 2>/dev/null | wc -l)
echo ""
log_info "선택한 백업: $BACKUP_PATH"
log_info "폴더 크기: $BACKUP_SIZE"
log_info "파일 수: $FILE_COUNT"
echo ""

# 경고 메시지
echo "=========================================="
log_warning "경고: 복원 시 주의사항"
echo "=========================================="
echo -e "${YELLOW}1. 기존 테이블의 데이터가 삭제될 수 있습니다!${NC}"
echo -e "${YELLOW}2. 백업 데이터로 대체됩니다!${NC}"
echo -e "${YELLOW}3. 이 작업은 되돌릴 수 없습니다!${NC}"
echo "=========================================="
echo ""

# 최종 확인
read -p "정말로 복원을 진행하시겠습니까? (y/n): " CONFIRM
if [ "$CONFIRM" != "y" ]; then
    log_warning "사용자에 의해 취소되었습니다."
    exit 0
fi

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

# 로그 디렉토리 생성
LOG_DIR="logs"
if [ ! -d "$LOG_DIR" ]; then
    mkdir -p "$LOG_DIR"
fi

TIMESTAMP=$(date +%Y%m%d_%H%M%S)
LOG_FILE="${LOG_DIR}/restore_cratedb_${TIMESTAMP}.log"
log_info "로그 파일: $LOG_FILE"
echo ""

# 로그 파일에 정보 기록
echo "=========================================" > "$LOG_FILE"
echo "CrateDB 복원" >> "$LOG_FILE"
echo "=========================================" >> "$LOG_FILE"
echo "호스트: $CRATE_HOST:$CRATE_PORT" >> "$LOG_FILE"
echo "스키마: $CRATE_SCHEMA" >> "$LOG_FILE"
echo "백업 폴더: $BACKUP_PATH" >> "$LOG_FILE"
echo "시작 시간: $(date '+%Y-%m-%d %H:%M:%S')" >> "$LOG_FILE"
echo "=========================================" >> "$LOG_FILE"
echo "" >> "$LOG_FILE"

# 복원 시작
echo "=========================================="
log_notice "데이터베이스 복원을 시작합니다..."
echo "=========================================="
echo ""

START_TIME=$(date +%s)
SUCCESS_TABLES=0
FAILED_TABLES=0

# 각 JSON 파일 복원
for JSON_FILE in "$BACKUP_PATH"/*.json; do
    FILENAME=$(basename "$JSON_FILE")

    # 스키마 파일은 건너뜀
    if [ "$FILENAME" = "_schema.json" ]; then
        continue
    fi

    TABLE_NAME="${FILENAME%.json}"
    log_info "테이블 복원 중: $TABLE_NAME"

    # JSON 파일에서 rows 추출
    if command -v jq &> /dev/null; then
        COLS=$(jq -r '.cols | @json' "$JSON_FILE" 2>/dev/null)
        ROWS=$(jq -c '.rows[]' "$JSON_FILE" 2>/dev/null)
    else
        log_warning "  jq가 설치되지 않아 복원을 건너뜁니다."
        echo "테이블 $TABLE_NAME: jq 필요" >> "$LOG_FILE"
        FAILED_TABLES=$((FAILED_TABLES + 1))
        continue
    fi

    if [ -z "$ROWS" ]; then
        log_warning "  데이터 없음: $TABLE_NAME"
        echo "테이블 $TABLE_NAME: 데이터 없음" >> "$LOG_FILE"
        continue
    fi

    # 테이블 존재 확인 및 데이터 삭제
    log_info "  기존 데이터 삭제 중..."
    DELETE_RESULT=$(curl -s -X POST "http://${CRATE_HOST}:${CRATE_PORT}/_sql" \
        -H "Content-Type: application/json" \
        -d "{\"stmt\": \"DELETE FROM \\\"${CRATE_SCHEMA}\\\".\\\"${TABLE_NAME}\\\"\"}" 2>&1)

    # 데이터 삽입
    ROW_COUNT=0
    ERROR_COUNT=0

    echo "$ROWS" | while read -r ROW; do
        if [ -z "$ROW" ]; then
            continue
        fi

        # INSERT 문 생성 및 실행
        INSERT_STMT="INSERT INTO \"${CRATE_SCHEMA}\".\"${TABLE_NAME}\" VALUES ($ROW)"
        INSERT_RESULT=$(curl -s -X POST "http://${CRATE_HOST}:${CRATE_PORT}/_sql" \
            -H "Content-Type: application/json" \
            -d "{\"stmt\": \"$INSERT_STMT\"}" 2>&1)

        if echo "$INSERT_RESULT" | grep -q '"error"'; then
            ERROR_COUNT=$((ERROR_COUNT + 1))
        else
            ROW_COUNT=$((ROW_COUNT + 1))
        fi
    done

    if [ $ERROR_COUNT -eq 0 ]; then
        log_info "  완료: $TABLE_NAME"
        echo "테이블 $TABLE_NAME 복원 완료" >> "$LOG_FILE"
        SUCCESS_TABLES=$((SUCCESS_TABLES + 1))
    else
        log_warning "  일부 오류 발생: $TABLE_NAME ($ERROR_COUNT errors)"
        echo "테이블 $TABLE_NAME 복원 (일부 오류: $ERROR_COUNT)" >> "$LOG_FILE"
        FAILED_TABLES=$((FAILED_TABLES + 1))
    fi
done

END_TIME=$(date +%s)
ELAPSED=$((END_TIME - START_TIME))
ELAPSED_MIN=$((ELAPSED / 60))
ELAPSED_SEC=$((ELAPSED % 60))

# 결과 기록
echo "" >> "$LOG_FILE"
echo "=========================================" >> "$LOG_FILE"
echo "실행 시간: ${ELAPSED}초 (${ELAPSED_MIN}분 ${ELAPSED_SEC}초)" >> "$LOG_FILE"
echo "종료 시간: $(date '+%Y-%m-%d %H:%M:%S')" >> "$LOG_FILE"
echo "=========================================" >> "$LOG_FILE"

# 결과 출력
echo ""
echo "=========================================="
log_info "복원 완료!"
echo ""
log_info "소요 시간: ${ELAPSED_MIN}분 ${ELAPSED_SEC}초"
log_info "로그 파일: $LOG_FILE"
echo "=========================================="
echo ""
log_notice "CrateDB 복원이 완료되었습니다."

exit 0
