package com.g2.personalaccount.model;

import com.g2.personalaccount.model.enumerated.TypeEnum;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-30 18:04
 */
@Entity
@Data
public class Transaction extends EditionDates {

  @Id
  @GeneratedValue(generator = "transaction-id-generator")
  @GenericGenerator(
      name = "transaction-id-generator",
      strategy = "com.g2.personalaccount.model.generator.TransactionIdGenerator")
  @Column(name = "transaction_id")
  private Long id;

  @Column(name = "type")
  private TypeEnum type;

  @Column(name = "amount")
  private BigDecimal amount;

  @Column(name = "description")
  private String description;
}
