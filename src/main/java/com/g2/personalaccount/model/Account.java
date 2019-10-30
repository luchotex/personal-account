package com.g2.personalaccount.model;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-30 18:03
 */
@Entity
public class Account extends EditionDates {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_generator")
  @SequenceGenerator(
      name = "account_generator",
      sequenceName = "account_id_seq",
      allocationSize = 1)
  @Column(name = "account_id")
  private Long id;

  @Embedded private AccountHolder accountHolder;

  @OneToOne
  @JoinColumn(name = "account_access_id", referencedColumnName = "account_access_id")
  private AccountAccess accountAccess;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public AccountHolder getAccountHolder() {
    return accountHolder;
  }

  public void setAccountHolder(AccountHolder accountHolder) {
    this.accountHolder = accountHolder;
  }

  public AccountAccess getAccountAccess() {
    return accountAccess;
  }

  public void setAccountAccess(AccountAccess accountAccess) {
    this.accountAccess = accountAccess;
  }
}
