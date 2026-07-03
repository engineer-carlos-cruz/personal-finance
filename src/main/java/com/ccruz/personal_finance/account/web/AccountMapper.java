package com.ccruz.personal_finance.account.web;

import com.ccruz.personal_finance.account.persistence.Account;
import com.ccruz.personal_finance.account.web.dto.AccountResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    AccountResponse toResponse(Account account);

    List<AccountResponse> toResponse(List<Account> accounts);
}
