package com.g2.personalaccount.services;

import com.g2.personalaccount.dto.responses.AccountLastTransactionsResponse;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-31 09:27
 */
public interface TransactionService {
  AccountLastTransactionsResponse retrieveLastTransactions(Long accountNumber);
}
