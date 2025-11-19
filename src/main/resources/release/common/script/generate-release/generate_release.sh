#!/bin/bash

################################################################################
# Infraeye2 누적 패치 생성 스크립트
#
# 사용법:
#   ./generate_release.sh [--type <standard|custom>] [--customer <customer_id>] [--from <version>] [--to <version>]
#   또는 대화형으로 실행
#
# 예시:
#   ./generate_release.sh --type standard --from 1.0.2 --to 1.1.3
#   ./generate_release.sh --type custom --customer companyA --from 1.0.0 --to 1.1.1
#   ./generate_release.sh (대화형)
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

# 스크립트 디렉토리 경로
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"
# RELEASES_DIR는 main() 함수에서 TYPE에 따라 동적으로 설정됨

# 버전 비교 함수 (v1 < v2 이면 -1, v1 == v2 이면 0, v1 > v2 이면 1 반환)
version_compare() {
    local v1=$1
    local v2=$2
    
    # 버전을 배열로 분리 (1.0.2 -> [1, 0, 2])
    IFS='.' read -ra V1_PARTS <<< "$v1"
    IFS='.' read -ra V2_PARTS <<< "$v2"
    
    # 각 파트 비교
    for i in 0 1 2; do
        local part1=${V1_PARTS[$i]:-0}
        local part2=${V2_PARTS[$i]:-0}
        
        if [ "$part1" -lt "$part2" ]; then
            echo -1
            return
        elif [ "$part1" -gt "$part2" ]; then
            echo 1
            return
        fi
    done
    
    echo 0
}

