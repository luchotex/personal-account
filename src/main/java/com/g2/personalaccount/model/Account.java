package com.g2.personalaccount.model;

import com.g2.personalaccount.model.enumerated.StatusEnum;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-30 18:03
 */
@Entity
public class Account extends EditionDates {

  @Id
  @GeneratedValue(generator = "number")
  @GenericGenerator(
      name = "account-number-generator",
      parameters = @Parameter(name = "prefix", value = "35"),
      strategy = "com.g2.personalaccount.model.generator.AccountNumberGenerator")
  @Column(name = "account_id")
  private Long id;

  @Embedded private AccountHolder accountHolder;

  @OneToOne
  @JoinColumn(name = "account_access_id", referencedColumnName = "account_access_id")
  private AccountAccess accountAccess;

  @Column(name = "status")
  private StatusEnum status;

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

  public StatusEnum getStatus() {
    return status;
  }

  public void setStatus(StatusEnum status) {
    this.status = status;
  }
}
