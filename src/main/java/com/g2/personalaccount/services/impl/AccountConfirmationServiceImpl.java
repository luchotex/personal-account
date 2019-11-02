package com.g2.personalaccount.services.impl;

import com.g2.personalaccount.model.AccountConfirmation;
import com.g2.personalaccount.model.enumerated.ConfirmationStatusEnum;
import com.g2.personalaccount.model.enumerated.StatusEnum;
import com.g2.personalaccount.repositories.AccountConfirmationRepository;
import com.g2.personalaccount.services.AccountConfirmationService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-11-01 15:07
 */
@Service
public class AccountConfirmationServiceImpl implements AccountConfirmationService {

  public static final String CONFIRMATION_DOESNT_EXIST_TEMPLATE = "confirmation-doesnt-exist";
  public static final String CONFIRMATION_ALREADY_EXECUTED_TEMPLATE =
      "confirmation-already-executed";
  public static final String CONFIRMATION_HAS_EXPIRED_TEMPLATE = "confirmation-has-expired";
  public static final String CONFIRMATION_SUCCESSFUL_TEMPLATE = "confirmation";
  private AccountConfirmationRepository accountConfirmationRepository;

  public AccountConfirmationServiceImpl(
      AccountConfirmationRepository accountConfirmationRepository) {
    this.accountConfirmationRepository = accountConfirmationRepository;
  }

  @Override
  public String confirmCreation(String confirmationId) {

    Optional<AccountConfirmation> accountConfirmation =
        accountConfirmationRepository.findByConfirmationId(confirmationId);

    if (!accountConfirmation.isPresent()) {
      return CONFIRMATION_DOESNT_EXIST_TEMPLATE;
    }

    AccountConfirmation confirmation = accountConfirmation.get();

    if (confirmation.getConfirmationStatusEnum().equals(ConfirmationStatusEnum.CONFIRMED)) {
      return CONFIRMATION_ALREADY_EXECUTED_TEMPLATE;
    }

    LocalDateTime now = LocalDateTime.now();

    if (confirmation.getExpirationDate().isBefore(now)) {
      return CONFIRMATION_HAS_EXPIRED_TEMPLATE;
    }

    confirmation.setConfirmationStatusEnum(ConfirmationStatusEnum.CONFIRMED);
    confirmation.getAccount().setStatus(StatusEnum.ACTIVE);
    accountConfirmationRepository.save(confirmation);

    return CONFIRMATION_SUCCESSFUL_TEMPLATE;
  }
}
