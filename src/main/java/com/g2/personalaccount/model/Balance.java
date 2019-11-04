package com.g2.personalaccount.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Data;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-30 18:03
 */
@Data
@Entity
@Table(
    indexes = {
      @Index(name = "idx_account_balance", columnList = "account_id"),
      @Index(name = "idx_locking_thread_name", columnList = "locking_thread_name")
    })
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

  @Column(name = "locking_thread_name")
  private String lockingThreadName;

  @Column(name = "locking_date_time")
  private LocalDateTime lockingDateTime;

  @ManyToOne
  @JoinColumn(name = "account_id")
  private Account account;
}
