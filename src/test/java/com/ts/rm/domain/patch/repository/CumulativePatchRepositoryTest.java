package com.ts.rm.domain.patch.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.ts.rm.domain.patch.entity.CumulativePatch;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(CumulativePatchRepositoryTest.TestConfig.class)
@ActiveProfiles("test")
@DisplayName("CumulativePatch Repository 테스트")
class CumulativePatchRepositoryTest {

    @Autowired
    private CumulativePatchRepository cumulativePatchRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Test
    @DisplayName("누적 패치 이력 저장 테스트")
    void saveCumulativePatch() {
        // given
        CumulativePatch patch = CumulativePatch.builder()
                .releaseType("STANDARD")
                .fromVersion("1.0.0")
                .toVersion("1.1.1")
                .patchName("from-1.0.0")
                .outputPath("releases/standard/1.1.x/1.1.1/from-1.0.0")
                .generatedAt(LocalDateTime.now())
                .generatedBy("admin@tscientific.co.kr")
                .status("SUCCESS")
                .build();

        // when
        CumulativePatch saved = cumulativePatchRepository.save(patch);
        entityManager.flush();
        entityManager.clear();

        // then
        CumulativePatch found = cumulativePatchRepository.findById(saved.getCumulativePatchId())
                .orElseThrow();
        assertThat(found.getReleaseType()).isEqualTo("STANDARD");
        assertThat(found.getFromVersion()).isEqualTo("1.0.0");
        assertThat(found.getToVersion()).isEqualTo("1.1.1");
        assertThat(found.getStatus()).isEqualTo("SUCCESS");
    }

    @Test
    @DisplayName("버전 범위로 누적 패치 이력 조회 테스트")
    void findByVersionRange() {
        // given
        CumulativePatch patch1 = CumulativePatch.builder()
                .releaseType("STANDARD")
                .fromVersion("1.0.0")
                .toVersion("1.1.0")
                .patchName("from-1.0.0")
                .outputPath("releases/standard/1.1.x/1.1.0/from-1.0.0")
                .generatedAt(LocalDateTime.now())
                .generatedBy("admin@tscientific.co.kr")
                .status("SUCCESS")
                .build();

        CumulativePatch patch2 = CumulativePatch.builder()
                .releaseType("STANDARD")
                .fromVersion("1.0.0")
                .toVersion("1.1.1")
                .patchName("from-1.0.0")
                .outputPath("releases/standard/1.1.x/1.1.1/from-1.0.0")
                .generatedAt(LocalDateTime.now())
                .generatedBy("admin@tscientific.co.kr")
                .status("SUCCESS")
                .build();

        cumulativePatchRepository.save(patch1);
        cumulativePatchRepository.save(patch2);
        entityManager.flush();
        entityManager.clear();

        // when
        List<CumulativePatch> found = cumulativePatchRepository
                .findByVersionRange("STANDARD", "1.0.0", "1.1.1");

        // then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getToVersion()).isEqualTo("1.1.1");
    }

    @Test
    @DisplayName("실패한 누적 패치 이력 저장 테스트")
    void saveCumulativePatch_withError() {
        // given
        CumulativePatch patch = CumulativePatch.builder()
                .releaseType("STANDARD")
                .fromVersion("1.0.0")
                .toVersion("1.2.0")
                .patchName("from-1.0.0")
                .outputPath("releases/standard/1.2.x/1.2.0/from-1.0.0")
                .generatedAt(LocalDateTime.now())
                .generatedBy("admin@tscientific.co.kr")
                .status("FAILED")
                .errorMessage("버전 1.2.0이 존재하지 않습니다.")
                .build();

        // when
        CumulativePatch saved = cumulativePatchRepository.save(patch);
        entityManager.flush();
        entityManager.clear();

        // then
        CumulativePatch found = cumulativePatchRepository.findById(saved.getCumulativePatchId())
                .orElseThrow();
        assertThat(found.getStatus()).isEqualTo("FAILED");
        assertThat(found.getErrorMessage()).contains("존재하지 않습니다");
    }

    /**
     * QueryDSL 테스트용 설정
     */
    @org.springframework.boot.test.context.TestConfiguration
    @org.springframework.data.jpa.repository.config.EnableJpaAuditing
    static class TestConfig {
        @org.springframework.context.annotation.Bean
        public com.querydsl.jpa.impl.JPAQueryFactory jpaQueryFactory(
                jakarta.persistence.EntityManager entityManager) {
            return new com.querydsl.jpa.impl.JPAQueryFactory(entityManager);
        }
    }
}
