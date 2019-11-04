package com.g2.personalaccount.dto.requests;

import java.math.BigDecimal;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-11-04 11:55
 */
@Data
public class MoneyMovementRequest {
  @NotNull(message = "The account number must have a value")
  private Long accountNumber;

  @NotNull(message = "The amount must have a value")
  private BigDecimal amount;

  @NotNull(message = "The description must have a value")
  @NotEmpty(message = "The description musn't be empty value")
  private String description;
}
