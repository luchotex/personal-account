package com.g2.personalaccount.exceptions;

import lombok.Data;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-31 10:30
 */
@Data
public class ErrorResponse {

  private Long transactionId;
  private int status;
  private String error;
  private String message;
}
