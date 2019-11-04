package com.g2.personalaccount.services.impl;

import static com.g2.personalaccount.services.impl.AccountServiceImpl.THE_ACCOUNT_IS_CLOSED;

import com.g2.personalaccount.config.ServiceConfig;
import com.g2.personalaccount.dto.responses.CurrentBalanceResponse;
import com.g2.personalaccount.exceptions.InvalidArgumentsException;
import com.g2.personalaccount.model.Account;
import com.g2.personalaccount.model.Balance;
import com.g2.personalaccount.repositories.AccountRepository;
import com.g2.personalaccount.repositories.BalanceRepository;
import com.g2.personalaccount.services.BalanceService;
import com.g2.personalaccount.validators.EditionValidator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-31 09:41
 */
@Service
public class BalanceServiceImpl implements BalanceService {

  private AccountRepository accountRepository;
  private BalanceRepository balanceRepository;
  private ServiceConfig serviceConfig;
  private EditionValidator editionValidator;

  public BalanceServiceImpl(
      AccountRepository accountRepository,
      BalanceRepository balanceRepository,
      ServiceConfig serviceConfig,
      EditionValidator editionValidator) {
    this.accountRepository = accountRepository;
    this.balanceRepository = balanceRepository;
    this.serviceConfig = serviceConfig;
    this.editionValidator = editionValidator;
  }

  @Override
  @Transactional
  public List<Balance> getAndLockBalances(
      Long accountNumber, BigDecimal amount, String threadName) {
    List<Balance> balances = balanceRepository.retrieveBalances(accountNumber, amount);

    BigDecimal total = BigDecimal.ZERO;

    for (Balance balance : balances) {
      total = total.add(balance.getAmount());
    }

    if (total.compareTo(amount) == -1) {
      throw (new InvalidArgumentsException("There is no sufficient founds on your account"));
    }

    lockingBalances(threadName, balances);

    return balances;
  }

  private void lockingBalances(String threadName, List<Balance> balances) {
    for (Balance balance : balances) {
      balance.setLockingThreadName(threadName);
      balance.setLockingDateTime(
          LocalDateTime.now()
              .plusSeconds(Integer.valueOf(serviceConfig.getLockingRegistriesSeconds())));
    }
    balanceRepository.saveAll(balances);
  }

  @Override
  @Transactional
  public void addBalances(BigDecimal amount, Account account) {
    Balance balance = new Balance();
    balance.setAccount(account);
    balance.setAmount(amount);

    balanceRepository.save(balance);
  }

  @Override
  @Transactional
  public void substractBalances(List<Balance> balances, BigDecimal amount) {
    BigDecimal totalTemp = BigDecimal.ZERO;

    for (Balance balance : balances) {
      totalTemp = totalTemp.add(balance.getAmount());
      if (totalTemp.compareTo(amount) == -1) {
        balanceRepository.delete(balance);
      } else {
        balance.setAmount(totalTemp.subtract(amount));
        balanceRepository.save(balance);
      }
    }
  }

  @Override
  @Transactional
  public void releaseBalances(String threadName) {
    balanceRepository.releaseBalances(threadName);
  }

  @Override
  public CurrentBalanceResponse retrieveTotalBalance(Long accountNumber) {
    Optional<Account> foundAccountOptional = accountRepository.findById(accountNumber);

    editionValidator.validateAccount(foundAccountOptional, accountNumber, THE_ACCOUNT_IS_CLOSED);
    foundAccountOptional.get().getAccountAccess().setAuthenticationExpiration(null);
    accountRepository.save(foundAccountOptional.get());

    BigDecimal total = BigDecimal.ZERO;
    Iterable<Balance> balances = balanceRepository.findAll();

    for (Balance balance : balances) {
      total = total.add(balance.getAmount());
    }

    CurrentBalanceResponse response = new CurrentBalanceResponse();
    response.setCurrentBalance(total);

    return response;
  }
}
