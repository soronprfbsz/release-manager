package com.ts.rm.domain.account.mapper;

import com.ts.rm.domain.account.dto.AccountDto;
import com.ts.rm.domain.account.entity.Account;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Account Entity ↔ DTO 변환 Mapper (MapStruct)
 */
@Mapper(componentModel = "spring")
public interface AccountDtoMapper {

    @Mapping(target = "accountId", ignore = true)
    Account toEntity(AccountDto.CreateRequest request);

    AccountDto.DetailResponse toDetailResponse(Account account);

    List<AccountDto.DetailResponse> toDetailResponseList(
            List<Account> accounts);

    AccountDto.SimpleResponse toSimpleResponse(Account account);

    List<AccountDto.SimpleResponse> toSimpleResponseList(
            List<Account> accounts);
}
