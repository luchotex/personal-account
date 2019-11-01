package com.g2.personalaccount.services.impl;

import com.g2.personalaccount.dto.mappers.AccountMapper;
import com.g2.personalaccount.dto.requests.AccountRequest;
import com.g2.personalaccount.dto.requests.AccountUpdateRequest;
import com.g2.personalaccount.dto.responses.AccountResponse;
import com.g2.personalaccount.model.Account;
import com.g2.personalaccount.proxy.EmailProxy;
import com.g2.personalaccount.repositories.AccountRepository;
import com.g2.personalaccount.services.AccountService;
import com.g2.personalaccount.utils.PinGenerator;
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
  private EmailProxy emailProxy;
  private PinGenerator pinGenerator;

  public AccountServiceImpl(
      AccountMapper accountMapper,
      AccountRepository accountRepository,
      EmailProxy emailProxy,
      PinGenerator pinGenerator) {
    this.accountMapper = accountMapper;
    this.accountRepository = accountRepository;
    this.emailProxy = emailProxy;
    this.pinGenerator = pinGenerator;
  }

  @Override
  public AccountResponse create(AccountRequest request) {

    Integer generatedPin = pinGenerator.generateRandom(4);

    Account account = accountMapper.toEntity(request, generatedPin);

    account = accountRepository.save(account);

    emailProxy.sendPin(account.getAccountHolder().getEmail(), generatedPin);

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
