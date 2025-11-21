package com.ts.rm.domain.releaseversion.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.ts.rm.domain.releaseversion.entity.ReleaseVersion;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(ReleaseVersionRepositoryCustomTest.TestConfig.class)
@ActiveProfiles("test")
@DisplayName("ReleaseVersion Custom Repository 테스트")
class ReleaseVersionRepositoryCustomTest {

    @Autowired
    private ReleaseVersionRepository releaseVersionRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Test
    @DisplayName("버전 범위 조회 테스트 - From 1.0.0 To 1.1.1")
    void findVersionsBetween() {
        // given
        ReleaseVersion v100 = createAndSaveVersion("1.0.0");
        ReleaseVersion v110 = createAndSaveVersion("1.1.0");
        ReleaseVersion v111 = createAndSaveVersion("1.1.1");
        ReleaseVersion v120 = createAndSaveVersion("1.2.0");

        entityManager.flush();
        entityManager.clear();

        // when
        List<ReleaseVersion> result = releaseVersionRepository
                .findVersionsBetween("STANDARD", "1.0.0", "1.1.1");

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getVersion()).isEqualTo("1.1.0");
        assertThat(result.get(1).getVersion()).isEqualTo("1.1.1");
    }

    @Test
    @DisplayName("버전 범위 조회 테스트 - 중간 버전이 없는 경우")
    void findVersionsBetween_noVersions() {
        // given
        ReleaseVersion v100 = createAndSaveVersion("1.0.0");
        ReleaseVersion v120 = createAndSaveVersion("1.2.0");

        entityManager.flush();
        entityManager.clear();

        // when
        List<ReleaseVersion> result = releaseVersionRepository
                .findVersionsBetween("STANDARD", "1.0.0", "1.1.0");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("버전 범위 조회 테스트 - 버전 정렬 확인")
    void findVersionsBetween_orderCheck() {
        // given
        createAndSaveVersion("1.0.0");
        createAndSaveVersion("1.2.0");
        createAndSaveVersion("1.1.0");
        createAndSaveVersion("1.1.5");
        createAndSaveVersion("1.1.3");

        entityManager.flush();
        entityManager.clear();

        // when
        List<ReleaseVersion> result = releaseVersionRepository
                .findVersionsBetween("STANDARD", "1.0.0", "1.2.0");

        // then
        assertThat(result).hasSize(4);
        assertThat(result.get(0).getVersion()).isEqualTo("1.1.0");
        assertThat(result.get(1).getVersion()).isEqualTo("1.1.3");
        assertThat(result.get(2).getVersion()).isEqualTo("1.1.5");
        assertThat(result.get(3).getVersion()).isEqualTo("1.2.0");
    }

    // ========== Helper Methods ==========

    private ReleaseVersion createAndSaveVersion(String version) {
        String[] parts = version.split("\\.");
        ReleaseVersion releaseVersion = ReleaseVersion.builder()
                .version(version)
                .releaseType("STANDARD")
                .majorVersion(Integer.parseInt(parts[0]))
                .minorVersion(Integer.parseInt(parts[1]))
                .patchVersion(Integer.parseInt(parts[2]))
                .majorMinor(parts[0] + "." + parts[1] + ".x")
                .createdBy("system")
                .comment("테스트 버전")
                .isInstall(false)
                .build();
        return releaseVersionRepository.save(releaseVersion);
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
