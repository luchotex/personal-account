package com.g2.personalaccount.model.enumerated;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-30 19:58
 */
public enum TypeEnum {
  DEPOSIT("deposit"),
  WITHDRAWAL("withdrawal"),
  DEBIT("debit"),
  CHECKS("checks"),
  CREATION("creation"),
  UPDATE_PERSONAL_DATA("updatePersonalData"),
  CONFIRMATION_CREATION("confirmCreation"),
  AUTHENTICATE("authenticate"),
  CLOSE("close"),
  LAST_TRANSACTIONS("lastTransactions");
  private String name;

  TypeEnum(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
