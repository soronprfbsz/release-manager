#!/bin/bash

################################################################################
# MariaDB 패치 스크립트
# 
# 사용법: ./mariadb_patch.sh
# 
# 패치본 생성 시 수정이 필요한 부분은 아래 "설정 영역"만 수정하세요.
################################################################################

################################################################################
# <<<< 설정 영역 시작 - 패치 시 이 부분만 수정하세요 >>>>
################################################################################

# Docker 컨테이너 설정
DOCKER_CONTAINER_NAME="mariadb"

# 데이터베이스 연결 기본값
DEFAULT_DB_USER="root"
DEFAULT_DB_NAME="NMS_DB"

# SQL 파일이 존재하는 디렉토리 경로
SQL_DIR="source_files"

################################################################################
# <<<< 설정 영역 끝 - 아래는 수정하지 마세요 >>>>
################################################################################


# 색상 코드
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
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

# 스크립트 시작
echo "=========================================="
echo "  MariaDB 패치 실행 스크립트"
echo "=========================================="
echo ""

# MariaDB 접속 정보 입력
read -p "MariaDB 사용자명 [$DEFAULT_DB_USER]: " DB_USER
DB_USER=${DB_USER:-$DEFAULT_DB_USER}

read -sp "MariaDB 비밀번호: " DB_PASSWORD
echo ""

# 데이터베이스 이름
DB_NAME=$DEFAULT_DB_NAME

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
echo ""

# 로그 디렉토리 생성
LOG_DIR="logs"
if [ ! -d "$LOG_DIR" ]; then
    mkdir -p "$LOG_DIR"
    log_info "로그 디렉토리 생성: $LOG_DIR"
fi

# 로그 파일 생성
LOG_FILE="${LOG_DIR}/patch_mariadb_$(date +%Y%m%d_%H%M%S).log"
log_info "로그 파일: $LOG_FILE"
echo ""

# SQL 디렉토리 확인
if [ ! -d "$SQL_DIR" ]; then
    log_error "SQL 디렉토리를 찾을 수 없습니다: $SQL_DIR"
    exit 1
fi

# SQL_DIR 내의 모든 .sql 파일을 찾아서 배열에 저장 (숫자 순서 정렬)
mapfile -t SQL_FILES < <(find "$SQL_DIR" -maxdepth 1 -name "*.sql" -type f -printf "%f\n" | sort -V)

