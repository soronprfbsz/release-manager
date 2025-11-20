package com.ts.rm.domain.customer.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ts.rm.domain.customer.entity.QCustomer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * Customer Repository Custom Implementation
 *
 * <p>QueryDSL을 사용한 복잡한 쿼리 구현
 */
@Repository
@RequiredArgsConstructor
public class CustomerRepositoryImpl implements CustomerRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public long activateByCustomerId(Long customerId) {
        QCustomer customer = QCustomer.customer;

        return queryFactory
                .update(customer)
                .set(customer.isActive, true)
                .where(customer.customerId.eq(customerId))
                .execute();
    }

    @Override
    public long deactivateByCustomerId(Long customerId) {
        QCustomer customer = QCustomer.customer;

        return queryFactory
                .update(customer)
                .set(customer.isActive, false)
                .where(customer.customerId.eq(customerId))
                .execute();
    }

    @Override
    public long updateCustomerInfoByCustomerId(Long customerId, String customerName,
            String description) {
        QCustomer customer = QCustomer.customer;

        return queryFactory
                .update(customer)
                .set(customer.customerName, customerName)
                .set(customer.description, description)
                .where(customer.customerId.eq(customerId))
                .execute();
    }
}
