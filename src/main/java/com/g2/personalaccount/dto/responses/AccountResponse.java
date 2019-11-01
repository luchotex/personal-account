package com.g2.personalaccount.dto.responses;

import lombok.Data;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-31 16:21
 */
@Data
public class AccountResponse {

  private Long transactionId;
  private Long accountNumber;
  private String holderFirstName;
  private String holderLastName;
  private String email;
  private Integer ssn;
  private Integer voterCardId;
}
