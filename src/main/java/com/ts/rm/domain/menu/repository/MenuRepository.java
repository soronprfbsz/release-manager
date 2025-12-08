package com.ts.rm.domain.menu.repository;

import com.ts.rm.domain.menu.entity.Menu;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Menu Repository
 */
public interface MenuRepository extends JpaRepository<Menu, String> {

    /**
     * 특정 role이 접근 가능한 1depth 메뉴 조회 (순서대로)
     *
     * @param role 역할
     * @return 1depth 메뉴 목록
     */
    @Query("""
        SELECT DISTINCT m
        FROM Menu m
        JOIN MenuRole mr ON m.menuId = mr.menuId
        WHERE mr.role = :role
          AND NOT EXISTS (
              SELECT 1
              FROM MenuHierarchy mh
              WHERE mh.descendant = m.menuId
                AND mh.depth > 0
          )
        ORDER BY m.menuOrder
        """)
    List<Menu> findFirstDepthMenusByRole(@Param("role") String role);

    /**
     * 특정 role이 접근 가능한 특정 부모의 자식 메뉴 조회 (순서대로)
     *
     * @param parentMenuId 부모 메뉴 ID
     * @param role         역할
     * @return 자식 메뉴 목록
     */
    @Query("""
        SELECT DISTINCT m
        FROM Menu m
        JOIN MenuHierarchy mh ON m.menuId = mh.descendant
        JOIN MenuRole mr ON m.menuId = mr.menuId
        WHERE mh.ancestor = :parentMenuId
          AND mh.depth = 1
          AND mr.role = :role
        ORDER BY m.menuOrder
        """)
    List<Menu> findChildMenusByParentAndRole(
            @Param("parentMenuId") String parentMenuId,
            @Param("role") String role
    );
}
