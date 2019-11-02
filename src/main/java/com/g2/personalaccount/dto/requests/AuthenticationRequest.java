package com.g2.personalaccount.dto.requests;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-11-01 23:34
 */
@Data
public class AuthenticationRequest {
  @NotNull(message = "Account number must have a value")
  private Long accountNumber;

  @NotNull(message = "PIN must have a value")
  @NotEmpty(message = "PIN cannot be empty")
  // TODO add on readme that is necessary to be used in md5 encrypting
  private String pin;
}
