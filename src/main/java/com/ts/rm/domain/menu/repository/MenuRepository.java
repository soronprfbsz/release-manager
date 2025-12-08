package com.ts.rm.domain.menu.repository;

import com.ts.rm.domain.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Menu Repository
 *
 * <p>Spring Data JPA 기본 CRUD + QueryDSL Custom 쿼리
 */
public interface MenuRepository extends JpaRepository<Menu, String>, MenuRepositoryCustom {
    // 기본 CRUD는 JpaRepository에서 제공
    // 복잡한 계층 쿼리는 MenuRepositoryCustom (QueryDSL)에서 구현
}
