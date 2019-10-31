package com.g2.personalaccount.model;

import com.g2.personalaccount.model.enumerated.TypeEnum;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-30 18:04
 */
@Entity
public class Transaction extends EditionDates {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transaction_generator")
  @SequenceGenerator(
      name = "transaction_generator",
      sequenceName = "transaction_id_seq",
      allocationSize = 1)
  @Column(name = "transaction_id")
  private Long id;

  private TypeEnum typeEnum;

  private BigDecimal amount;

  private String description;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public TypeEnum getTypeEnum() {
    return typeEnum;
  }

  public void setTypeEnum(TypeEnum typeEnum) {
    this.typeEnum = typeEnum;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}