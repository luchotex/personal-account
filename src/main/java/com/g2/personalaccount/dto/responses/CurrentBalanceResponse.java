package com.g2.personalaccount.dto.responses;

import java.math.BigDecimal;
import lombok.Data;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-11-04 13:20
 */
@Data
public class CurrentBalanceResponse {
  private Long transactionId;
  private BigDecimal currentBalance;
}
