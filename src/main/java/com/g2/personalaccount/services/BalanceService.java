package com.g2.personalaccount.services;

import com.g2.personalaccount.dto.responses.CurrentBalanceResponse;
import com.g2.personalaccount.model.Account;
import com.g2.personalaccount.model.Balance;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-31 09:28
 */
public interface BalanceService {
  List<Balance> getAndLockBalances(Long accountNumber, BigDecimal amount, String threadName);

  void addBalances(BigDecimal amount, Account account);

  void substractBalances(List<Balance> balances, BigDecimal amount);

  void releaseBalances(String threadName);

  CurrentBalanceResponse retrieveTotalBalance(Long accountNumber);
}
