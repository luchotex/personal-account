package com.g2.personalaccount.model;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Data;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-11-04 08:06
 */
@Entity
@Data
@Table(indexes = {@Index(name = "idx_account_lock", columnList = "account_id")})
public class AccountLock {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_lock_generator")
  @SequenceGenerator(
      name = "account_lock_generator",
      sequenceName = "account_lock_id_seq",
      allocationSize = 1)
  @Column(name = "account_lock_id")
  private Long id;

  @Column(name = "thread_name")
  private String threadName;

  @Column(name = "expiration_date")
  private LocalDateTime expirationDate;

  @OneToOne
  @JoinColumn(name = "account_id")
  private Account account;
}
