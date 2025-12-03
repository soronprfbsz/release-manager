#!/bin/bash

################################################################################
# CrateDB 스냅샷 복원 스크립트
#
# 사용법: ./cratedb_restore.sh
#
# CrateDB의 SNAPSHOT 기능을 사용하여 백업된 데이터를 복원합니다.
# 복원 전략: 백업 파일 복사 → CREATE REPOSITORY → RESTORE SNAPSHOT
################################################################################

################################################################################
# <<<< 설정 영역 시작 - 복원 시 이 부분만 수정하세요 >>>>
################################################################################

# CrateDB 연결 기본값
DEFAULT_CRATE_HOST="localhost"
DEFAULT_CRATE_PORT="4200"

# 백업 파일 설정
BACKUP_DIR="backup_files"

# 스냅샷 리포지토리 기본 설정 (CrateDB 서버의 파일 시스템 경로)
DEFAULT_SNAPSHOT_REPO_PATH="/tmp/crate_snapshots"

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
echo "  CrateDB 스냅샷 복원 스크립트"
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

BACKUP_FOLDERS=$(ls -dt ${BACKUP_DIR}/cratedb_snapshot_* 2>/dev/null)
if [ -z "$BACKUP_FOLDERS" ]; then
    echo "  (백업 폴더 없음)"
    echo "=========================================="
    log_error "복원할 백업이 없습니다."
    exit 1
fi

BACKUP_COUNT=0
echo "$BACKUP_FOLDERS" | while read -r folder; do
    BACKUP_COUNT=$((BACKUP_COUNT + 1))
    SIZE=$(du -sh "$folder" 2>/dev/null | cut -f1)
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

# 백업 메타데이터 확인
METADATA_FILE="${BACKUP_PATH}/backup_metadata.txt"
if [ ! -f "$METADATA_FILE" ]; then
    log_error "백업 메타데이터를 찾을 수 없습니다: $METADATA_FILE"
    exit 1
fi

# 백업 정보 표시
BACKUP_SIZE=$(du -sh "$BACKUP_PATH" 2>/dev/null | cut -f1)
echo ""
log_info "선택한 백업: $BACKUP_PATH"
log_info "폴더 크기: $BACKUP_SIZE"
echo ""
log_info "백업 메타데이터:"
cat "$METADATA_FILE"
echo ""

# 메타데이터에서 리포지토리와 스냅샷 이름 추출
REPO_NAME=$(grep "리포지토리 이름:" "$METADATA_FILE" | cut -d':' -f2 | xargs)
SNAPSHOT_NAME=$(grep "스냅샷 이름:" "$METADATA_FILE" | cut -d':' -f2 | xargs)

if [ -z "$REPO_NAME" ] || [ -z "$SNAPSHOT_NAME" ]; then
    log_error "메타데이터에서 리포지토리 또는 스냅샷 이름을 찾을 수 없습니다."
    exit 1
fi

log_info "복원 정보:"
echo "  - 리포지토리: $REPO_NAME"
echo "  - 스냅샷: $SNAPSHOT_NAME"
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

# 실행 방식 선택
echo "=========================================="
echo "CrateDB 접속 방식을 선택하세요:"
echo ""
echo "  1) Docker 컨테이너 방식"
echo "     → Docker 컨테이너 내부의 CrateDB에 접속"
echo "     → 'docker exec' 명령으로 컨테이너 내부에서 실행"
echo ""
echo "  2) 네트워크 직접 연결 방식"
echo "     → 호스트:포트로 CrateDB에 직접 연결"
echo "     → 로컬/원격 서버 모두 지원 (IP 또는 localhost)"
echo "=========================================="
echo ""
read -p "선택 (1 또는 2) [1]: " EXECUTION_MODE

# 입력값 정리 (공백 제거)
EXECUTION_MODE=$(echo "${EXECUTION_MODE:-1}" | tr -d '[:space:]')

# 유효성 검증
if [ "$EXECUTION_MODE" != "1" ] && [ "$EXECUTION_MODE" != "2" ]; then
    log_error "잘못된 선택입니다. 1 또는 2를 입력하세요. (입력값: '$EXECUTION_MODE')"
    exit 1
fi

echo ""

