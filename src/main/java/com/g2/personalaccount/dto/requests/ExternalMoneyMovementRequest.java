package com.g2.personalaccount.dto.requests;

import lombok.Data;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-11-04 12:07
 */
@Data
public class ExternalMoneyMovementRequest extends MoneyMovementRequest {
  private String pin;
}
