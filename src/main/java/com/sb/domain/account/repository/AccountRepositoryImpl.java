package com.sb.domain.account.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sb.domain.account.entity.QAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * Account Repository Custom Implementation
 *
 * <p>QueryDSL을 사용한 복잡한 쿼리 구현 - Spring Data JPA가 AccountRepository 인터페이스를 스캔할 때
 * 자동으로 이 구현체를 찾아서 결합함
 * - 네이밍 규칙: {InterfaceName}Impl
 */
@Repository
@RequiredArgsConstructor
public class AccountRepositoryImpl implements AccountRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public long deleteAccountByAccountId(Long accountId) {
        QAccount account = QAccount.account;

        return queryFactory.delete(account)
                .where(account.accountId.eq(accountId)).execute();
    }

    @Override
    public long activateByAccountId(Long accountId) {
        QAccount account = QAccount.account;

        return queryFactory
                .update(account)
                .set(account.status, "ACCOUNT_STATUS_ACTIVE")
                .where(account.accountId.eq(accountId))
                .execute();
    }

    @Override
    public long deactivateByAccountId(Long accountId) {
        QAccount account = QAccount.account;

        return queryFactory
                .update(account)
                .set(account.status, "ACCOUNT_STATUS_INACTIVE")
                .where(account.accountId.eq(accountId))
                .execute();
    }

    @Override
    public long updateAccountNameByAccountId(Long accountId,
            String accountName) {
        QAccount account = QAccount.account;

        return queryFactory
                .update(account)
                .set(account.accountName, accountName)
                .where(account.accountId.eq(accountId))
                .execute();
    }

    @Override
    public long updatePasswordByAccountId(Long accountId, String password) {
        QAccount account = QAccount.account;

        return queryFactory
                .update(account)
                .set(account.password, password)
                .where(account.accountId.eq(accountId))
                .execute();
    }
}
