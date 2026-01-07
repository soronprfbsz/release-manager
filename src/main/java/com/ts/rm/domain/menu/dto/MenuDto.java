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
     * @param menuId               메뉴 ID
     * @param menuName             메뉴명
     * @param menuUrl              메뉴 URL
     * @param icon                 메뉴 아이콘 (Lucide React 아이콘명)
     * @param isIconVisible        아이콘 표시 여부
     * @param description          메뉴 설명
     * @param isDescriptionVisible 설명 표시 여부
     * @param isLineBreak          줄바꿈 여부
     * @param children             하위 메뉴 목록
     */
    @Schema(description = "메뉴 (계층 구조)")
    public record MenuResponse(
            @Schema(description = "메뉴 ID", example = "version_management")
            String menuId,

            @Schema(description = "메뉴명", example = "버전 관리")
            String menuName,

            @Schema(description = "메뉴 URL", example = "releases/standard")
            String menuUrl,

            @Schema(description = "메뉴 아이콘 (Lucide React 아이콘명)", example = "tag")
            String icon,

            @Schema(description = "아이콘 표시 여부", example = "true")
            Boolean isIconVisible,

            @Schema(description = "메뉴 설명", example = "릴리즈 버전 관리")
            String description,

            @Schema(description = "설명 표시 여부", example = "true")
            Boolean isDescriptionVisible,

            @Schema(description = "줄바꿈 여부 (가로 배치 시 강제 줄바꿈)", example = "false")
            Boolean isLineBreak,

            @Schema(description = "하위 메뉴 목록")
            List<MenuResponse> children
    ) {
        /**
         * 하위 메뉴가 없는 메뉴 생성
         */
        public static MenuResponse of(String menuId, String menuName, String menuUrl, String icon, Boolean isIconVisible, String description, Boolean isDescriptionVisible, Boolean isLineBreak) {
            return new MenuResponse(menuId, menuName, menuUrl, icon, isIconVisible, description, isDescriptionVisible, isLineBreak, List.of());
        }

        /**
         * 하위 메뉴가 있는 메뉴 생성
         */
        public static MenuResponse of(String menuId, String menuName, String menuUrl, String icon, Boolean isIconVisible, String description, Boolean isDescriptionVisible, Boolean isLineBreak, List<MenuResponse> children) {
            return new MenuResponse(menuId, menuName, menuUrl, icon, isIconVisible, description, isDescriptionVisible, isLineBreak, children);
        }
    }
}
