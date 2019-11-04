package com.g2.personalaccount.services;

import static com.g2.personalaccount.services.impl.AccountServiceImpl.THE_ACCOUNT_IS_CLOSED;

import com.g2.personalaccount.config.ServiceConfig;
import com.g2.personalaccount.exceptions.InvalidArgumentsException;
import com.g2.personalaccount.model.Account;
import com.g2.personalaccount.model.AccountLock;
import com.g2.personalaccount.repositories.AccountLockRepository;
import com.g2.personalaccount.repositories.AccountRepository;
import com.g2.personalaccount.validators.EditionValidator;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-11-04 08:13
 */
public class AccountLockServiceImpl implements AccountLockService {

  private AccountRepository accountRepository;
  private AccountLockRepository accountLockRepository;
  private EditionValidator editionValidator;

  private ServiceConfig serviceConfig;

  public AccountLockServiceImpl(
      AccountRepository accountRepository,
      AccountLockRepository accountLockRepository,
      EditionValidator editionValidator,
      ServiceConfig serviceConfig) {
    this.accountRepository = accountRepository;
    this.accountLockRepository = accountLockRepository;
    this.editionValidator = editionValidator;
    this.serviceConfig = serviceConfig;
  }

  @Override
  public void lockAccount(Long accountNumber, String threadName) {

    Optional<Account> optionalAccount = accountRepository.findById(accountNumber);

    editionValidator.validateAccount(optionalAccount, accountNumber, THE_ACCOUNT_IS_CLOSED);

    Optional<AccountLock> optionalAccountLock =
        accountLockRepository.findByAccount_Id(accountNumber);

    if (!optionalAccountLock.isPresent()) {
      AccountLock accountLockToSave = new AccountLock();
      accountLockToSave.setThreadName(threadName);
      accountLockToSave.setExpirationDate(getExpirationLocalDateTime());
      accountLockToSave.setAccount(optionalAccount.get());
      accountLockRepository.save(accountLockToSave);
    } else {
      LocalDateTime now = LocalDateTime.now();
      AccountLock accountLock = optionalAccountLock.get();

      if (now.isAfter(accountLock.getExpirationDate())) {
        accountLock.setThreadName(threadName);
        accountLock.setExpirationDate(getExpirationLocalDateTime());
        accountLockRepository.save(accountLock);
      } else {
        throw new InvalidArgumentsException(
            String.format(
                "The account number %s is locking to perform money movements", accountNumber));
      }
    }
  }

  private LocalDateTime getExpirationLocalDateTime() {
    return LocalDateTime.now()
        .plusSeconds(Integer.valueOf(serviceConfig.getLockingRegistriesSeconds()));
  }
}
