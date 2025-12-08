package com.ts.rm.domain.menu.repository;

import com.ts.rm.domain.menu.entity.Menu;
import java.util.List;

/**
 * Menu Repository Custom Interface
 *
 * <p>QueryDSL을 사용한 복잡한 메뉴 계층 조회
 */
public interface MenuRepositoryCustom {

    /**
     * 특정 role이 접근 가능한 1depth 메뉴 조회 (순서대로)
     *
     * <p>클로저 테이블에서 depth > 0인 레코드가 없는 메뉴 = 최상위(1depth) 메뉴
     *
     * @param role 역할
     * @return 1depth 메뉴 목록
     */
    List<Menu> findFirstDepthMenusByRole(String role);

    /**
     * 특정 role이 접근 가능한 특정 부모의 자식 메뉴 조회 (순서대로)
     *
     * <p>클로저 테이블에서 depth = 1인 관계로 직계 자식만 조회
     *
     * @param parentMenuId 부모 메뉴 ID
     * @param role         역할
     * @return 자식 메뉴 목록
     */
    List<Menu> findChildMenusByParentAndRole(String parentMenuId, String role);
}
