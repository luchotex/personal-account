package com.g2.personalaccount.utils;

import com.g2.personalaccount.model.Account;
import com.g2.personalaccount.model.AccountLock;
import java.time.LocalDateTime;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-11-04 09:32
 */
public class AccountLockTestUtils {
  private AccountLockTestUtils() {}

  public static AccountLock createAccountLock(Account foundAccount, LocalDateTime localDateTime) {
    AccountLock accountLockFound = new AccountLock();
    accountLockFound.setId(234234L);
    accountLockFound.setThreadName("threadName-2");
    accountLockFound.setExpirationDate(localDateTime);
    accountLockFound.setAccount(foundAccount);
    return accountLockFound;
  }
}
