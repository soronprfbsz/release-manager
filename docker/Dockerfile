# ========================================
# Runtime Only
# ========================================
FROM eclipse-temurin:17-jre-alpine

# 이미지 메타데이터 (dangling 이미지 식별용)
LABEL project="spring-boilerplate"
LABEL maintainer="ts-team"
LABEL version="1.0"

WORKDIR /app

# 타임존 설정
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone && \
    apk del tzdata

# 애플리케이션 사용자 생성 (보안)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# CI/CD build 스테이지에서 빌드된 JAR 복사
COPY build/libs/*.jar app.jar

# 헬스체크
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# 포트 노출
EXPOSE 8081 9090

# JVM 옵션 설정
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
