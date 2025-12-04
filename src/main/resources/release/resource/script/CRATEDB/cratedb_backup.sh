#!/bin/bash

################################################################################
# CrateDB 스냅샷 백업 스크립트
#
# 사용법: ./cratedb_backup.sh
#
# CrateDB의 SNAPSHOT 기능을 사용하여 데이터를 백업합니다.
# 백업 전략: CREATE REPOSITORY → CREATE SNAPSHOT → 압축 백업
################################################################################

################################################################################
# <<<< 설정 영역 시작 - 백업 시 이 부분만 수정하세요 >>>>
################################################################################

# CrateDB 연결 기본값
DEFAULT_CRATE_HOST="localhost"
DEFAULT_CRATE_PORT="4200"

# 백업 파일 설정
BACKUP_DIR="backup_files"
BACKUP_PREFIX="cratedb_snapshot"

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
echo "  CrateDB 스냅샷 백업 스크립트"
echo "=========================================="
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

LOG_FILE="${LOG_DIR}/backup_cratedb_snapshot_${TIMESTAMP}.log"
log_info "로그 파일: $LOG_FILE"
echo ""

# 로그 파일에 정보 기록
echo "=========================================" > "$LOG_FILE"
echo "CrateDB 스냅샷 백업" >> "$LOG_FILE"
echo "=========================================" >> "$LOG_FILE"
echo "호스트: $CRATE_HOST:$CRATE_PORT" >> "$LOG_FILE"
echo "리포지토리 경로: $SNAPSHOT_REPO_PATH" >> "$LOG_FILE"
echo "백업 디렉토리: $BACKUP_SUBDIR" >> "$LOG_FILE"
echo "시작 시간: $(date '+%Y-%m-%d %H:%M:%S')" >> "$LOG_FILE"
echo "=========================================" >> "$LOG_FILE"
echo "" >> "$LOG_FILE"

# 백업 시작
echo "=========================================="
log_notice "스냅샷 백업을 시작합니다..."
echo "=========================================="
echo ""

START_TIME=$(date +%s)

# 리포지토리 이름 생성
REPO_NAME="backup_repo_${TIMESTAMP}"
SNAPSHOT_NAME="snapshot_${TIMESTAMP}"

# 1. 스냅샷 리포지토리 생성
log_info "1단계: 스냅샷 리포지토리 생성 중..."
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

# 2. 스냅샷 생성
log_info "2단계: 스냅샷 생성 중..."
CREATE_SNAPSHOT_RESULT=$(curl -s -X PUT "http://${CRATE_HOST}:${CRATE_PORT}/_snapshot/${REPO_NAME}/${SNAPSHOT_NAME}?wait_for_completion=true" \
    -H "Content-Type: application/json" \
    -d '{
        "indices": "_all",
        "include_global_state": true
    }')

if echo "$CREATE_SNAPSHOT_RESULT" | grep -q '"state":"SUCCESS"'; then
    log_info "  스냅샷 생성 완료: $SNAPSHOT_NAME"
    echo "스냅샷 생성: 성공" >> "$LOG_FILE"
else
    log_error "  스냅샷 생성 실패"
    log_error "  응답: $CREATE_SNAPSHOT_RESULT"
    echo "스냅샷 생성: 실패 - $CREATE_SNAPSHOT_RESULT" >> "$LOG_FILE"

    # 실패한 리포지토리 삭제
    curl -s -X DELETE "http://${CRATE_HOST}:${CRATE_PORT}/_snapshot/${REPO_NAME}" > /dev/null 2>&1
    exit 1
fi

# 3. 스냅샷 정보 조회 및 저장
log_info "3단계: 스냅샷 정보 조회 중..."
SNAPSHOT_INFO=$(curl -s -X GET "http://${CRATE_HOST}:${CRATE_PORT}/_snapshot/${REPO_NAME}/${SNAPSHOT_NAME}")
echo "$SNAPSHOT_INFO" > "${BACKUP_SUBDIR}/snapshot_info.json"
log_info "  스냅샷 정보 저장 완료"

# 4. Docker 모드인 경우 스냅샷 파일 복사
if [ "$EXECUTION_MODE" = "1" ]; then
    log_info "4단계: 스냅샷 파일을 컨테이너에서 호스트로 복사 중..."

    # 컨테이너 내부의 스냅샷 디렉토리를 tar로 압축하여 복사
    SNAPSHOT_TAR="${BACKUP_SUBDIR}/snapshot_data.tar.gz"

    docker exec "$DOCKER_CONTAINER_NAME" tar czf - -C "${SNAPSHOT_REPO_PATH}" "${REPO_NAME}" > "$SNAPSHOT_TAR" 2>> "$LOG_FILE"

    if [ $? -eq 0 ] && [ -f "$SNAPSHOT_TAR" ]; then
        SNAPSHOT_SIZE=$(du -h "$SNAPSHOT_TAR" | cut -f1)
        log_info "  스냅샷 파일 복사 완료 (크기: $SNAPSHOT_SIZE)"
        echo "스냅샷 파일 복사: 성공 (크기: $SNAPSHOT_SIZE)" >> "$LOG_FILE"
    else
        log_warning "  스냅샷 파일 복사 실패 (스냅샷은 서버에 존재함)"
        echo "스냅샷 파일 복사: 실패" >> "$LOG_FILE"
    fi
fi

# 5. 메타데이터 저장
log_info "5단계: 백업 메타데이터 저장 중..."
METADATA_FILE="${BACKUP_SUBDIR}/backup_metadata.txt"
cat > "$METADATA_FILE" <<EOF
========================================
CrateDB 스냅샷 백업 메타데이터
========================================
백업 시간: $(date '+%Y-%m-%d %H:%M:%S')
CrateDB 호스트: $CRATE_HOST:$CRATE_PORT
리포지토리 이름: $REPO_NAME
스냅샷 이름: $SNAPSHOT_NAME
리포지토리 경로: $SNAPSHOT_REPO_PATH
실행 모드: $([ "$EXECUTION_MODE" = "1" ] && echo "Docker 컨테이너" || echo "네트워크 직접 연결")
========================================
EOF

log_info "  메타데이터 저장 완료"

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

# 백업 디렉토리 크기
BACKUP_SIZE=$(du -sh "$BACKUP_SUBDIR" 2>/dev/null | cut -f1)

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
log_notice "CrateDB 스냅샷 백업이 완료되었습니다."
echo ""

# 중요 정보 표시
log_warning "복원 시 필요한 정보:"
echo "  - 리포지토리: $REPO_NAME"
echo "  - 스냅샷: $SNAPSHOT_NAME"
echo "  - 메타데이터: ${BACKUP_SUBDIR}/backup_metadata.txt"
echo ""

# 백업 파일 목록 표시
log_info "백업된 파일 목록:"
ls -lh "$BACKUP_SUBDIR" | tail -n +2 | while read -r line; do
    echo "  $line"
done

exit 0
