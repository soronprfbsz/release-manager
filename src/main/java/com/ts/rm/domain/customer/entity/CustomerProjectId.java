package com.ts.rm.domain.customer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * CustomerProject 복합 기본키
 *
 * <p>고객사-프로젝트 매핑 테이블의 복합 PK
 */
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProjectId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "project_id")
    private String projectId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CustomerProjectId that = (CustomerProjectId) o;
        return Objects.equals(customerId, that.customerId)
                && Objects.equals(projectId, that.projectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId, projectId);
    }
}