# SQL 파일 개수 확인
if [ ${#SQL_FILES[@]} -eq 0 ]; then
    log_error "SQL 디렉토리에 .sql 파일이 없습니다: $SQL_DIR"
    exit 1
fi

log_info "발견된 SQL 파일 목록:"
for sql_file in "${SQL_FILES[@]}"; do
    echo "  - $sql_file"
done
echo ""

log_info "실행할 파일 수: ${#SQL_FILES[@]}개"
echo ""

# 총 파일 수
TOTAL_FILES=${#SQL_FILES[@]}
SUCCESS_COUNT=0
FAIL_COUNT=0

# 각 SQL 파일 실행
for i in "${!SQL_FILES[@]}"; do
    SQL_FILE="${SQL_DIR}/${SQL_FILES[$i]}"
    FILE_NUM=$((i + 1))

    echo "=========================================="
    log_info "[$FILE_NUM/$TOTAL_FILES] ${SQL_FILES[$i]} 실행 중..."
    echo "=========================================="
    
    # 파일 존재 확인
    if [ ! -f "$SQL_FILE" ]; then
        log_error "파일을 찾을 수 없습니다: $SQL_FILE"
        FAIL_COUNT=$((FAIL_COUNT + 1))
        echo "" >> "$LOG_FILE"
        echo "=========================================" >> "$LOG_FILE"
        echo "파일: $SQL_FILE" >> "$LOG_FILE"
        echo "상태: 실패 (파일 없음)" >> "$LOG_FILE"
        echo "=========================================" >> "$LOG_FILE"
        continue
    fi
    
    # SQL 파일 실행
    echo "" >> "$LOG_FILE"
    echo "=========================================" >> "$LOG_FILE"
    echo "파일: $SQL_FILE" >> "$LOG_FILE"
    echo "시작 시간: $(date '+%Y-%m-%d %H:%M:%S')" >> "$LOG_FILE"
    echo "=========================================" >> "$LOG_FILE"

    START_TIME=$(date +%s)

    # 임시 출력 파일 생성
    TEMP_OUTPUT="${LOG_DIR}/temp_output_$$_${FILE_NUM}.log"

    # Docker 컨테이너 내부에서 mariadb 실행
    # stdout과 stderr를 모두 임시 파일에 저장
    docker exec -i "$DOCKER_CONTAINER_NAME" mariadb -u"$DB_USER" -p"$DB_PASSWORD" \
        --verbose --show-warnings \
        "$DB_NAME" < "$SQL_FILE" > "$TEMP_OUTPUT" 2>&1
    EXIT_CODE=$?

    END_TIME=$(date +%s)
    ELAPSED=$((END_TIME - START_TIME))

    # 실행 결과를 로그 파일에 추가
    cat "$TEMP_OUTPUT" >> "$LOG_FILE"

    # 결과 확인
    echo "" >> "$LOG_FILE"
    echo "=========================================" >> "$LOG_FILE"
    echo "종료 코드: $EXIT_CODE" >> "$LOG_FILE"
    echo "실행 시간: ${ELAPSED}초" >> "$LOG_FILE"
    echo "종료 시간: $(date '+%Y-%m-%d %H:%M:%S')" >> "$LOG_FILE"

    if [ $EXIT_CODE -eq 0 ]; then
        log_info "✓ 성공 (${ELAPSED}초)"
        SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
        echo "상태: 성공" >> "$LOG_FILE"

        # 성공 시 임시 파일 삭제
        rm -f "$TEMP_OUTPUT"
    else
        log_error "✗ 실패 (종료 코드: $EXIT_CODE)"
        FAIL_COUNT=$((FAIL_COUNT + 1))
        echo "상태: 실패 (종료 코드: $EXIT_CODE)" >> "$LOG_FILE"

        # 오류 메시지 상세 출력
        echo "" >> "$LOG_FILE"
        echo "========== 오류 상세 정보 ==========" >> "$LOG_FILE"

        # ERROR 키워드가 포함된 라인 추출
        if grep -i "ERROR" "$TEMP_OUTPUT" > /dev/null 2>&1; then
            grep -i "ERROR" "$TEMP_OUTPUT" >> "$LOG_FILE"
            echo "" >> "$LOG_FILE"

            # 콘솔에도 오류 출력
            echo ""
            log_error "발견된 오류:"
            grep -i "ERROR" "$TEMP_OUTPUT"
            echo ""
        else
            echo "구체적인 오류 메시지를 찾을 수 없습니다." >> "$LOG_FILE"
            echo "전체 출력은 위 로그를 참조하세요." >> "$LOG_FILE"

            # 마지막 20줄을 콘솔에 출력
            echo ""
            log_error "실행 결과 (마지막 20줄):"
            tail -20 "$TEMP_OUTPUT"
            echo ""
        fi
        echo "====================================" >> "$LOG_FILE"

        # 임시 파일 삭제
        rm -f "$TEMP_OUTPUT"

        # 실패 시 계속 진행할지 확인
        echo ""
        read -p "오류가 발생했습니다. 계속 진행하시겠습니까? (y/n): " CONTINUE
        if [[ ! $CONTINUE =~ ^[Yy]$ ]]; then
            log_warning "사용자에 의해 중단되었습니다."
            echo "사용자에 의해 중단됨" >> "$LOG_FILE"
            break
        fi
    fi
    echo "=========================================" >> "$LOG_FILE"
    echo ""
done

# 최종 결과 출력
echo ""
echo "=========================================="
echo "  패치 실행 완료"
echo "=========================================="
log_info "총 파일: $TOTAL_FILES"
log_info "성공: ${GREEN}$SUCCESS_COUNT${NC}"
if [ $FAIL_COUNT -gt 0 ]; then
    log_info "실패: ${RED}$FAIL_COUNT${NC}"
else
    log_info "실패: $FAIL_COUNT"
fi
log_info "로그 파일: $LOG_FILE"
echo "=========================================="

# 종료 코드 반환
if [ $FAIL_COUNT -eq 0 ] && [ $SUCCESS_COUNT -eq $TOTAL_FILES ]; then
    log_info "모든 패치가 성공적으로 적용되었습니다!"
    exit 0
else
    log_warning "일부 패치 적용에 실패했습니다. 로그 파일을 확인해주세요."
    exit 1
fi
