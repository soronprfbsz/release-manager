package com.ts.rm.domain.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * 메뉴 DTO
 */
public final class MenuDto {

    private MenuDto() {
    }

    /**
     * 메뉴 응답 (계층 구조)
     *
     * @param menuId   메뉴 ID
     * @param menuName 메뉴명
     * @param children 하위 메뉴 목록
     */
    @Schema(description = "메뉴 (계층 구조)")
    public record MenuResponse(
            @Schema(description = "메뉴 ID", example = "version_management")
            String menuId,

            @Schema(description = "메뉴명", example = "버전 관리")
            String menuName,

            @Schema(description = "하위 메뉴 목록")
            List<MenuResponse> children
    ) {
        /**
         * 하위 메뉴가 없는 메뉴 생성
         */
        public static MenuResponse of(String menuId, String menuName) {
            return new MenuResponse(menuId, menuName, List.of());
        }

        /**
         * 하위 메뉴가 있는 메뉴 생성
         */
        public static MenuResponse of(String menuId, String menuName, List<MenuResponse> children) {
            return new MenuResponse(menuId, menuName, children);
        }
    }
}
