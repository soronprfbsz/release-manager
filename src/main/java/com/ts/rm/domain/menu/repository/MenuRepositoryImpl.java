package com.ts.rm.domain.menu.repository;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ts.rm.domain.menu.entity.Menu;
import com.ts.rm.domain.menu.entity.QMenu;
import com.ts.rm.domain.menu.entity.QMenuHierarchy;
import com.ts.rm.domain.menu.entity.QMenuRole;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * Menu Repository Custom Implementation (QueryDSL)
 *
 * <p>클로저 테이블 기반 계층 구조 조회 구현
 */
@Repository
@RequiredArgsConstructor
public class MenuRepositoryImpl implements MenuRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 특정 role이 접근 가능한 1depth 메뉴 조회 (순서대로)
     *
     * <p>클로저 테이블에서 depth > 0인 레코드가 없는 메뉴 = 최상위(1depth) 메뉴
     *
     * @param role 역할
     * @return 1depth 메뉴 목록
     */
    @Override
    public List<Menu> findFirstDepthMenusByRole(String role) {
        QMenu menu = QMenu.menu;
        QMenuRole menuRole = QMenuRole.menuRole;
        QMenuHierarchy subMh = new QMenuHierarchy("subMh");

        return queryFactory
                .selectFrom(menu)
                .distinct()
                .join(menuRole).on(menu.menuId.eq(menuRole.menuId))
                .where(
                        menuRole.role.eq(role),
                        // NOT EXISTS: depth > 0인 계층이 없는 메뉴 = 최상위 메뉴
                        JPAExpressions
                                .selectOne()
                                .from(subMh)
                                .where(
                                        subMh.descendant.eq(menu.menuId),
                                        subMh.depth.gt(0)
                                )
                                .notExists()
                )
                .orderBy(menu.menuOrder.asc())
                .fetch();
    }

    /**
     * 특정 role이 접근 가능한 특정 부모의 자식 메뉴 조회 (순서대로)
     *
     * <p>클로저 테이블에서 depth = 1인 관계로 직계 자식만 조회
     *
     * @param parentMenuId 부모 메뉴 ID
     * @param role         역할
     * @return 자식 메뉴 목록
     */
    @Override
    public List<Menu> findChildMenusByParentAndRole(String parentMenuId, String role) {
        QMenu menu = QMenu.menu;
        QMenuHierarchy menuHierarchy = QMenuHierarchy.menuHierarchy;
        QMenuRole menuRole = QMenuRole.menuRole;

        return queryFactory
                .selectFrom(menu)
                .distinct()
                .join(menuHierarchy).on(menu.menuId.eq(menuHierarchy.descendant))
                .join(menuRole).on(menu.menuId.eq(menuRole.menuId))
                .where(
                        menuHierarchy.ancestor.eq(parentMenuId),
                        menuHierarchy.depth.eq(1),  // 직계 자식만 (depth = 1)
                        menuRole.role.eq(role)
                )
                .orderBy(menu.menuOrder.asc())
                .fetch();
    }
}
