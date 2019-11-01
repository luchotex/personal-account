package com.g2.personalaccount.model.enumerated;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-31 16:13
 */
public enum StatusEnum {
  ACTIVE("active"),
  INACTIVE("inactive");

  private String name;

  StatusEnum(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
