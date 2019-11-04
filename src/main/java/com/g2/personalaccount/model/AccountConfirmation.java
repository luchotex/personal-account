package com.g2.personalaccount.model;

import com.g2.personalaccount.model.enumerated.ConfirmationStatusEnum;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Data;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-11-01 14:31
 */
@Entity
@Data
@Table(
    uniqueConstraints = @UniqueConstraint(columnNames = {"confirmation_id"}),
    indexes = {@Index(name = "idx_account_confirmation", columnList = "confirmation_id")})
public class AccountConfirmation {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_confirmation_generator")
  @SequenceGenerator(
      name = "account_confirmation_generator",
      sequenceName = "account_confirmation_id_seq",
      allocationSize = 1)
  @Column(name = "account_confirmation_id")
  private Long id;

  @Column(name = "expiration_date")
  private LocalDateTime expirationDate;

  @Column(name = "status")
  private ConfirmationStatusEnum confirmationStatusEnum;

  @OneToOne(mappedBy = "accountConfirmation")
  private Account account;

  @Column(name = "confirmation_id")
  private String confirmationId;
}
