package com.g2.personalaccount.dto.requests;

import java.math.BigDecimal;
import lombok.Data;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-11-04 11:55
 */
@Data
public class MoneyMovementRequest {
  private Long accountNumber;
  private BigDecimal amount;
  private String description;
}
