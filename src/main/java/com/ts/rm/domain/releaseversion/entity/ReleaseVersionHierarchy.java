package com.ts.rm.domain.releaseversion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ReleaseVersionHierarchy Entity (Closure Table)
 *
 * <p>릴리즈 버전 간 계층 관계를 클로저 테이블 패턴으로 관리
 * <ul>
 *   <li>빠른 계층 조회</li>
 *   <li>버전 간 순서 관계 명시적 관리</li>
 *   <li>누적 패치 생성 시 버전 범위 쿼리 최적화</li>
 * </ul>
 */
@Entity
@Table(name = "release_version_hierarchy")
@IdClass(ReleaseVersionHierarchy.HierarchyId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReleaseVersionHierarchy {

    /**
     * 상위 버전 (조상)
     */
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ancestor_id", nullable = false)
    private ReleaseVersion ancestor;

    /**
     * 하위 버전 (후손)
     */
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "descendant_id", nullable = false)
    private ReleaseVersion descendant;

    /**
     * 계층 깊이
     * <ul>
     *   <li>0: 자기 자신</li>
     *   <li>1: 직접 자식 (부모 → 자식)</li>
     *   <li>2+: 손자 이하 (조상 → 후손)</li>
     * </ul>
     */
    @Column(name = "depth", nullable = false)
    private Integer depth;

    /**
     * Composite Primary Key for ReleaseVersionHierarchy
     */
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HierarchyId implements Serializable {
        private Long ancestor;
        private Long descendant;
    }
}
