package com.g2.personalaccount.dto.requests;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-31 16:21
 */
public class AccountRequest {

  @NotNull(message = "The First name must have a value")
  @NotEmpty(message = "The First name musn't be empty value")
  private String holderFirstName;

  @NotNull(message = "The Last name must have a value")
  @NotEmpty(message = "The last name musn't be empty value")
  private String holderLastName;

  @NotNull(message = "The ssn must have a value")
  private Integer ssn;

  @NotNull(message = "The Voter card Id must have a value")
  private Integer voterCardId;

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
