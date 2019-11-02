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
  private Long ssn;

  @Column(name = "voter_card_id")
  private Long voterCardId;

  public Long getSsn() {
    return ssn;
  }

  public void setSsn(Long ssn) {
    this.ssn = ssn;
  }

  public Long getVoterCardId() {
    return voterCardId;
  }

  public void setVoterCardId(Long voterCardId) {
    this.voterCardId = voterCardId;
  }
}
