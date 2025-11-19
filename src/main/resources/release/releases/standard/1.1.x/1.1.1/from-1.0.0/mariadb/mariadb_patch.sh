#!/bin/bash

################################################################################
# MariaDB 패치 스크립트 (누적 패치용 - 자동 생성됨)
# 
# 사용법: ./mariadb_patch.sh
# 
# 이 스크립트는 누적 패치 생성 시 자동으로 생성되었습니다.
# 버전 순서대로 SQL이 실행되도록 구성되어 있습니다.
################################################################################

################################################################################
# <<<< 설정 영역 시작 >>>>
################################################################################

# Docker 컨테이너 기본값
DEFAULT_DOCKER_CONTAINER_NAME="infraeye_2.0"

# 데이터베이스 연결 기본값
DEFAULT_DB_USER="root"
DEFAULT_DB_NAME="NMS_DB"

# SQL 파일이 존재하는 디렉토리 경로
SQL_DIR="source_files"

################################################################################
# <<<< 설정 영역 끝 - 아래는 수정하지 마세요 >>>>
################################################################################

# 버전 메타데이터 (자동 생성됨)
declare -A VERSION_METADATA
VERSION_METADATA["1.1.0"]="2025-10-31|jhlee@tscientific||데이터코드, 이벤트코드, 메뉴코드 추가 / SMS 기능 추가 / VERSION_HISTORY 테이블 추가 / V_INFO_MCH 관련 뷰 변경"
VERSION_METADATA["1.1.1"]="2025-11-05|jhlee@tscientific||운영관리 - 파일 기능 관련 테이블 추가"



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
echo "  MariaDB 누적 패치 실행 스크립트"
echo "=========================================="
echo ""

# 실행 방식 선택
echo "패치 실행 방식을 선택하세요:"
echo "  1) 로컬 Docker 컨테이너"
echo "  2) 원격 MariaDB 서버"
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
    # ===== 로컬 Docker 모드 =====
    log_info "실행 모드: 로컬 Docker 컨테이너"
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
    
    # Docker 명령 함수
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
    # ===== 원격 MariaDB 모드 =====
    log_info "실행 모드: 원격 MariaDB 서버"
    echo ""
    
    # 원격 서버 정보 입력
    read -p "MariaDB 호스트 주소: " DB_HOST
    if [ -z "$DB_HOST" ]; then
        log_error "호스트 주소는 필수입니다."
        exit 1
    fi
    
    read -p "MariaDB 포트 [13306]: " DB_PORT
    DB_PORT=${DB_PORT:-13306}
    
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
    log_info "MariaDB 연결 테스트 중..."
    mariadb -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -e "SELECT 1;" > /dev/null 2>&1
    if [ $? -ne 0 ]; then
        log_error "MariaDB 접속에 실패했습니다. 접속 정보를 확인해주세요."
        log_warning "mariadb 클라이언트가 설치되어 있는지 확인하세요."
        exit 1
    fi
    
    log_info "MariaDB 접속 성공!"
    
    # 원격 명령 함수
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

# 패치 적용자 정보 입력
read -p "패치 적용자 이메일: " PATCH_EXECUTOR
if [ -z "$PATCH_EXECUTOR" ]; then
    log_error "패치 적용자 이메일은 필수입니다."
    exit 1
fi
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

# 버전 디렉토리 목록 가져오기 (버전 순서로 정렬)
VERSION_DIRS=($(find "$SQL_DIR" -maxdepth 1 -type d ! -path "$SQL_DIR" | sort -V))

