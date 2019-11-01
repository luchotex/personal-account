package com.g2.personalaccount.model;

import com.g2.personalaccount.model.enumerated.StatusEnum;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-30 18:03
 */
@Entity
@Data
public class Account extends EditionDates {

  @Id
  @GeneratedValue(generator = "account-number-generator")
  @GenericGenerator(
      name = "account-number-generator",
      parameters = @Parameter(name = "prefix", value = "35"),
      strategy = "com.g2.personalaccount.model.generator.AccountNumberGenerator")
  @Column(name = "account_id")
  private Long id;

  @Embedded private AccountHolder accountHolder;

  @OneToOne(cascade = CascadeType.PERSIST)
  @JoinColumn(name = "account_access_id", referencedColumnName = "account_access_id")
  private AccountAccess accountAccess;

  @Column(name = "status")
  private StatusEnum status;
}
