package com.ts.rm.domain.releasefile.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.ts.rm.domain.releasefile.enums.FileCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * SubCategoryValidator 테스트
 */
class SubCategoryValidatorTest {

    @Test
    @DisplayName("isValid - 유효성 체크")
    void isValid() {
        // given
        FileCategory database = FileCategory.DATABASE;
        FileCategory engine = FileCategory.ENGINE;

        // when & then
        assertThat(SubCategoryValidator.isValid(database, "MARIADB")).isTrue();
        assertThat(SubCategoryValidator.isValid(database, "CRATEDB")).isTrue();
        assertThat(SubCategoryValidator.isValid(database, "BUILD")).isFalse();

        assertThat(SubCategoryValidator.isValid(engine, "NC_PERF")).isTrue();
        assertThat(SubCategoryValidator.isValid(engine, "MARIADB")).isFalse();
    }

    @Test
    @DisplayName("getAllowedSubCategories - 허용된 서브 카테고리 조회")
    void getAllowedSubCategories() {
        // when
        var databaseSubs = SubCategoryValidator.getAllowedSubCategories(FileCategory.DATABASE);
        var webSubs = SubCategoryValidator.getAllowedSubCategories(FileCategory.WEB);
        var engineSubs = SubCategoryValidator.getAllowedSubCategories(FileCategory.ENGINE);
        var etcSubs = SubCategoryValidator.getAllowedSubCategories(FileCategory.ETC);

        // then
        assertThat(databaseSubs).containsExactlyInAnyOrder("MARIADB", "CRATEDB", "ETC");
        // WEB은 서브 카테고리 없음
        assertThat(webSubs).isEmpty();
        // ENGINE은 65개 항목(64개 엔진 + ETC)이므로 일부만 확인
        assertThat(engineSubs).contains("NC_AI_EVENT", "NC_AP", "NC_CONF", "NC_PERF", "NC_WATCHDOG", "ETC");
        assertThat(engineSubs).hasSize(65);
        // ETC는 서브 카테고리 없음
        assertThat(etcSubs).isEmpty();
    }
}
