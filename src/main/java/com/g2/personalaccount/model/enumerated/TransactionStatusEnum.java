package com.g2.personalaccount.model.enumerated;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-11-03 13:39
 */
public enum TransactionStatusEnum {
  CORRECT("correct"),
  ERROR("error");

  private String name;

  TransactionStatusEnum(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
