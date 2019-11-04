package com.g2.personalaccount.dto.requests;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-11-04 12:07
 */
@Data
public class ExternalMoneyMovementRequest extends MoneyMovementRequest {
  @NotNull(message = "The pin must have a value")
  @NotEmpty(message = "The pin musn't be empty value")
  private String pin;
}
