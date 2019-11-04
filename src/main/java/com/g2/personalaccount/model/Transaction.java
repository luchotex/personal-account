package com.g2.personalaccount.model;

import com.g2.personalaccount.model.enumerated.TransactionStatusEnum;
import com.g2.personalaccount.model.enumerated.TypeEnum;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-30 18:04
 */
@Entity
@Data
@Table(indexes = {@Index(name = "idx_account_ordered", columnList = "account_id,create_dateTime")})
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

  @Column(name = "elapsed_seconds")
  private Integer elapsedSeconds;

  @Column(name = "status")
  private TransactionStatusEnum status;

  @Column(name = "error_message")
  private String errorMessage;

  @ManyToOne
  @JoinColumn(name = "account_id")
  private Account account;
}
