package com.g2.personalaccount.dto.requests;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-31 16:21
 */
@Data
public class AccountRequest {

  @NotNull(message = "The First name must have a value")
  @NotEmpty(message = "The First name musn't be empty value")
  @Pattern(
      regexp = "[A-Za-z ]*",
      message = "The first name must contain only characters or letters")
  private String holderFirstName;

  @NotNull(message = "The Last name must have a value")
  @NotEmpty(message = "The last name musn't be empty value")
  @Pattern(regexp = "[A-Za-z ]*", message = "The last name must contain only characters or letters")
  private String holderLastName;

  @NotNull(message = "The email must have a value")
  @NotEmpty(message = "The email musn't be empty value")
  @Email(message = "Email must have valid format")
  private String email;

  @NotNull(message = "The ssn must have a value")
  @Positive(message = "The ssn cannot be a negative value")
  @Range(min = 111111111, max = 999999999, message = "SSN must be 9 digit number")
  private Long ssn;

  @NotNull(message = "The Voter card Id must have a value")
  @Positive(message = "The voter card Id cannot be a negative value")
  // TODO validate this format
  private Long voterCardId;
}
