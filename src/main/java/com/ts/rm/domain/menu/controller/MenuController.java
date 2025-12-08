package com.ts.rm.domain.menu.controller;

import com.ts.rm.domain.menu.dto.MenuDto.MenuResponse;
import com.ts.rm.domain.menu.service.MenuService;
import com.ts.rm.global.response.ApiResponse;
import com.ts.rm.global.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 메뉴 관리 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
@Tag(name = "기본", description = "공통 코드, 메뉴 등 솔루션 기본 API")
public class MenuController {

    private final MenuService menuService;

    /**
     * 현재 사용자의 권한에 따른 메뉴 목록 조회 (계층 구조)
     *
     * @return 메뉴 계층 구조 목록
     */
    @GetMapping
    @Operation(summary = "메뉴 목록 조회", description = "현재 로그인한 사용자의 권한에 따라 접근 가능한 메뉴를 계층 구조로 조회합니다.")
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getMenus() {
        // JWT 토큰에서 역할 정보 추출
        String role = SecurityUtil.getTokenInfo().role();

        log.info("메뉴 조회 요청 - role: {}", role);

        List<MenuResponse> menus = menuService.getMenusByRole(role);

        return ResponseEntity.ok(ApiResponse.success(menus));
    }
}
