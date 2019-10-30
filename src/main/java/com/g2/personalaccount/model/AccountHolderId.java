package com.g2.personalaccount.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-30 18:03
 */
@Embeddable
public class AccountHolderId {
  @Column(name = "ssn")
  private Integer ssn;

  @Column(name = "voter_card_id")
  private Integer voterCardId;

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
