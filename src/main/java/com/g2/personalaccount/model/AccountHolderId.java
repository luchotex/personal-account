package com.g2.personalaccount.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.Data;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-30 18:03
 */
@Data
@Embeddable
public class AccountHolderId {
  @Column(name = "ssn")
  private Long ssn;

  @Column(name = "voter_card_id")
  private Long voterCardId;
}
