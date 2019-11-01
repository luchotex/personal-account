package com.g2.personalaccount.dto.responses;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-31 16:21
 */
public class AccountResponse {

  private Long transactionId;
  private Long accountNumber;
  private String holderFirstName;
  private String holderLastName;
  private Integer ssn;
  private Integer voterCardId;

  public Long getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(Long transactionId) {
    this.transactionId = transactionId;
  }

  public Long getAccountNumber() {
    return accountNumber;
  }

  public void setAccountNumber(Long accountNumber) {
    this.accountNumber = accountNumber;
  }

  public String getHolderFirstName() {
    return holderFirstName;
  }

  public void setHolderFirstName(String holderFirstName) {
    this.holderFirstName = holderFirstName;
  }

  public String getHolderLastName() {
    return holderLastName;
  }

  public void setHolderLastName(String holderLastName) {
    this.holderLastName = holderLastName;
  }

  public Integer getSsn() {
    return ssn;
  }

  public void setSsn(Integer ssn) {
    this.ssn = ssn;
  }

  public Integer getVoterCardId() {
    return voterCardId;
  }

  public void setVoterCardId(Integer voterCardId) {
    this.voterCardId = voterCardId;
  }
}
