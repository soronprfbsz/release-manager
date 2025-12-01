package com.ts.rm.domain.releasefile.util;

import com.ts.rm.domain.releasefile.enums.FileCategory;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import java.util.Map;
import java.util.Set;

/**
 * SubCategory 유효성 검증 유틸리티
 *
 * <p>각 FileCategory에 허용된 subCategory 값을 검증합니다.
 */
public class SubCategoryValidator {

    /**
     * 카테고리별 허용된 서브 카테고리 매핑
     *
     * <p>Database: FILE_SUBCATEGORY_DATABASE (mariadb, cratedb, metadata, etc)
     * <p>Web: FILE_SUBCATEGORY_WEB (build, webobjects, metadata, etc)
     * <p>Install: FILE_SUBCATEGORY_INSTALL (sh, image, metadata, etc)
     * <p>Engine: FILE_SUBCATEGORY_ENGINE (build, sh, image, metadata, etc)
     */
    private static final Map<FileCategory, Set<String>> ALLOWED_SUBCATEGORIES = Map.of(
            FileCategory.DATABASE, Set.of("mariadb", "cratedb", "metadata", "etc"),
            FileCategory.WEB, Set.of("build", "webobjects", "metadata", "etc"),
            FileCategory.INSTALL, Set.of("sh", "image", "metadata", "etc"),
            FileCategory.ENGINE, Set.of("build", "sh", "image", "metadata", "etc")
    );

    /**
     * SubCategory 유효성 검증
     *
     * @param fileCategory 파일 카테고리
     * @param subCategory  서브 카테고리
     * @throws BusinessException 유효하지 않은 subCategory인 경우
     */
    public static void validate(FileCategory fileCategory, String subCategory) {
        if (fileCategory == null || subCategory == null) {
            return; // null은 허용 (선택적 필드)
        }

        Set<String> allowedValues = ALLOWED_SUBCATEGORIES.get(fileCategory);
        if (allowedValues == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "알 수 없는 파일 카테고리입니다: " + fileCategory);
        }

        String normalizedSubCategory = subCategory.toLowerCase();
        if (!allowedValues.contains(normalizedSubCategory)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    String.format("카테고리 '%s'에 허용되지 않은 서브 카테고리입니다: %s (허용값: %s)",
                            fileCategory.getCode(), subCategory, allowedValues));
        }
    }

    /**
     * 특정 카테고리에서 subCategory가 유효한지 확인
     *
     * @param fileCategory 파일 카테고리
     * @param subCategory  서브 카테고리
     * @return 유효하면 true, 아니면 false
     */
    public static boolean isValid(FileCategory fileCategory, String subCategory) {
        if (fileCategory == null || subCategory == null) {
            return true; // null은 허용
        }

        Set<String> allowedValues = ALLOWED_SUBCATEGORIES.get(fileCategory);
        if (allowedValues == null) {
            return false;
        }

        return allowedValues.contains(subCategory.toLowerCase());
    }

    /**
     * 카테고리에 허용된 모든 서브 카테고리 조회
     *
     * @param fileCategory 파일 카테고리
     * @return 허용된 서브 카테고리 Set
     */
    public static Set<String> getAllowedSubCategories(FileCategory fileCategory) {
        return ALLOWED_SUBCATEGORIES.getOrDefault(fileCategory, Set.of());
    }
}
