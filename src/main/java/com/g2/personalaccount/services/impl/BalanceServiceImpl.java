package com.g2.personalaccount.services.impl;

import com.g2.personalaccount.config.ServiceConfig;
import com.g2.personalaccount.exceptions.InvalidArgumentsException;
import com.g2.personalaccount.model.Account;
import com.g2.personalaccount.model.Balance;
import com.g2.personalaccount.repositories.BalanceRepository;
import com.g2.personalaccount.services.BalanceService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-31 09:41
 */
@Service
public class BalanceServiceImpl implements BalanceService {

  private BalanceRepository balanceRepository;
  private ServiceConfig serviceConfig;

  public BalanceServiceImpl(BalanceRepository balanceRepository, ServiceConfig serviceConfig) {
    this.balanceRepository = balanceRepository;
    this.serviceConfig = serviceConfig;
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
}
