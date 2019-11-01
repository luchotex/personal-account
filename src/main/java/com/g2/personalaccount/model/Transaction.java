package com.g2.personalaccount.model;

import com.g2.personalaccount.model.enumerated.TypeEnum;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import lombok.Data;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-30 18:04
 */
@Entity
@Data
public class Transaction extends EditionDates {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transaction_generator")
  @SequenceGenerator(
      name = "transaction_generator",
      sequenceName = "transaction_id_seq",
      allocationSize = 1)
  @Column(name = "transaction_id")
  private Long id;

  @Column(name = "type")
  private TypeEnum type;

  @Column(name = "amount")
  private BigDecimal amount;

  @Column(name = "description")
  private String description;
}
