package com.g2.personalaccount.services;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-11-04 08:13
 */
public interface AccountLockService {

  void lockAccount(Long accountNumber, String threadName);

  void releaseAccount(Long accountNumber, String threadName);
}
