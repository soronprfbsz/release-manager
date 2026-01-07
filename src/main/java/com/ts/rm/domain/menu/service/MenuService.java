package com.ts.rm.domain.menu.service;

import com.ts.rm.domain.menu.dto.MenuDto.MenuResponse;
import com.ts.rm.domain.menu.entity.Menu;
import com.ts.rm.domain.menu.repository.MenuRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 메뉴 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;

    /**
     * 역할별 메뉴 계층 구조 조회
     *
     * @param role 역할 (ADMIN, USER, GUEST)
     * @return 메뉴 계층 구조 목록
     */
    public List<MenuResponse> getMenusByRole(String role) {
        log.info("역할별 메뉴 조회 시작 - role: {}", role);

        // 1depth 메뉴 조회
        List<Menu> firstDepthMenus = menuRepository.findFirstDepthMenusByRole(role);

        // 계층 구조 생성
        List<MenuResponse> menuTree = new ArrayList<>();
        for (Menu menu : firstDepthMenus) {
            MenuResponse menuResponse = buildMenuTree(menu, role);
            menuTree.add(menuResponse);
        }

        log.info("역할별 메뉴 조회 완료 - role: {}, 1depth 메뉴 수: {}", role, menuTree.size());
        return menuTree;
    }

    /**
     * 메뉴 계층 구조 재귀적 생성
     *
     * @param menu 현재 메뉴
     * @param role 역할
     * @return 메뉴 응답 (하위 메뉴 포함)
     */
    private MenuResponse buildMenuTree(Menu menu, String role) {
        // 하위 메뉴 조회
        List<Menu> childMenus = menuRepository.findChildMenusByParentAndRole(menu.getMenuId(), role);

        // 하위 메뉴가 없으면 leaf 노드
        if (childMenus.isEmpty()) {
            return MenuResponse.of(
                    menu.getMenuId(),
                    menu.getMenuName(),
                    menu.getMenuUrl(),
                    menu.getIcon(),
                    menu.getIsIconVisible(),
                    menu.getDescription(),
                    menu.getIsDescriptionVisible(),
                    menu.getIsLineBreak()
            );
        }

        // 하위 메뉴가 있으면 재귀적으로 구조 생성
        List<MenuResponse> children = new ArrayList<>();
        for (Menu childMenu : childMenus) {
            MenuResponse childResponse = buildMenuTree(childMenu, role);
            children.add(childResponse);
        }

        return MenuResponse.of(
                menu.getMenuId(),
                menu.getMenuName(),
                menu.getMenuUrl(),
                menu.getIcon(),
                menu.getIsIconVisible(),
                menu.getDescription(),
                menu.getIsDescriptionVisible(),
                menu.getIsLineBreak(),
                children
        );
    }
}
