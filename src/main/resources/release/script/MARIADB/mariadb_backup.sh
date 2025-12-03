#!/bin/bash

################################################################################
# MariaDB 백업 스크립트
# 
# 사용법: ./backup_mariadb.sh
# 
# 다음 백업 시 수정이 필요한 부분은 아래 "설정 영역"만 수정하세요.
################################################################################

################################################################################
# <<<< 설정 영역 시작 - 백업 시 이 부분만 수정하세요 >>>>
################################################################################

# Docker 컨테이너 기본값
DEFAULT_DOCKER_CONTAINER_NAME="infraeye_2.0"

# 데이터베이스 연결 기본값
DEFAULT_DB_USER="root"
DEFAULT_DB_NAME="NMS_DB"

# 백업 파일 설정
BACKUP_DIR="backup_files"  # 백업 파일 저장 디렉토리
BACKUP_PREFIX="backup"  # 백업 파일 접두사

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
echo "  MariaDB 백업 스크립트"
echo "=========================================="
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
    read -p "Docker 컨테이너 이름 [$DEFAULT_DOCKER_CONTAINER_NAME]: " DOCKER_CONTAINER_NAME
    DOCKER_CONTAINER_NAME=${DOCKER_CONTAINER_NAME:-$DEFAULT_DOCKER_CONTAINER_NAME}
    
    # MariaDB 접속 정보 입력
    read -p "MariaDB 사용자명 [$DEFAULT_DB_USER]: " DB_USER
    DB_USER=${DB_USER:-$DEFAULT_DB_USER}
    
    read -sp "MariaDB 비밀번호: " DB_PASSWORD
    echo ""
    
    # 데이터베이스 이름
    read -p "데이터베이스 이름 [$DEFAULT_DB_NAME]: " DB_NAME
    DB_NAME=${DB_NAME:-$DEFAULT_DB_NAME}
    
    echo ""
    log_info "Docker 컨테이너: $DOCKER_CONTAINER_NAME"
    log_info "데이터베이스: $DB_NAME"
    log_info "사용자: $DB_USER"
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
    
    # 연결 테스트
    log_info "MariaDB 연결 테스트 중..."
    docker exec "$DOCKER_CONTAINER_NAME" mariadb -u"$DB_USER" -p"$DB_PASSWORD" -e "SELECT 1;" > /dev/null 2>&1
    if [ $? -ne 0 ]; then
        log_error "MariaDB 접속에 실패했습니다. 접속 정보를 확인해주세요."
        exit 1
    fi
    
    log_info "MariaDB 접속 성공!"
    
else
    # ===== 네트워크 직접 연결 방식 =====
    log_info "선택된 방식: 네트워크 직접 연결 방식"
    echo ""

    # 서버 정보 입력
    read -p "MariaDB 호스트 주소 (예: localhost, 192.168.1.100): " DB_HOST
    if [ -z "$DB_HOST" ]; then
        log_error "호스트 주소는 필수입니다."
        exit 1
    fi
    
    read -p "MariaDB 포트 [3306]: " DB_PORT
    DB_PORT=${DB_PORT:-3306}
    
    read -p "MariaDB 사용자명 [$DEFAULT_DB_USER]: " DB_USER
    DB_USER=${DB_USER:-$DEFAULT_DB_USER}
    
    read -sp "MariaDB 비밀번호: " DB_PASSWORD
    echo ""
    
    read -p "데이터베이스 이름 [$DEFAULT_DB_NAME]: " DB_NAME
    DB_NAME=${DB_NAME:-$DEFAULT_DB_NAME}
    
    echo ""
    log_info "호스트: $DB_HOST:$DB_PORT"
    log_info "데이터베이스: $DB_NAME"
    log_info "사용자: $DB_USER"
    
    # 연결 테스트
    log_info "네트워크를 통해 MariaDB 연결 테스트 중..."
    mariadb -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -e "SELECT 1;" > /dev/null 2>&1
    if [ $? -ne 0 ]; then
        log_error "MariaDB 접속에 실패했습니다. 접속 정보를 확인해주세요."
        log_warning "mariadb 클라이언트가 설치되어 있는지 확인하세요."
        log_warning "방화벽 설정 및 포트가 열려있는지 확인하세요."
        exit 1
    fi

    log_info "MariaDB 접속 성공!"
fi

# 백업 파일명 생성
BACKUP_FILENAME="${BACKUP_PREFIX}_$(date +%Y%m%d_%H%M%S).sql"
BACKUP_FILE="${BACKUP_DIR}/${BACKUP_FILENAME}"

echo ""
log_info "백업 파일: $BACKUP_FILE"
echo ""

# 백업 디렉토리 생성
if [ ! -d "$BACKUP_DIR" ]; then
    mkdir -p "$BACKUP_DIR"
    log_info "백업 디렉토리 생성: $BACKUP_DIR"
fi

# 로그 디렉토리 생성
LOG_DIR="logs"
if [ ! -d "$LOG_DIR" ]; then
    mkdir -p "$LOG_DIR"
    log_info "로그 디렉토리 생성: $LOG_DIR"
fi

# 로그 파일 생성
LOG_FILE="${LOG_DIR}/backup_mariadb_$(date +%Y%m%d_%H%M%S).log"
log_info "로그 파일: $LOG_FILE"
echo ""

# 백업 시작
echo "=========================================="
log_notice "데이터베이스 백업을 시작합니다..."
echo "=========================================="
echo ""

