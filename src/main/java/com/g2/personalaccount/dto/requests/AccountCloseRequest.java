package com.g2.personalaccount.dto.requests;

import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-11-02 02:58
 */
@Data
public class AccountCloseRequest {
  @NotNull(message = "The account number must have a value")
  private Long accountNumber;
}
