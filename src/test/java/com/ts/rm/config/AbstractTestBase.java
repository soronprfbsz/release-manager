package com.ts.rm.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 통합 테스트 기본 클래스
 *
 * <p>모든 @SpringBootTest 통합 테스트는 이 클래스를 상속받아야 합니다.
 * <p>이를 통해 test 프로파일이 자동으로 적용되어 실제 release 폴더를 보호합니다.
 *
 * <p><b>중요</b>: test 프로파일은 application-test.yml에서 다음을 설정합니다:
 * <ul>
 *   <li>app.release.base-path = build/test-release (테스트 전용 경로)</li>
 *   <li>H2 인메모리 데이터베이스 사용</li>
 *   <li>Flyway 비활성화 (ddl-auto: create-drop 사용)</li>
 * </ul>
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractTestBase {
    // 모든 통합 테스트가 상속받을 기본 설정
}
