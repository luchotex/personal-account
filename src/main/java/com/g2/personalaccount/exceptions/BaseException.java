package com.g2.personalaccount.exceptions;

import lombok.Data;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-11-03 18:05
 */
@Data
public abstract class BaseException extends RuntimeException {

  private Long transactionId;

  public BaseException(String message) {
    super(message);
  }
}
