package com.ts.rm.domain.menu.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 메뉴 엔티티
 */
@Entity
@Table(name = "menu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Menu {

    @Id
    @Column(name = "menu_id", length = 50)
    private String menuId;

    @Column(name = "menu_name", nullable = false, length = 100)
    private String menuName;

    @Column(name = "menu_url", length = 200)
    private String menuUrl;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "is_icon_visible", nullable = false)
    private Boolean isIconVisible;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_description_visible", nullable = false)
    private Boolean isDescriptionVisible;

    @Column(name = "is_line_break", nullable = false)
    private Boolean isLineBreak;

    @Column(name = "menu_order", nullable = false)
    private Integer menuOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
