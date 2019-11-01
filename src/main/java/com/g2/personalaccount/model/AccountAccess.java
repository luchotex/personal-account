package com.g2.personalaccount.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import lombok.Data;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-30 19:03
 */
@Entity
@Data
public class AccountAccess extends EditionDates {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_access_generator")
  @SequenceGenerator(
      name = "account_access_generator",
      sequenceName = "account_access_id_seq",
      allocationSize = 1)
  @Column(name = "account_access_id")
  private Long id;

  @OneToOne(mappedBy = "accountAccess")
  private Account account;

  @Column(name = "pin")
  private String pin;
}
