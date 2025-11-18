package com.sb.global.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * QueryDSL 설정 - JPAQueryFactory를 Bean으로 등록하여 QueryDSL 사용 가능하게 함 - test
 * profile에서는 비활성화 (DataJpaTest와 충돌 방지)
 */
@Configuration
@org.springframework.context.annotation.Profile("!test")
public class QueryDslConfig {

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