# 로그 파일에 정보 기록
echo "=========================================" > "$LOG_FILE"
echo "MariaDB 백업" >> "$LOG_FILE"
echo "=========================================" >> "$LOG_FILE"
echo "데이터베이스: $DB_NAME" >> "$LOG_FILE"
echo "백업 파일: $BACKUP_FILE" >> "$LOG_FILE"
echo "시작 시간: $(date '+%Y-%m-%d %H:%M:%S')" >> "$LOG_FILE"
echo "=========================================" >> "$LOG_FILE"
echo "" >> "$LOG_FILE"

# 백업 실행
START_TIME=$(date +%s)

log_info "백업 진행 중... (시간이 걸릴 수 있습니다)"
echo ""

# 임시 오류 파일 생성
ERROR_FILE="${LOG_DIR}/temp_error_backup_$$.log"

# 백업 실행 (Docker 또는 원격)
if [ "$EXECUTION_MODE" = "1" ]; then
    # Docker 모드
    docker exec "$DOCKER_CONTAINER_NAME" mariadb-dump -u"$DB_USER" -p"$DB_PASSWORD" \
        --single-transaction \
        --routines \
        --triggers \
        --events \
        --hex-blob \
        --add-drop-database \
        --databases "$DB_NAME" > "$BACKUP_FILE" 2> "$ERROR_FILE"
    EXIT_CODE=$?
else
    # 원격 모드
    mariadb-dump -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" \
        --single-transaction \
        --routines \
        --triggers \
        --events \
        --hex-blob \
        --add-drop-database \
        --databases "$DB_NAME" > "$BACKUP_FILE" 2> "$ERROR_FILE"
    EXIT_CODE=$?
fi

END_TIME=$(date +%s)
ELAPSED=$((END_TIME - START_TIME))
ELAPSED_MIN=$((ELAPSED / 60))
ELAPSED_SEC=$((ELAPSED % 60))

# 백업 파일 크기 확인
if [ -f "$BACKUP_FILE" ]; then
    BACKUP_SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
    BACKUP_LINES=$(wc -l < "$BACKUP_FILE")
else
    BACKUP_SIZE="0"
    BACKUP_LINES="0"
fi

# 결과 기록
echo "" >> "$LOG_FILE"
echo "=========================================" >> "$LOG_FILE"
echo "종료 코드: $EXIT_CODE" >> "$LOG_FILE"
echo "실행 시간: ${ELAPSED}초 (${ELAPSED_MIN}분 ${ELAPSED_SEC}초)" >> "$LOG_FILE"
echo "파일 크기: $BACKUP_SIZE" >> "$LOG_FILE"
echo "파일 줄 수: $BACKUP_LINES 줄" >> "$LOG_FILE"
echo "종료 시간: $(date '+%Y-%m-%d %H:%M:%S')" >> "$LOG_FILE"

# 결과 확인
echo ""
echo "=========================================="
if [ $EXIT_CODE -eq 0 ] && [ -f "$BACKUP_FILE" ] && [ -s "$BACKUP_FILE" ]; then
    echo "상태: 성공" >> "$LOG_FILE"
    echo "=========================================" >> "$LOG_FILE"

    # 성공 시 임시 오류 파일 삭제
    rm -f "$ERROR_FILE"

    log_info "✓ 백업 성공!"
    echo ""
    log_info "백업 파일: $BACKUP_FILE"
    log_info "파일 크기: $BACKUP_SIZE"
    log_info "파일 줄 수: $BACKUP_LINES 줄"
    log_info "소요 시간: ${ELAPSED_MIN}분 ${ELAPSED_SEC}초"
    log_info "로그 파일: $LOG_FILE"
    echo "=========================================="
    echo ""
    log_notice "데이터베이스가 성공적으로 백업되었습니다."
    echo ""

    # 기존 백업 파일 목록 표시
    log_info "백업 디렉토리의 백업 파일 목록:"
    ls -lht ${BACKUP_DIR}/${BACKUP_PREFIX}_*.sql 2>/dev/null | head -5 | while read -r line; do
        echo "  $line"
    done

    exit 0
else
    echo "상태: 실패 (종료 코드: $EXIT_CODE)" >> "$LOG_FILE"

    # 오류 메시지 상세 출력
    if [ -f "$ERROR_FILE" ] && [ -s "$ERROR_FILE" ]; then
        echo "" >> "$LOG_FILE"
        echo "========== 오류 상세 정보 ==========" >> "$LOG_FILE"
        cat "$ERROR_FILE" >> "$LOG_FILE"
        echo "====================================" >> "$LOG_FILE"

        # 콘솔에도 오류 출력
        echo ""
        log_error "오류 상세:"
        cat "$ERROR_FILE"
        echo ""
    else
        echo "오류 메시지를 찾을 수 없습니다." >> "$LOG_FILE"
    fi
    echo "=========================================" >> "$LOG_FILE"

    # 임시 파일 삭제
    rm -f "$ERROR_FILE"

    log_error "✗ 백업 실패 (종료 코드: $EXIT_CODE)"
    log_info "소요 시간: ${ELAPSED_MIN}분 ${ELAPSED_SEC}초"
    log_warning "로그 파일을 확인하세요: $LOG_FILE"
    echo "=========================================="
    echo ""
    log_error "데이터베이스 백업에 실패했습니다. 로그를 확인해주세요."

    # 실패 시 생성된 빈 파일 제거
    if [ -f "$BACKUP_FILE" ] && [ ! -s "$BACKUP_FILE" ]; then
        log_warning "빈 백업 파일을 삭제합니다: $BACKUP_FILE"
        rm -f "$BACKUP_FILE"
    fi

    exit 1
fi

