package com.ts.rm.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * 테스트 환경용 QueryDSL 설정
 *
 * <p>@SpringBootTest를 사용하는 통합 테스트에서 JPAQueryFactory를 제공합니다.
 * <p>@DataJpaTest는 이 설정을 사용하지 않으므로 충돌하지 않습니다.
 */
@TestConfiguration
public class TestQueryDslConfig {

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