# 실행 방식에 따른 설정
if [ "$EXECUTION_MODE" = "1" ]; then
    # ===== Docker 컨테이너 방식 =====
    log_info "선택된 방식: Docker 컨테이너 방식"
    echo ""

    # Docker 컨테이너 이름 입력
    read -p "Docker 컨테이너 이름: " DOCKER_CONTAINER_NAME
    if [ -z "$DOCKER_CONTAINER_NAME" ]; then
        log_error "Docker 컨테이너 이름은 필수입니다."
        exit 1
    fi

    echo ""
    log_info "Docker 컨테이너: $DOCKER_CONTAINER_NAME"
    log_info "컨테이너 확인 중..."

    # Docker 컨테이너 확인
    docker ps --format "{{.Names}}" | grep -q "^${DOCKER_CONTAINER_NAME}$"
    if [ $? -ne 0 ]; then
        log_error "Docker 컨테이너 '$DOCKER_CONTAINER_NAME'를 찾을 수 없거나 실행 중이 아닙니다."
        log_warning "실행 중인 컨테이너 목록:"
        docker ps --format "  - {{.Names}} ({{.Image}})"
        exit 1
    fi

    log_info "컨테이너 확인 완료!"

    # CrateDB HTTP 포트 입력
    read -p "CrateDB HTTP 포트 [$DEFAULT_CRATE_PORT]: " CRATE_PORT
    CRATE_PORT=${CRATE_PORT:-$DEFAULT_CRATE_PORT}

    # 컨테이너 내부에서는 localhost 사용
    CRATE_HOST="localhost"

    # 스냅샷 리포지토리 경로 (컨테이너 내부 경로)
    read -p "스냅샷 리포지토리 경로 (컨테이너 내부) [$DEFAULT_SNAPSHOT_REPO_PATH]: " SNAPSHOT_REPO_PATH
    SNAPSHOT_REPO_PATH=${SNAPSHOT_REPO_PATH:-$DEFAULT_SNAPSHOT_REPO_PATH}

else
    # ===== 네트워크 직접 연결 방식 =====
    log_info "선택된 방식: 네트워크 직접 연결 방식"
    echo ""

    # CrateDB 연결 정보 입력
    read -p "CrateDB 호스트 [$DEFAULT_CRATE_HOST]: " CRATE_HOST
    CRATE_HOST=${CRATE_HOST:-$DEFAULT_CRATE_HOST}

    read -p "CrateDB HTTP 포트 [$DEFAULT_CRATE_PORT]: " CRATE_PORT
    CRATE_PORT=${CRATE_PORT:-$DEFAULT_CRATE_PORT}

    # 스냅샷 리포지토리 경로 (CrateDB 서버의 파일 시스템 경로)
    read -p "스냅샷 리포지토리 경로 (CrateDB 서버) [$DEFAULT_SNAPSHOT_REPO_PATH]: " SNAPSHOT_REPO_PATH
    SNAPSHOT_REPO_PATH=${SNAPSHOT_REPO_PATH:-$DEFAULT_SNAPSHOT_REPO_PATH}
fi

echo ""
log_info "호스트: $CRATE_HOST:$CRATE_PORT"
log_info "스냅샷 리포지토리 경로: $SNAPSHOT_REPO_PATH"
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
LOG_FILE="${LOG_DIR}/restore_cratedb_snapshot_${TIMESTAMP}.log"
log_info "로그 파일: $LOG_FILE"
echo ""

# 로그 파일에 정보 기록
echo "=========================================" > "$LOG_FILE"
echo "CrateDB 스냅샷 복원" >> "$LOG_FILE"
echo "=========================================" >> "$LOG_FILE"
echo "호스트: $CRATE_HOST:$CRATE_PORT" >> "$LOG_FILE"
echo "리포지토리 경로: $SNAPSHOT_REPO_PATH" >> "$LOG_FILE"
echo "백업 폴더: $BACKUP_PATH" >> "$LOG_FILE"
echo "리포지토리 이름: $REPO_NAME" >> "$LOG_FILE"
echo "스냅샷 이름: $SNAPSHOT_NAME" >> "$LOG_FILE"
echo "시작 시간: $(date '+%Y-%m-%d %H:%M:%S')" >> "$LOG_FILE"
echo "=========================================" >> "$LOG_FILE"
echo "" >> "$LOG_FILE"

# 복원 시작
echo "=========================================="
log_notice "스냅샷 복원을 시작합니다..."
echo "=========================================="
echo ""

START_TIME=$(date +%s)

# 1. Docker 모드인 경우 스냅샷 파일을 컨테이너로 복사
if [ "$EXECUTION_MODE" = "1" ]; then
    log_info "1단계: 스냅샷 파일을 호스트에서 컨테이너로 복사 중..."

    SNAPSHOT_TAR="${BACKUP_PATH}/snapshot_data.tar.gz"

    if [ ! -f "$SNAPSHOT_TAR" ]; then
        log_error "스냅샷 데이터 파일을 찾을 수 없습니다: $SNAPSHOT_TAR"
        exit 1
    fi

    # 컨테이너 내부의 스냅샷 디렉토리에 복사
    docker exec "$DOCKER_CONTAINER_NAME" mkdir -p "${SNAPSHOT_REPO_PATH}" 2>> "$LOG_FILE"
    cat "$SNAPSHOT_TAR" | docker exec -i "$DOCKER_CONTAINER_NAME" tar xzf - -C "${SNAPSHOT_REPO_PATH}" 2>> "$LOG_FILE"

    if [ $? -eq 0 ]; then
        log_info "  스냅샷 파일 복사 완료"
        echo "스냅샷 파일 복사: 성공" >> "$LOG_FILE"
    else
        log_error "  스냅샷 파일 복사 실패"
        echo "스냅샷 파일 복사: 실패" >> "$LOG_FILE"
        exit 1
    fi
