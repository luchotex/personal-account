package com.g2.personalaccount.model.enumerated;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-30 19:58
 */
public enum TypeEnum {
  DEPOSIT("deposit"),
  WITH_DRAWL("withdrawal"),
  DEBIT("debit"),
  CHECKS("checks");
  private String name;

  TypeEnum(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