# 버전이 유효한지 확인
validate_version() {
    local version=$1
    
    # 형식 검증: X.Y.Z
    if ! [[ $version =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
        log_error "버전 형식이 올바르지 않습니다: $version (예: 1.0.0)"
        return 1
    fi
    
    return 0
}

# 버전 디렉토리가 존재하는지 확인
check_version_exists() {
    local version=$1
    IFS='.' read -ra PARTS <<< "$version"
    local major=${PARTS[0]}
    local minor=${PARTS[1]}
    local patch=${PARTS[2]}
    
    local version_dir="$RELEASES_DIR/${major}.${minor}.x/${version}"
    
    if [ ! -d "$version_dir" ]; then
        return 1
    fi
    
    return 0
}

# 사용 가능한 모든 버전 목록 가져오기
get_all_versions() {
    local versions=()
    
    # X.Y.x 디렉토리 탐색
    for major_minor_dir in "$RELEASES_DIR"/*.*.x; do
        if [ -d "$major_minor_dir" ]; then
            # 하위 버전 디렉토리 탐색
            for version_dir in "$major_minor_dir"/*.*.*; do
                if [ -d "$version_dir" ]; then
                    local version=$(basename "$version_dir")
                    versions+=("$version")
                fi
            done
        fi
    done
    
    # 버전 정렬
    IFS=$'\n' sorted_versions=($(sort -V <<< "${versions[*]}"))
    unset IFS
    
    echo "${sorted_versions[@]}"
}

# from 버전과 to 버전 사이의 모든 버전 가져오기
get_versions_between() {
    local from_version=$1
    local to_version=$2
    local between_versions=()
    
    local all_versions=($(get_all_versions))
    
    for version in "${all_versions[@]}"; do
        local cmp_from=$(version_compare "$version" "$from_version")
        local cmp_to=$(version_compare "$version" "$to_version")
        
        # from < version <= to
        if [ "$cmp_from" -eq 1 ] && [ "$cmp_to" -le 0 ]; then
            between_versions+=("$version")
        fi
    done
    
    echo "${between_versions[@]}"
}

# 특정 버전의 패치 디렉토리 찾기 (patch 폴더만 - 개발자가 생성한 신규 패치)
find_patch_dirs_for_version() {
    local version=$1
    IFS='.' read -ra PARTS <<< "$version"
    local major=${PARTS[0]}
    local minor=${PARTS[1]}
    
    local version_dir="$RELEASES_DIR/${major}.${minor}.x/${version}"
    
    # patch/ 디렉토리만 찾기 (from-*는 스크립트가 생성한 결과물이므로 제외)
    local patch_dirs=()
    
    if [ -d "$version_dir/patch" ]; then
        patch_dirs+=("$version_dir/patch")
    fi
    
    echo "${patch_dirs[@]}"
}

# patch_note.md에서 특정 버전의 메타데이터 추출
parse_version_metadata() {
    local version=$1
    local patch_note_file="$RELEASES_DIR/patch_note.md"
    
    if [ ! -f "$patch_note_file" ]; then
        log_warning "patch_note.md를 찾을 수 없습니다: $patch_note_file"
        return 1
    fi
    
    # VERSION: X.Y.Z로 시작하는 섹션 추출
    local section=$(sed -n "/^VERSION: ${version}[[:space:]]*$/,/^=====/p" "$patch_note_file" | sed '/^=====/d')
    
    if [ -z "$section" ]; then
        log_warning "버전 $version의 메타데이터를 찾을 수 없습니다."
        return 1
    fi
    
    # 각 필드 추출
    local created_at=$(echo "$section" | grep "^CREATED_AT:" | sed 's/^CREATED_AT:[[:space:]]*//')
    local created_by=$(echo "$section" | grep "^CREATED_BY:" | sed 's/^CREATED_BY:[[:space:]]*//')
    local custom_version=$(echo "$section" | grep "^CUSTOM_VERSION:" | sed 's/^CUSTOM_VERSION:[[:space:]]*//')
    local comment=$(echo "$section" | grep "^COMMENT:" | sed 's/^COMMENT:[[:space:]]*//')
    
    # 출력 (bash associative array 대신 구분자로 반환)
    echo "${version}|${created_at}|${created_by}|${custom_version}|${comment}"
}

# SQL 파일 병합
merge_sql_files() {
    local db_type=$1  # mariadb 또는 cratedb
    local versions=("${@:2}")
    local output_dir=$3
    
    log_step "[$db_type] SQL 파일 병합 시작..."
    
    if [ "$db_type" == "mariadb" ]; then
        merge_mariadb_files "${versions[@]}"
    elif [ "$db_type" == "cratedb" ]; then
        merge_cratedb_files "${versions[@]}"
    fi
}

# MariaDB SQL 파일 복사 (버전별 디렉토리 구조 유지)
merge_mariadb_files() {
    local versions=("$@")
    local output_sql_dir="$OUTPUT_DIR/mariadb/source_files"
    
    # 출력 디렉토리 생성
    mkdir -p "$output_sql_dir"
    
    local total_files=0
    
    # 각 버전의 SQL 파일 복사 (버전별 디렉토리로)
    for version in "${versions[@]}"; do
        log_info "  버전 $version의 MariaDB SQL 파일 복사 중..."
        
        local version_dir="$output_sql_dir/$version"
        mkdir -p "$version_dir"
        
        local patch_dirs=($(find_patch_dirs_for_version "$version"))
        local version_file_count=0
        
        for patch_dir in "${patch_dirs[@]}"; do
            local mariadb_dir="$patch_dir/mariadb"
            
            if [ ! -d "$mariadb_dir" ]; then
                continue
            fi
            
            # mariadb/ 디렉토리 내 모든 .sql 파일 복사 (파일명 순서 유지)
            while IFS= read -r -d '' file_path; do
                local sql_file=$(basename "$file_path")
                
                # 버전별 디렉토리에 파일 복사
                cp "$file_path" "$version_dir/"
                
                version_file_count=$((version_file_count + 1))
                total_files=$((total_files + 1))
                
                log_info "    ✓ $sql_file 복사됨"
                
            done < <(find "$mariadb_dir" -maxdepth 1 -name "*.sql" -type f -print0 | sort -zV)
        done
        
        if [ $version_file_count -gt 0 ]; then
            log_success "  버전 $version: ${version_file_count}개 파일 복사 완료"
        fi
    done
    
    log_success "  총 ${total_files}개의 SQL 파일이 버전별로 복사됨"
}

# CrateDB SQL 파일 복사 (버전별 디렉토리 구조 유지)
merge_cratedb_files() {
    local versions=("$@")
    local output_sql_dir="$OUTPUT_DIR/cratedb/source_files"
    
    # 출력 디렉토리 생성
    mkdir -p "$output_sql_dir"
    
    local total_files=0
    
    # 각 버전의 SQL 파일 복사 (버전별 디렉토리로)
    for version in "${versions[@]}"; do
        log_info "  버전 $version의 CrateDB SQL 파일 복사 중..."
        
        local version_dir="$output_sql_dir/$version"
        mkdir -p "$version_dir"
        
        local patch_dirs=($(find_patch_dirs_for_version "$version"))
        local version_file_count=0
        
        for patch_dir in "${patch_dirs[@]}"; do
            local cratedb_dir="$patch_dir/cratedb"
            
            if [ ! -d "$cratedb_dir" ]; then
                continue
            fi
            
            # cratedb/ 디렉토리 내 모든 .sql 파일 복사 (파일명 순서 유지)
            while IFS= read -r -d '' file_path; do
                local sql_file=$(basename "$file_path")
                
                # 버전별 디렉토리에 파일 복사
                cp "$file_path" "$version_dir/"
                
                version_file_count=$((version_file_count + 1))
                total_files=$((total_files + 1))
                
                log_info "    ✓ $sql_file 복사됨"
                
            done < <(find "$cratedb_dir" -maxdepth 1 -name "*.sql" -type f -print0 | sort -zV)
        done
        
        if [ $version_file_count -gt 0 ]; then
            log_success "  버전 $version: ${version_file_count}개 파일 복사 완료"
        fi
    done
    
    if [ $total_files -gt 0 ]; then
        log_success "  총 ${total_files}개의 SQL 파일이 버전별로 복사됨"
    fi
}

# 패치 스크립트 생성 (동적 SQL 파일 목록 포함)
generate_patch_scripts() {
    log_step "패치 스크립트 생성 중..."
    
    # MariaDB 패치 스크립트 생성
    generate_mariadb_patch_script
    
    # CrateDB 패치 스크립트 생성
    generate_cratedb_patch_script
}

# MariaDB 패치 스크립트 생성
generate_mariadb_patch_script() {
    local script_file="$OUTPUT_DIR/mariadb/mariadb_patch.sh"
    
    # 각 버전의 메타데이터 수집
    log_info "  버전 메타데이터 수집 중..."
    local version_metadata_array=()
    for version in "${MERGED_VERSIONS[@]}"; do
        local metadata=$(parse_version_metadata "$version")
        if [ $? -eq 0 ] && [ -n "$metadata" ]; then
            version_metadata_array+=("$metadata")
            log_info "    ✓ $version 메타데이터 추출 완료"
        else
            log_warning "    ⚠ $version 메타데이터를 찾을 수 없습니다."
        fi
    done
    
    # 패치 스크립트 생성 (버전별 SQL 파일 순차 실행 + VERSION_HISTORY INSERT)
    cat > "$script_file" << 'SCRIPT_HEADER'
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
SCRIPT_HEADER

    # 메타데이터 배열을 bash declare 형태로 추가
    echo "declare -A VERSION_METADATA" >> "$script_file"
    for metadata in "${version_metadata_array[@]}"; do
        IFS='|' read -r version created_at created_by custom_version comment <<< "$metadata"
        # 작은따옴표를 이스케이프 처리
        comment_escaped=$(echo "$comment" | sed "s/'/'\\\''/g")
        created_by_escaped=$(echo "$created_by" | sed "s/'/'\\\''/g")
        custom_version_escaped=$(echo "$custom_version" | sed "s/'/'\\\''/g")
        
        echo "VERSION_METADATA[\"$version\"]=\"$created_at|$created_by_escaped|$custom_version_escaped|$comment_escaped\"" >> "$script_file"
    done
    echo "" >> "$script_file"
    
    # 나머지 스크립트 계속
    cat >> "$script_file" << 'EOF'


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
EOF

    chmod +x "$script_file"
    log_success "  mariadb_patch.sh 생성 완료"
}

# CrateDB 패치 스크립트 생성
generate_cratedb_patch_script() {
    local script_file="$OUTPUT_DIR/cratedb/cratedb_patch.sh"
    
    # 패치 스크립트 생성 (버전별 SQL 파일 순차 실행)
    cat > "$script_file" << 'EOF'
#!/bin/bash

################################################################################
# CrateDB 패치 스크립트 (누적 패치용 - 자동 생성됨)
# 
# 사용법: ./cratedb_patch.sh
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
DEFAULT_DB_HOST="localhost"
DEFAULT_DB_PORT="15432"

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
echo "  CrateDB 누적 패치 실행 스크립트"
echo "=========================================="
echo ""

# 실행 방식 선택
echo "패치 실행 방식을 선택하세요:"
echo "  1) 로컬 Docker 컨테이너"
echo "  2) 원격 CrateDB 서버"
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
    
    # 연결 테스트
    log_info "CrateDB 연결 테스트 중..."
    docker exec "$DOCKER_CONTAINER_NAME" crash -c "SELECT 1;" > /dev/null 2>&1
    if [ $? -ne 0 ]; then
        log_error "CrateDB 연결에 실패했습니다."
        exit 1
    fi
    
    log_info "CrateDB 연결 성공!"

else
    # ===== 원격 모드 =====
    log_info "실행 모드: 원격 CrateDB 서버"
    echo ""
    
    # CrateDB 접속 정보 입력
    read -p "CrateDB 호스트 주소: " DB_HOST
    
    read -p "CrateDB 포트 [$DEFAULT_DB_PORT]: " DB_PORT
    DB_PORT=${DB_PORT:-$DEFAULT_DB_PORT}
    
    if [ -z "$DB_HOST" ]; then
        log_error "호스트 주소를 입력해주세요."
        exit 1
    fi
    
    echo ""
    log_info "호스트: $DB_HOST:$DB_PORT"
    
    # 연결 테스트
    log_info "CrateDB 연결 테스트 중..."
    crash --hosts "$DB_HOST:$DB_PORT" -c "SELECT 1;" > /dev/null 2>&1
    if [ $? -ne 0 ]; then
        log_error "CrateDB 접속에 실패했습니다. 접속 정보를 확인해주세요."
        log_warning "crash 클라이언트가 설치되어 있는지 확인하세요."
        exit 1
    fi
    
    log_info "CrateDB 접속 성공!"
fi

echo ""

# 로그 디렉토리 생성
LOG_DIR="logs"
if [ ! -d "$LOG_DIR" ]; then
    mkdir -p "$LOG_DIR"
    log_info "로그 디렉토리 생성: $LOG_DIR"
fi

# 로그 파일 생성
LOG_FILE="${LOG_DIR}/patch_cratedb_$(date +%Y%m%d_%H%M%S).log"
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

        # 실행 모드에 따라 SQL 실행
        if [ "$EXECUTION_MODE" = "1" ]; then
            # Docker 모드: 컨테이너 내부에서 crash 실행
            docker exec -i "$DOCKER_CONTAINER_NAME" crash --verbose < "$sql_file" > "$TEMP_OUTPUT" 2>&1
            EXIT_CODE=$?
        else
            # 원격 모드: crash 클라이언트로 직접 실행
            crash --hosts "$DB_HOST:$DB_PORT" --verbose < "$sql_file" > "$TEMP_OUTPUT" 2>&1
            EXIT_CODE=$?
        fi

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

            if grep -i "ERROR\|Exception\|FAILED" "$TEMP_OUTPUT" > /dev/null 2>&1; then
                grep -i "ERROR\|Exception\|FAILED" "$TEMP_OUTPUT" >> "$LOG_FILE"
                echo "" >> "$LOG_FILE"

                echo ""
                log_error "발견된 오류:"
                grep -i "ERROR\|Exception\|FAILED" "$TEMP_OUTPUT"
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
EOF

    chmod +x "$script_file"
    log_success "  cratedb_patch.sh 생성 완료"
}

# README 생성
generate_readme() {
    log_step "README 생성 중..."
    
    local readme_file="$OUTPUT_DIR/README.md"
    
    cat > "$readme_file" << EOF
# 누적 패치: from-${FROM_VERSION} to ${TO_VERSION}
$(if [ "$TYPE" == "custom" ]; then echo "## 고객사: $CUSTOMER_ID"; fi)

## 개요
이 패치는 **${FROM_VERSION}** 버전에서 **${TO_VERSION}** 버전으로 업그레이드하기 위한 누적 패치입니다.
$(if [ "$TYPE" == "custom" ]; then echo "고객사 **$CUSTOMER_ID** 전용 패치입니다."; fi)

## 생성 정보
- **생성일**: $(date '+%Y-%m-%d %H:%M:%S')
- **From Version**: ${FROM_VERSION}
- **To Version**: ${TO_VERSION}
- **포함된 버전**: ${MERGED_VERSIONS[@]}

## 디렉토리 구조
\`\`\`
from-${FROM_VERSION}/
├── mariadb/
│   ├── mariadb_patch.sh        # MariaDB 패치 실행 스크립트
│   └── source_files/           # 누적된 SQL 파일들
├── cratedb/
│   ├── cratedb_patch.sh        # CrateDB 패치 실행 스크립트
│   └── source_files/           # 누적된 SQL 파일들
└── README.md                   # 이 파일
\`\`\`

## 사용 방법

### 사전 준비
1. **백업 필수**: 패치 실행 전 반드시 데이터베이스를 백업하세요.
   \`\`\`bash
   # MariaDB 백업
   cd mariadb
   ../../../common/script/mariadb/mariadb_backup.sh
   \`\`\`

2. **버전 확인**: 현재 시스템 버전이 ${FROM_VERSION}인지 확인하세요.    
   \`\`\`sql
    SELECT * FROM NMS_DB.VERSION_HISTORY;
    \`\`\`

### 패치 실행

#### MariaDB 패치
\`\`\`bash
cd mariadb
chmod +x mariadb_patch.sh
./mariadb_patch.sh
\`\`\`

#### CrateDB 패치
\`\`\`bash
cd cratedb
chmod +x cratedb_patch.sh
./cratedb_patch.sh
\`\`\`

## 주의사항
⚠️ **중요**: 이 패치는 여러 버전의 변경사항을 누적한 것입니다.
- 패치 실행 전 반드시 백업을 수행하세요.
- 패치 실행 중 오류 발생 시 로그를 확인하세요.

## 포함된 패치 버전 목록

EOF

    # 각 버전별 변경사항 추가
    for version in "${MERGED_VERSIONS[@]}"; do
        echo "### Version ${version}" >> "$readme_file"
        
        # patch_note.md에서 해당 버전 정보 추출 (선택적)
        if [ -f "$RELEASES_DIR/patch_note.md" ]; then
            # VERSION: X.Y.Z로 시작하는 라인부터 다음 구분자(=====) 전까지 추출 (구분자 제외)
            local version_info=$(sed -n "/^VERSION: ${version}[[:space:]]*$/,/^=====/{/^=====/d;p;}" "$RELEASES_DIR/patch_note.md")
            if [ -n "$version_info" ]; then
                echo "\`\`\`" >> "$readme_file"
                echo "$version_info" >> "$readme_file"
                echo "\`\`\`" >> "$readme_file"
            fi
        fi
        
        echo "" >> "$readme_file"
    done
    
    cat >> "$readme_file" << EOF

## 문제 발생 시
1. 로그 파일 확인: \`mariadb/logs/\`, \`cratedb/logs/\`
2. 백업으로 복구
3. 개발팀에 문의

---
CREATED BY. Infraeye2 누적 패치 생성기
EOF

    log_success "README.md 생성 완료"
}

# 메인 함수
main() {
    echo "=========================================="
    echo "  Infraeye2 누적 패치 생성기"
    echo "=========================================="
    echo ""
    
    # 파라미터 파싱
    TYPE="standard"  # 기본값: standard
    CUSTOMER_ID=""
    FROM_VERSION=""
    TO_VERSION=""

    while [[ $# -gt 0 ]]; do
        case $1 in
            --type)
                TYPE="$2"
                shift 2
                ;;
            --customer)
                CUSTOMER_ID="$2"
                shift 2
                ;;
            --from)
                FROM_VERSION="$2"
                shift 2
                ;;
            --to)
                TO_VERSION="$2"
                shift 2
                ;;
            *)
                log_error "알 수 없는 옵션: $1"
                echo "사용법: $0 [--type <standard|custom>] [--customer <customer_id>] [--from <version>] [--to <version>]"
                exit 1
                ;;
        esac
    done

    # TYPE 유효성 검증
    if [ "$TYPE" != "standard" ] && [ "$TYPE" != "custom" ]; then
        log_error "TYPE은 'standard' 또는 'custom'이어야 합니다."
        exit 1
    fi

    # custom 타입일 때 CUSTOMER_ID 필수
    if [ "$TYPE" == "custom" ] && [ -z "$CUSTOMER_ID" ]; then
        log_error "--type custom 사용 시 --customer 옵션이 필요합니다."
        exit 1
    fi

    # RELEASES_DIR 동적 설정
    if [ "$TYPE" == "standard" ]; then
        RELEASES_DIR="$PROJECT_ROOT/releases/standard"
    else
        RELEASES_DIR="$PROJECT_ROOT/releases/custom/$CUSTOMER_ID"
    fi

    # 디렉토리 존재 확인
    if [ ! -d "$RELEASES_DIR" ]; then
        log_error "디렉토리를 찾을 수 없습니다: $RELEASES_DIR"
        if [ "$TYPE" == "custom" ]; then
            log_error "고객사 '$CUSTOMER_ID'의 디렉토리가 없습니다."
            log_info "다음 명령으로 디렉토리를 생성하세요:"
            log_info "  mkdir -p $RELEASES_DIR"
            log_info "  touch $RELEASES_DIR/patch_note.md"
        fi
        exit 1
    fi
    
    # 대화형 입력
    # 파라미터가 하나도 제공되지 않은 경우에만 대화형 모드
    if [[ $# -eq 0 ]] || [[ -z "$FROM_VERSION" ]] || [[ -z "$TO_VERSION" ]]; then
        # TYPE 입력 (파라미터로 제공되지 않은 경우)
        if [[ $# -eq 0 ]]; then
            read -p "패치 타입을 선택하세요 (standard/custom) [standard]: " INPUT_TYPE
            TYPE=${INPUT_TYPE:-standard}

            # TYPE 유효성 검증
            if [ "$TYPE" != "standard" ] && [ "$TYPE" != "custom" ]; then
                log_error "TYPE은 'standard' 또는 'custom'이어야 합니다."
                exit 1
            fi

            # custom인 경우 CUSTOMER_ID 입력
            if [ "$TYPE" == "custom" ]; then
                read -p "고객사 ID를 입력하세요: " CUSTOMER_ID
                if [ -z "$CUSTOMER_ID" ]; then
                    log_error "고객사 ID는 필수입니다."
                    exit 1
                fi
            fi

            # RELEASES_DIR 설정
            if [ "$TYPE" == "standard" ]; then
                RELEASES_DIR="$PROJECT_ROOT/releases/standard"
            else
                RELEASES_DIR="$PROJECT_ROOT/releases/custom/$CUSTOMER_ID"
            fi

            # 디렉토리 존재 확인
            if [ ! -d "$RELEASES_DIR" ]; then
                log_error "디렉토리를 찾을 수 없습니다: $RELEASES_DIR"
                if [ "$TYPE" == "custom" ]; then
                    log_error "고객사 '$CUSTOMER_ID'의 디렉토리가 없습니다."
                    log_info "다음 명령으로 디렉토리를 생성하세요:"
                    log_info "  mkdir -p $RELEASES_DIR"
                    log_info "  touch $RELEASES_DIR/patch_note.md"
                fi
                exit 1
            fi
        fi

        echo ""
        if [ "$TYPE" == "custom" ]; then
            log_info "고객사: $CUSTOMER_ID"
        fi
        log_info "패치 타입: $TYPE"
        echo ""
    fi

    if [ -z "$FROM_VERSION" ]; then
        echo "사용 가능한 버전:"
        local all_versions=($(get_all_versions))
        for version in "${all_versions[@]}"; do
            echo "  - $version"
        done
        echo ""
        read -p "고객사 현재 버전 (From Version): " FROM_VERSION
    fi

    if [ -z "$TO_VERSION" ]; then
        read -p "목표 패치 버전 (To Version): " TO_VERSION
    fi
    
    # 버전 유효성 검증
    log_step "버전 유효성 검증 중..."
    
    if ! validate_version "$FROM_VERSION"; then
        exit 1
    fi
    
    if ! validate_version "$TO_VERSION"; then
        exit 1
    fi
    
    # 버전 비교
    local cmp=$(version_compare "$FROM_VERSION" "$TO_VERSION")
    if [ "$cmp" -ge 0 ]; then
        log_error "From Version ($FROM_VERSION)은 To Version ($TO_VERSION)보다 작아야 합니다."
        exit 1
    fi
    
    # From Version 존재 여부 확인 (선택적)
    if ! check_version_exists "$FROM_VERSION"; then
        log_warning "From Version ($FROM_VERSION) 디렉토리가 존재하지 않습니다."
        log_warning "계속 진행하시겠습니까? 이 버전은 참조용으로만 사용됩니다."
        read -p "(y/n): " CONTINUE
        if [[ ! $CONTINUE =~ ^[Yy]$ ]]; then
            log_error "작업이 취소되었습니다."
            exit 1
        fi
    fi
    
    # 중간 버전 목록 가져오기 (TO_VERSION은 참조용, 실제 존재하지 않아도 됨)
    log_step "패치 버전 목록 수집 중..."
    MERGED_VERSIONS=($(get_versions_between "$FROM_VERSION" "$TO_VERSION"))
    
    if [ ${#MERGED_VERSIONS[@]} -eq 0 ]; then
        log_error "From Version ($FROM_VERSION)과 To Version ($TO_VERSION) 사이에 패치할 버전이 없습니다."
        log_error "실제 존재하는 버전들을 확인하세요."
        log_info "사용 가능한 버전:"
        local all_versions=($(get_all_versions))
        for version in "${all_versions[@]}"; do
            echo "  - $version"
        done
        exit 1
    fi
    
    log_success "버전 검증 완료"
    log_info "병합할 버전: ${MERGED_VERSIONS[@]}"
    echo ""
    
    # 실제 마지막 버전 확인 (병합할 버전 중 가장 높은 버전)
    ACTUAL_TO_VERSION="${MERGED_VERSIONS[-1]}"
    
    if [ "$ACTUAL_TO_VERSION" != "$TO_VERSION" ]; then
        log_warning "입력한 To Version ($TO_VERSION)이 실제로 존재하지 않습니다."
        log_warning "실제 마지막 버전 ($ACTUAL_TO_VERSION)으로 패치를 생성합니다."
        TO_VERSION="$ACTUAL_TO_VERSION"
    fi
    
    # 출력 디렉토리 설정
    IFS='.' read -ra TO_PARTS <<< "$TO_VERSION"
    local to_major=${TO_PARTS[0]}
    local to_minor=${TO_PARTS[1]}

    if [ "$TYPE" == "standard" ]; then
        OUTPUT_DIR="$PROJECT_ROOT/releases/standard/${to_major}.${to_minor}.x/${TO_VERSION}/from-${FROM_VERSION}"
    else
        OUTPUT_DIR="$PROJECT_ROOT/releases/custom/$CUSTOMER_ID/${to_major}.${to_minor}.x/${TO_VERSION}/from-${FROM_VERSION}"
    fi

    log_info "패치 타입: $TYPE"
    if [ "$TYPE" == "custom" ]; then
        log_info "고객사: $CUSTOMER_ID"
    fi
    log_info "출력 디렉토리: $OUTPUT_DIR"
    echo ""
    
    # 출력 디렉토리 확인
    if [ -d "$OUTPUT_DIR" ]; then
        log_warning "출력 디렉토리가 이미 존재합니다: $OUTPUT_DIR"
        read -p "덮어쓰시겠습니까? (y/n): " OVERWRITE
        if [[ ! $OVERWRITE =~ ^[Yy]$ ]]; then
            log_error "작업이 취소되었습니다."
            exit 1
        fi
        rm -rf "$OUTPUT_DIR"
    fi
    
    # 디렉토리 생성 (단계별로 안전하게)
    log_step "출력 디렉토리 생성 중..."
    
    # 1단계: 루트 디렉토리
    if ! mkdir -p "$OUTPUT_DIR"; then
        log_error "루트 디렉토리 생성 실패: $OUTPUT_DIR"
        exit 1
    fi
    
    # 2단계: mariadb 디렉토리
    if ! mkdir -p "$OUTPUT_DIR/mariadb"; then
        log_error "mariadb 디렉토리 생성 실패"
        exit 1
    fi
    
    # 3단계: mariadb/source_files 디렉토리
    if ! mkdir -p "$OUTPUT_DIR/mariadb/source_files"; then
        log_error "mariadb/source_files 디렉토리 생성 실패"
        exit 1
    fi
    
    # 4단계: cratedb 디렉토리
    if ! mkdir -p "$OUTPUT_DIR/cratedb"; then
        log_error "cratedb 디렉토리 생성 실패"
        exit 1
    fi
    
    # 5단계: cratedb/source_files 디렉토리
    if ! mkdir -p "$OUTPUT_DIR/cratedb/source_files"; then
        log_error "cratedb/source_files 디렉토리 생성 실패"
        exit 1
    fi
    
    log_success "디렉토리 생성 완료"
    
    # SQL 파일 병합
    echo ""
    log_step "SQL 파일 병합 시작..."
    
    merge_mariadb_files "${MERGED_VERSIONS[@]}"
    echo ""
    merge_cratedb_files "${MERGED_VERSIONS[@]}"
    
    # 패치 스크립트 생성
    echo ""
    generate_patch_scripts
    
    # README 생성
    echo ""
    generate_readme
    
    # 완료
    echo ""
    echo "=========================================="
    log_success "누적 패치 생성 완료!"
    echo "=========================================="
    echo ""
    log_info "출력 위치: $OUTPUT_DIR"
    log_info "포함된 버전: ${MERGED_VERSIONS[@]}"
    echo ""
    log_warning "⚠️ 패치 파일이 올바르게 생성되었는지 반드시 확인 후 사용하세요!"
    echo ""
}

# 스크립트 실행
main "$@"

