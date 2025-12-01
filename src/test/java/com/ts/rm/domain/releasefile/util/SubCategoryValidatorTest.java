package com.ts.rm.domain.releasefile.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ts.rm.domain.releasefile.enums.FileCategory;
import com.ts.rm.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * SubCategoryValidator 테스트
 */
class SubCategoryValidatorTest {

    @Test
    @DisplayName("DATABASE 카테고리 - 유효한 서브 카테고리")
    void validate_Database_ValidSubCategory() {
        // given
        FileCategory category = FileCategory.DATABASE;

        // when & then - 예외 없이 통과
        SubCategoryValidator.validate(category, "mariadb");
        SubCategoryValidator.validate(category, "cratedb");
        SubCategoryValidator.validate(category, "metadata");
        SubCategoryValidator.validate(category, "etc");
    }

    @Test
    @DisplayName("DATABASE 카테고리 - 유효하지 않은 서브 카테고리")
    void validate_Database_InvalidSubCategory() {
        // given
        FileCategory category = FileCategory.DATABASE;

        // when & then
        assertThatThrownBy(() -> SubCategoryValidator.validate(category, "build"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("허용되지 않은 서브 카테고리");
    }

    @Test
    @DisplayName("WEB 카테고리 - 유효한 서브 카테고리")
    void validate_Web_ValidSubCategory() {
        // given
        FileCategory category = FileCategory.WEB;

        // when & then
        SubCategoryValidator.validate(category, "build");
        SubCategoryValidator.validate(category, "webobjects");
        SubCategoryValidator.validate(category, "metadata");
        SubCategoryValidator.validate(category, "etc");
    }

    @Test
    @DisplayName("WEB 카테고리 - 유효하지 않은 서브 카테고리")
    void validate_Web_InvalidSubCategory() {
        // given
        FileCategory category = FileCategory.WEB;

        // when & then
        assertThatThrownBy(() -> SubCategoryValidator.validate(category, "mariadb"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("허용되지 않은 서브 카테고리");
    }

    @Test
    @DisplayName("INSTALL 카테고리 - 유효한 서브 카테고리")
    void validate_Install_ValidSubCategory() {
        // given
        FileCategory category = FileCategory.INSTALL;

        // when & then
        SubCategoryValidator.validate(category, "sh");
        SubCategoryValidator.validate(category, "image");
        SubCategoryValidator.validate(category, "metadata");
        SubCategoryValidator.validate(category, "etc");
    }

    @Test
    @DisplayName("ENGINE 카테고리 - 유효한 서브 카테고리")
    void validate_Engine_ValidSubCategory() {
        // given
        FileCategory category = FileCategory.ENGINE;

        // when & then
        SubCategoryValidator.validate(category, "build");
        SubCategoryValidator.validate(category, "sh");
        SubCategoryValidator.validate(category, "image");
        SubCategoryValidator.validate(category, "metadata");
        SubCategoryValidator.validate(category, "etc");
    }

    @Test
    @DisplayName("null 값은 허용됨")
    void validate_NullValues() {
        // when & then - 예외 없이 통과
        SubCategoryValidator.validate(null, "mariadb");
        SubCategoryValidator.validate(FileCategory.DATABASE, null);
        SubCategoryValidator.validate(null, null);
    }

    @Test
    @DisplayName("isValid - 유효성 체크")
    void isValid() {
        // given
        FileCategory database = FileCategory.DATABASE;
        FileCategory web = FileCategory.WEB;

        // when & then
        assertThat(SubCategoryValidator.isValid(database, "mariadb")).isTrue();
        assertThat(SubCategoryValidator.isValid(database, "cratedb")).isTrue();
        assertThat(SubCategoryValidator.isValid(database, "build")).isFalse();

        assertThat(SubCategoryValidator.isValid(web, "build")).isTrue();
        assertThat(SubCategoryValidator.isValid(web, "mariadb")).isFalse();
    }

    @Test
    @DisplayName("대소문자 무관 검증")
    void validate_CaseInsensitive() {
        // given
        FileCategory category = FileCategory.DATABASE;

        // when & then - 대소문자 관계없이 허용
        SubCategoryValidator.validate(category, "MARIADB");
        SubCategoryValidator.validate(category, "MariaDB");
        SubCategoryValidator.validate(category, "mariadb");
    }

    @Test
    @DisplayName("getAllowedSubCategories - 허용된 서브 카테고리 조회")
    void getAllowedSubCategories() {
        // when
        var databaseSubs = SubCategoryValidator.getAllowedSubCategories(FileCategory.DATABASE);
        var webSubs = SubCategoryValidator.getAllowedSubCategories(FileCategory.WEB);
        var installSubs = SubCategoryValidator.getAllowedSubCategories(FileCategory.INSTALL);
        var engineSubs = SubCategoryValidator.getAllowedSubCategories(FileCategory.ENGINE);

        // then
        assertThat(databaseSubs).containsExactlyInAnyOrder("mariadb", "cratedb", "metadata", "etc");
        assertThat(webSubs).containsExactlyInAnyOrder("build", "webobjects", "metadata", "etc");
        assertThat(installSubs).containsExactlyInAnyOrder("sh", "image", "metadata", "etc");
        assertThat(engineSubs).containsExactlyInAnyOrder("build", "sh", "image", "metadata", "etc");
    }
}
