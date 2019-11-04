package com.g2.personalaccount.dto.responses;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-11-03 22:49
 */
@Data
public class AccountLastTransactionsResponse {
  private Long transactionId;
  private List<TransactionResponse> transactionResponses = new ArrayList<>();
}
