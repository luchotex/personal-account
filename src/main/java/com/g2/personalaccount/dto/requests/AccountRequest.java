package com.g2.personalaccount.dto.requests;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-31 16:21
 */
@Data
public class AccountRequest {

  @NotNull(message = "The First name must have a value")
  @NotEmpty(message = "The First name musn't be empty value")
  private String holderFirstName;

  @NotNull(message = "The Last name must have a value")
  @NotEmpty(message = "The last name musn't be empty value")
  private String holderLastName;

  @NotNull(message = "The email must have a value")
  @NotEmpty(message = "The email musn't be empty value")
  private String email;

  @NotNull(message = "The ssn must have a value")
  private Long ssn;

  @NotNull(message = "The Voter card Id must have a value")
  private Long voterCardId;
}
