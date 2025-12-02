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
        FileCategory web = FileCategory.WEB;

        // when & then - 대문자
        assertThat(SubCategoryValidator.isValid(database, "MARIADB")).isTrue();
        assertThat(SubCategoryValidator.isValid(database, "CRATEDB")).isTrue();
        assertThat(SubCategoryValidator.isValid(database, "BUILD")).isFalse();

        assertThat(SubCategoryValidator.isValid(web, "BUILD")).isTrue();
        assertThat(SubCategoryValidator.isValid(web, "MARIADB")).isFalse();
    }

    @Test
    @DisplayName("getAllowedSubCategories - 허용된 서브 카테고리 조회")
    void getAllowedSubCategories() {
        // when
        var databaseSubs = SubCategoryValidator.getAllowedSubCategories(FileCategory.DATABASE);
        var webSubs = SubCategoryValidator.getAllowedSubCategories(FileCategory.WEB);
        var installSubs = SubCategoryValidator.getAllowedSubCategories(FileCategory.INSTALL);
        var engineSubs = SubCategoryValidator.getAllowedSubCategories(FileCategory.ENGINE);

        // then - 대문자
        assertThat(databaseSubs).containsExactlyInAnyOrder("MARIADB", "CRATEDB", "ETC");
        assertThat(webSubs).containsExactlyInAnyOrder("BUILD", "IMAGE", "METADATA", "ETC");
        assertThat(installSubs).containsExactlyInAnyOrder("SH", "IMAGE", "METADATA", "ETC");
        // ENGINE은 65개 항목(64개 엔진 + ETC)이므로 일부만 확인
        assertThat(engineSubs).contains("NC_AI_EVENT", "NC_AP", "NC_CONF", "NC_PERF", "NC_WATCHDOG", "ETC");
        assertThat(engineSubs).hasSize(65);
    }
}
