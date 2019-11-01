package com.g2.personalaccount.services.impl;

import com.g2.personalaccount.dto.mappers.AccountMapper;
import com.g2.personalaccount.dto.requests.AccountRequest;
import com.g2.personalaccount.dto.requests.AccountUpdateRequest;
import com.g2.personalaccount.dto.responses.AccountResponse;
import com.g2.personalaccount.model.Account;
import com.g2.personalaccount.repositories.AccountRepository;
import com.g2.personalaccount.services.AccountService;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-31 09:41
 */
@Service
public class AccountServiceImpl implements AccountService {

  private AccountMapper accountMapper;
  private AccountRepository accountRepository;

  public AccountServiceImpl(AccountMapper accountMapper, AccountRepository accountRepository) {
    this.accountMapper = accountMapper;
    this.accountRepository = accountRepository;
  }

  @Override
  public AccountResponse create(AccountRequest request) {
    Account account = accountMapper.toEntity(request);

    account = accountRepository.save(account);

    return accountMapper.toResponse(account);
  }

  @Override
  public AccountResponse updatePersonalData(AccountUpdateRequest request) {

    Optional<Account> foundAccountOptional = accountRepository.findById(request.getId());

    if (!foundAccountOptional.isPresent()) {}

    Account foundAccount = foundAccountOptional.get();

    accountMapper.toEntity(request, foundAccount);

    foundAccount = accountRepository.save(foundAccount);

    return accountMapper.toResponse(foundAccount);
  }
}
