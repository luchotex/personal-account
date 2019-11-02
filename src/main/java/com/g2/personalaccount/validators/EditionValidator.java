package com.g2.personalaccount.validators;

import static com.g2.personalaccount.services.impl.AccountServiceImpl.ACCOUNT_EMAIL_ON_CONFIRMATION;
import static com.g2.personalaccount.services.impl.AccountServiceImpl.ACCOUNT_NUMBER_DOESNT_EXISTS;
import static com.g2.personalaccount.services.impl.AccountServiceImpl.ACCOUNT_SSN_ON_CONFIRMATION;
import static com.g2.personalaccount.services.impl.AccountServiceImpl.ACCOUNT_WITH_SAME_EMAIL_EXISTS;
import static com.g2.personalaccount.services.impl.AccountServiceImpl.ACCOUNT_WITH_SAME_SSN_EXIST;
import static com.g2.personalaccount.services.impl.AccountServiceImpl.EMAIL_ALREADY_EXISTS_IN_ANOTHER_ACCOUNT;
import static com.g2.personalaccount.services.impl.AccountServiceImpl.PIN_IS_INCORRECT;
import static com.g2.personalaccount.services.impl.AccountServiceImpl.THE_ACCOUNT_NUMBER_IS_NOT_ACTIVE;

import com.g2.personalaccount.dto.requests.AccountRequest;
import com.g2.personalaccount.dto.requests.AccountUpdateRequest;
import com.g2.personalaccount.dto.requests.AuthenticationRequest;
import com.g2.personalaccount.exceptions.InvalidArgumentsException;
import com.g2.personalaccount.model.Account;
import com.g2.personalaccount.model.enumerated.StatusEnum;
import com.g2.personalaccount.repositories.AccountRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-11-02 00:29
 */
@Component
public class EditionValidator {

  private AccountRepository accountRepository;

  public EditionValidator(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  public void validateCreation(AccountRequest request) {
    Optional<Account> foundSSNAccount =
        accountRepository.findByAccountHolder_AccountHolderId_Ssn(request.getSsn());

    if (foundSSNAccount.isPresent()) {
      if (foundSSNAccount.get().getStatus().equals(StatusEnum.ON_CONFIRM)) {
        throw new InvalidArgumentsException(
            String.format(
                ACCOUNT_SSN_ON_CONFIRMATION, foundSSNAccount.get().getId(), request.getSsn()));
      } else if (foundSSNAccount.get().getStatus().equals(StatusEnum.ACTIVE)) {
        throw new InvalidArgumentsException(
            String.format(ACCOUNT_WITH_SAME_SSN_EXIST, request.getSsn()));
      }
    }

    Optional<Account> foundEmailAccount =
        accountRepository.findByAccountHolder_Email(request.getEmail());

    if (foundEmailAccount.isPresent()) {
      if (foundEmailAccount.get().getStatus().equals(StatusEnum.ON_CONFIRM)) {
        throw new InvalidArgumentsException(
            String.format(
                ACCOUNT_EMAIL_ON_CONFIRMATION,
                foundEmailAccount.get().getId(),
                request.getEmail()));
      } else if (foundEmailAccount.get().getStatus().equals(StatusEnum.ACTIVE)) {
        throw new InvalidArgumentsException(
            String.format(ACCOUNT_WITH_SAME_EMAIL_EXISTS, request.getEmail()));
      }
    }
  }

  public Account validateUpdate(
      AccountUpdateRequest request, Optional<Account> foundAccountOptional) {
    if (!foundAccountOptional.isPresent()) {
      throw new InvalidArgumentsException(
          String.format(ACCOUNT_NUMBER_DOESNT_EXISTS, request.getId()));
    }

    Account foundAccount = foundAccountOptional.get();

    if (!foundAccount.getStatus().equals(StatusEnum.ACTIVE)) {
      throw new InvalidArgumentsException(
          String.format(THE_ACCOUNT_NUMBER_IS_NOT_ACTIVE, request.getId()));
    }

    Optional<Account> foundEmailAccountOptional =
        accountRepository.findByAccountHolder_Email(request.getEmail());

    if (foundEmailAccountOptional.isPresent()
        && !foundAccount.getId().equals(foundEmailAccountOptional.get().getId())) {
      throw new InvalidArgumentsException(
          String.format(EMAIL_ALREADY_EXISTS_IN_ANOTHER_ACCOUNT, request.getEmail()));
    }

    if (foundAccount.getAccountAccess().getAuthenticationExpiration() == null
        || foundAccount
            .getAccountAccess()
            .getAuthenticationExpiration()
            .isBefore(LocalDateTime.now())) {
      throw new InvalidArgumentsException(
          String.format(
              "The account number is not authenticated to perform this operation",
              request.getId()));
    }
    return foundAccount;
  }

  public Account validateAuthentication(
      AuthenticationRequest request, Optional<Account> foundAccountOptional) {
    if (!foundAccountOptional.isPresent()) {
      throw new InvalidArgumentsException(
          String.format(ACCOUNT_NUMBER_DOESNT_EXISTS, request.getAccountNumber()));
    }

    Account foundAccount = foundAccountOptional.get();

    if (!foundAccount.getStatus().equals(StatusEnum.ACTIVE)) {
      throw new InvalidArgumentsException(
          String.format(THE_ACCOUNT_NUMBER_IS_NOT_ACTIVE, request.getAccountNumber()));
    }

    if (!foundAccount.getAccountAccess().getPin().equals(request.getPin())) {
      throw new InvalidArgumentsException(
          String.format(PIN_IS_INCORRECT, request.getAccountNumber()));
    }
    return foundAccount;
  }
}
