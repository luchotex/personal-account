package com.g2.personalaccount.model;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-30 18:03
 */
@Entity
public class Balance extends EditionDates {

  // TODO to be defined in future issue

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "balance_generator")
  @SequenceGenerator(
      name = "balance_generator",
      sequenceName = "balance_id_seq",
      allocationSize = 1)
  @Column(name = "balance_id")
  private Long id;

  @Column(name = "amount")
  private BigDecimal amount;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }
}