else
    log_info "1단계: 네트워크 직접 연결 모드 - 스냅샷 파일이 이미 서버에 있다고 가정합니다."
    log_warning "  스냅샷 파일이 ${SNAPSHOT_REPO_PATH}/${REPO_NAME} 경로에 존재해야 합니다."
    echo "스냅샷 파일 확인: 건너뜀 (수동 확인 필요)" >> "$LOG_FILE"
fi

# 2. 스냅샷 리포지토리 생성
log_info "2단계: 스냅샷 리포지토리 생성 중..."

# 기존 리포지토리 확인 및 삭제
EXISTING_REPO=$(curl -s -X GET "http://${CRATE_HOST}:${CRATE_PORT}/_snapshot/${REPO_NAME}")
if ! echo "$EXISTING_REPO" | grep -q '"error"'; then
    log_warning "  기존 리포지토리 발견, 삭제 중..."
    curl -s -X DELETE "http://${CRATE_HOST}:${CRATE_PORT}/_snapshot/${REPO_NAME}" > /dev/null 2>&1
fi

CREATE_REPO_RESULT=$(curl -s -X PUT "http://${CRATE_HOST}:${CRATE_PORT}/_snapshot/${REPO_NAME}" \
    -H "Content-Type: application/json" \
    -d "{
        \"type\": \"fs\",
        \"settings\": {
            \"location\": \"${SNAPSHOT_REPO_PATH}/${REPO_NAME}\"
        }
    }")

if echo "$CREATE_REPO_RESULT" | grep -q '"acknowledged":true'; then
    log_info "  리포지토리 생성 완료: $REPO_NAME"
    echo "리포지토리 생성: 성공" >> "$LOG_FILE"
else
    log_error "  리포지토리 생성 실패"
    log_error "  응답: $CREATE_REPO_RESULT"
    echo "리포지토리 생성: 실패 - $CREATE_REPO_RESULT" >> "$LOG_FILE"
    exit 1
fi

# 3. 스냅샷 확인
log_info "3단계: 스냅샷 확인 중..."
SNAPSHOT_CHECK=$(curl -s -X GET "http://${CRATE_HOST}:${CRATE_PORT}/_snapshot/${REPO_NAME}/${SNAPSHOT_NAME}")

if echo "$SNAPSHOT_CHECK" | grep -q '"state":"SUCCESS"'; then
    log_info "  스냅샷 확인 완료: $SNAPSHOT_NAME"
    echo "스냅샷 확인: 성공" >> "$LOG_FILE"
else
    log_error "  스냅샷을 찾을 수 없거나 상태가 올바르지 않습니다."
    log_error "  응답: $SNAPSHOT_CHECK"
    echo "스냅샷 확인: 실패 - $SNAPSHOT_CHECK" >> "$LOG_FILE"

    # 실패한 리포지토리 삭제
    curl -s -X DELETE "http://${CRATE_HOST}:${CRATE_PORT}/_snapshot/${REPO_NAME}" > /dev/null 2>&1
    exit 1
fi

# 4. 스냅샷 복원
log_info "4단계: 스냅샷 복원 중... (시간이 걸릴 수 있습니다)"
RESTORE_RESULT=$(curl -s -X POST "http://${CRATE_HOST}:${CRATE_PORT}/_snapshot/${REPO_NAME}/${SNAPSHOT_NAME}/_restore?wait_for_completion=true" \
    -H "Content-Type: application/json" \
    -d '{
        "indices": "_all",
        "include_global_state": true
    }')

if echo "$RESTORE_RESULT" | grep -q '"accepted":true\|"state":"SUCCESS"'; then
    log_info "  스냅샷 복원 완료"
    echo "스냅샷 복원: 성공" >> "$LOG_FILE"
else
    log_error "  스냅샷 복원 실패"
    log_error "  응답: $RESTORE_RESULT"
    echo "스냅샷 복원: 실패 - $RESTORE_RESULT" >> "$LOG_FILE"

    # 실패한 리포지토리 삭제
    curl -s -X DELETE "http://${CRATE_HOST}:${CRATE_PORT}/_snapshot/${REPO_NAME}" > /dev/null 2>&1
    exit 1
fi

# 5. 리포지토리 삭제 (정리)
log_info "5단계: 리포지토리 정리 중..."
DELETE_REPO_RESULT=$(curl -s -X DELETE "http://${CRATE_HOST}:${CRATE_PORT}/_snapshot/${REPO_NAME}")

if echo "$DELETE_REPO_RESULT" | grep -q '"acknowledged":true'; then
    log_info "  리포지토리 삭제 완료"
    echo "리포지토리 정리: 성공" >> "$LOG_FILE"
else
    log_warning "  리포지토리 삭제 실패 (수동으로 삭제해야 할 수 있습니다)"
    echo "리포지토리 정리: 실패 - $DELETE_REPO_RESULT" >> "$LOG_FILE"
fi

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
log_notice "CrateDB 스냅샷 복원이 완료되었습니다."

exit 0
