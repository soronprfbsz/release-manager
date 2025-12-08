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
 * 메뉴별 접근 권한 엔티티
 */
@Entity
@Table(name = "menu_role")
@IdClass(MenuRole.MenuRoleId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuRole {

    @Id
    @Column(name = "menu_id", length = 50)
    private String menuId;

    @Id
    @Column(name = "role", length = 50)
    private String role;

    /**
     * 복합키 클래스
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class MenuRoleId implements Serializable {
        private String menuId;
        private String role;
    }
}
