package com.ts.rm.domain.menu.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 메뉴 계층 구조 엔티티 (Closure Table 패턴)
 */
@Entity
@Table(name = "menu_hierarchy")
@IdClass(MenuHierarchy.MenuHierarchyId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuHierarchy {

    @Id
    @Column(name = "ancestor", length = 50)
    private String ancestor;

    @Id
    @Column(name = "descendant", length = 50)
    private String descendant;

    @Column(name = "depth", nullable = false)
    private Integer depth;

    /**
     * 복합키 클래스
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class MenuHierarchyId implements Serializable {
        private String ancestor;
        private String descendant;
    }
}
