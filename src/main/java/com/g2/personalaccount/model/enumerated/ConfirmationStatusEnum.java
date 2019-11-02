package com.g2.personalaccount.model.enumerated;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-11-01 14:34
 */
public enum ConfirmationStatusEnum {
  ACTIVE("active"),
  CONFIRMED("confirmed");

  private String name;

  ConfirmationStatusEnum(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