if [ ${#VERSION_DIRS[@]} -eq 0 ]; then
    log_error "SQL 파일 디렉토리를 찾을 수 없습니다: $SQL_DIR"
    exit 1
fi

log_info "실행할 버전: ${#VERSION_DIRS[@]}개"
echo ""

# 확인 메시지
log_warning "⚠️  이 스크립트는 여러 버전의 변경사항을 순차적으로 적용합니다."
log_warning "⚠️  실행 전 반드시 데이터베이스를 백업하셨는지 확인하세요!"
echo ""
read -p "계속 진행하시겠습니까? (y/n): " CONFIRM
if [[ ! $CONFIRM =~ ^[Yy]$ ]]; then
    log_warning "사용자에 의해 취소되었습니다."
    exit 0
fi

echo ""
echo "=========================================="
log_info "패치 실행 시작..."
echo "=========================================="

SUCCESS_COUNT=0
FAIL_COUNT=0
TOTAL_FILES=0

# 각 버전 디렉토리의 SQL 파일 실행
for version_dir in "${VERSION_DIRS[@]}"; do
    version_name=$(basename "$version_dir")
    
    echo ""
    echo "=========================================="
    log_info "버전 $version_name 패치 실행 중..."
    echo "=========================================="
    
    # 버전 디렉토리 내의 SQL 파일들을 순서대로 실행
    while IFS= read -r -d '' sql_file; do
        file_name=$(basename "$sql_file")
        TOTAL_FILES=$((TOTAL_FILES + 1))

        log_info "  [$version_name] $file_name 실행 중..."

        echo "" >> "$LOG_FILE"
        echo "=========================================" >> "$LOG_FILE"
        echo "버전: $version_name" >> "$LOG_FILE"
        echo "파일: $file_name" >> "$LOG_FILE"
        echo "시작 시간: $(date '+%Y-%m-%d %H:%M:%S')" >> "$LOG_FILE"
        echo "=========================================" >> "$LOG_FILE"

        START_TIME=$(date +%s)

        # 임시 출력 파일 생성
        TEMP_OUTPUT="${LOG_DIR}/temp_output_$${BASHPID}_${version_name}_${file_name}.log"

        # SQL 실행 (Docker 또는 원격)
        execute_sql "$sql_file" > "$TEMP_OUTPUT" 2>&1
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
        echo "=========================================" >> "$LOG_FILE"

        if [ $EXIT_CODE -eq 0 ]; then
            log_info "  ✓ 성공 (${ELAPSED}초)"
            SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
            rm -f "$TEMP_OUTPUT"
        else
            log_error "  ✗ 실패 (종료 코드: $EXIT_CODE)"
            FAIL_COUNT=$((FAIL_COUNT + 1))

            # 오류 메시지 상세 출력
            echo "" >> "$LOG_FILE"
            echo "========== 오류 상세 정보 ==========" >> "$LOG_FILE"

            if grep -i "ERROR" "$TEMP_OUTPUT" > /dev/null 2>&1; then
                grep -i "ERROR" "$TEMP_OUTPUT" >> "$LOG_FILE"
                echo "" >> "$LOG_FILE"

                echo ""
                log_error "발견된 오류:"
                grep -i "ERROR" "$TEMP_OUTPUT"
                echo ""
            else
                echo "구체적인 오류 메시지를 찾을 수 없습니다." >> "$LOG_FILE"
                echo "전체 출력은 위 로그를 참조하세요." >> "$LOG_FILE"

                echo ""
                log_error "실행 결과 (마지막 20줄):"
                tail -20 "$TEMP_OUTPUT"
                echo ""
            fi
            echo "====================================" >> "$LOG_FILE"

            rm -f "$TEMP_OUTPUT"

            # 실패 시 계속 진행할지 확인
            echo ""
            read -p "오류가 발생했습니다. 계속 진행하시겠습니까? (y/n): " CONTINUE
            if [[ ! $CONTINUE =~ ^[Yy]$ ]]; then
                log_warning "사용자에 의해 중단되었습니다."
                echo "사용자에 의해 중단됨" >> "$LOG_FILE"
                break 2
            fi
        fi

    done < <(find "$version_dir" -maxdepth 1 -name "*.sql" -type f -print0 | sort -zV)
    
    # 해당 버전의 모든 SQL 실행 완료 후 VERSION_HISTORY INSERT
    if [ ${VERSION_METADATA["$version_name"]+isset} ]; then
        log_info "  [$version_name] VERSION_HISTORY 등록 중..."
        
        # 메타데이터 파싱
        IFS='|' read -r created_at created_by custom_version comment <<< "${VERSION_METADATA[$version_name]}"
        
        # CUSTOM_VERSION 처리
        if [ -z "$custom_version" ]; then
            custom_version_sql="NULL"
        else
            custom_version_sql="'$custom_version'"
        fi
        
        # VERSION_HISTORY INSERT SQL 생성
        VERSION_INSERT_SQL="INSERT INTO NMS_DB.VERSION_HISTORY (
    VERSION_ID, 
    STANDARD_VERSION, 
    CUSTOM_VERSION, 
    VERSION_CREATED_AT, 
    VERSION_CREATED_BY, 
    SYSTEM_APPLIED_BY, 
    SYSTEM_APPLIED_AT, 
    COMMENT
) VALUES (
    '$version_name',
    '$version_name',
    $custom_version_sql,
    '$created_at',
    '$created_by',
    '$PATCH_EXECUTOR',
    CURRENT_TIMESTAMP(),
    '$comment'
);"
        
        echo "" >> "$LOG_FILE"
        echo "=========================================" >> "$LOG_FILE"
        echo "버전: $version_name - VERSION_HISTORY 등록" >> "$LOG_FILE"
        echo "시작 시간: $(date '+%Y-%m-%d %H:%M:%S')" >> "$LOG_FILE"
        echo "=========================================" >> "$LOG_FILE"
        
        # VERSION_HISTORY INSERT 실행
        TEMP_OUTPUT="${LOG_DIR}/temp_version_history_${version_name}.log"
        execute_sql_string "$VERSION_INSERT_SQL" > "$TEMP_OUTPUT" 2>&1
        EXIT_CODE=$?
        
        cat "$TEMP_OUTPUT" >> "$LOG_FILE"
        echo "" >> "$LOG_FILE"
        echo "종료 코드: $EXIT_CODE" >> "$LOG_FILE"
        echo "=========================================" >> "$LOG_FILE"
        
        if [ $EXIT_CODE -eq 0 ]; then
            log_info "  ✓ VERSION_HISTORY 등록 완료"
            rm -f "$TEMP_OUTPUT"
        else
            log_error "  ✗ VERSION_HISTORY 등록 실패 (버전: $version_name)"
            if grep -i "ERROR" "$TEMP_OUTPUT" > /dev/null 2>&1; then
                grep -i "ERROR" "$TEMP_OUTPUT"
            fi
            rm -f "$TEMP_OUTPUT"
            FAIL_COUNT=$((FAIL_COUNT + 1))
            
            # 실패 시 계속 진행할지 확인
            echo ""
            read -p "VERSION_HISTORY 등록 실패. 계속 진행하시겠습니까? (y/n): " CONTINUE
            if [[ ! $CONTINUE =~ ^[Yy]$ ]]; then
                log_warning "사용자에 의해 중단되었습니다."
                break
            fi
        fi
    fi
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
    log_info "✓ 모든 패치가 성공적으로 적용되었습니다!"
    exit 0
else
    log_warning "일부 패치 적용에 실패했습니다. 로그 파일을 확인해주세요."
    exit 1
fi
