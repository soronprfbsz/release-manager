package com.ts.rm.domain.customer.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ts.rm.domain.customer.entity.CustomerProject;
import com.ts.rm.domain.customer.entity.QCustomer;
import com.ts.rm.domain.customer.entity.QCustomerProject;
import com.ts.rm.domain.project.entity.QProject;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * CustomerProject Repository Custom Implementation
 *
 * <p>QueryDSL을 사용한 고객사-프로젝트 매핑 쿼리 구현
 */
@Repository
@RequiredArgsConstructor
public class CustomerProjectRepositoryImpl implements CustomerProjectRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QCustomerProject customerProject = QCustomerProject.customerProject;
    private static final QCustomer customer = QCustomer.customer;
    private static final QProject project = QProject.project;

    @Override
    public List<CustomerProject> findAllByCustomerIdWithProject(Long customerId) {
        return queryFactory
                .selectFrom(customerProject)
                .join(customerProject.project, project).fetchJoin()
                .where(customerProject.customer.customerId.eq(customerId))
                .orderBy(project.projectName.asc())
                .fetch();
    }

    @Override
    public List<CustomerProject> findAllByProjectIdWithCustomer(String projectId) {
        return queryFactory
                .selectFrom(customerProject)
                .join(customerProject.customer, customer).fetchJoin()
                .where(customerProject.project.projectId.eq(projectId))
                .orderBy(customer.customerName.asc())
                .fetch();
    }
}
