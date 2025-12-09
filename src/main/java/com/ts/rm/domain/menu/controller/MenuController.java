package com.ts.rm.domain.menu.controller;

import com.ts.rm.domain.menu.dto.MenuDto.MenuResponse;
import com.ts.rm.domain.menu.service.MenuService;
import com.ts.rm.global.response.ApiResponse;
import com.ts.rm.global.security.SecurityUtil;
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
public class MenuController implements MenuControllerDocs {

    private final MenuService menuService;

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getMenus() {
        // JWT 토큰에서 역할 정보 추출
        String role = SecurityUtil.getTokenInfo().role();

        log.info("메뉴 조회 요청 - role: {}", role);

        List<MenuResponse> menus = menuService.getMenusByRole(role);

        return ResponseEntity.ok(ApiResponse.success(menus));
    }
}
