# Release Manager 배포 가이드

## 사전 준비사항

### Docker Network 생성

```bash
# network-ts 네트워크가 없으면 생성
docker network create network-ts
```

## CI/CD 배포

### GitLab CI/CD Pipeline

`.gitlab-ci.yml` 파이프라인이 자동으로 실행됩니다:

1. **build**: Gradle 빌드
2. **docker-build**: Docker 이미지 빌드
3. **deploy**: GitLab Runner 호스트에 배포

### 수동 배포 (필요 시)

```bash
# 1. 저장소 클론 또는 최신 코드 pull
cd /path/to/release-manager
git pull origin main

# 2. 빌드
./gradlew clean build -x test

# 3. Docker 이미지 빌드
cd docker
docker compose build

# 4. 컨테이너 재시작
docker compose down
docker compose up -d

# 5. 로그 확인
docker compose logs -f app
```

## 디렉토리 구조

```
/data/release_manager/
├── release_files/              # 릴리즈 파일 저장소
│   ├── versions/              # 버전별 파일
│   │   ├── standard/          # 표준 릴리즈
│   │   │   └── 1.1.x/
│   │   │       └── 1.1.3/
│   │   └── custom/            # 커스텀 릴리즈
│   │       └── company_a/
│   ├── patches/               # 생성된 패치
│   └── release_metadata.json  # 버전 메타데이터
├── mariadb/
│   ├── lib/                   # 데이터베이스 파일
│   └── log/                   # 로그 파일
└── redis/
    └── data/                  # Redis 데이터
```

## Health Check

### 애플리케이션 상태 확인

```bash
# Docker 컨테이너 상태
docker compose ps

# 애플리케이션 헬스체크
curl http://localhost:8081/actuator/health

# 로그 확인
docker compose logs -f app
```

### 예상 응답

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "redis": {
      "status": "UP"
    }
  }
}
```

## 트러블슈팅

### 컨테이너가 시작되지 않음

```bash
# 로그 확인
docker compose logs app

# 컨테이너 상세 정보
docker inspect release-manager-api

# 볼륨 마운트 확인
docker inspect release-manager-api | grep -A 10 "Mounts"
```

### 디스크 용량 부족

```bash
# 디스크 사용량 확인
df -h /data

# Docker 정리
docker system prune -a --volumes
```

### 데이터베이스 연결 실패

```bash
# MariaDB 컨테이너 상태 확인
docker compose ps mariadb

# MariaDB 로그 확인
docker compose logs mariadb

# 직접 연결 테스트
docker exec -it release-manager-mariadb mysql -u root -p
```

## 백업 및 복구

### 백업

```bash
# 릴리즈 파일 백업
sudo tar -czf release_files_backup_$(date +%Y%m%d).tar.gz /data/release_manager/release_files

# 데이터베이스 백업
docker exec release-manager-mariadb mysqldump -u root -p${MARIADB_ROOT_PASSWORD} release_manager > backup_$(date +%Y%m%d).sql
```

### 복구

```bash
# 릴리즈 파일 복구
sudo tar -xzf release_files_backup_20241202.tar.gz -C /

# 데이터베이스 복구
docker exec -i release-manager-mariadb mysql -u root -p${MARIADB_ROOT_PASSWORD} release_manager < backup_20241202.sql
```

## 보안 고려사항

1. **파일 권한**: 755 (읽기/실행 모든 사용자, 쓰기는 소유자만)
2. **컨테이너 사용자**: root가 아닌 spring 사용자로 실행
3. **네트워크**: 내부 Docker 네트워크(network-ts) 사용
4. **환경변수**: `.env` 파일로 관리, Git에 커밋하지 않음

## 문의

문제 발생 시:
1. 로그 확인: `docker compose logs -f app`
2. 권한 확인: `ls -la /data/release_manager/`
3. 이슈 등록: GitLab Issues
