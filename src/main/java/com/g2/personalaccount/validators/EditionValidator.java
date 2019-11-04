package com.g2.personalaccount.validators;

import static com.g2.personalaccount.services.impl.AccountServiceImpl.ACCOUNT_EMAIL_ON_CONFIRMATION;
import static com.g2.personalaccount.services.impl.AccountServiceImpl.ACCOUNT_IS_LOCKED;
import static com.g2.personalaccount.services.impl.AccountServiceImpl.ACCOUNT_NUMBER_DOESNT_EXISTS;
import static com.g2.personalaccount.services.impl.AccountServiceImpl.ACCOUNT_SSN_ON_CONFIRMATION;
import static com.g2.personalaccount.services.impl.AccountServiceImpl.ACCOUNT_WITH_SAME_EMAIL_EXISTS;
import static com.g2.personalaccount.services.impl.AccountServiceImpl.ACCOUNT_WITH_SAME_SSN_EXIST;
import static com.g2.personalaccount.services.impl.AccountServiceImpl.ALREADY_CLOSED_ACCOUNT;
import static com.g2.personalaccount.services.impl.AccountServiceImpl.EMAIL_ALREADY_EXISTS_IN_ANOTHER_ACCOUNT;
import static com.g2.personalaccount.services.impl.AccountServiceImpl.IS_NOT_AUTHENTICATED_TO_PERFORM_THIS_OPERATION;
import static com.g2.personalaccount.services.impl.AccountServiceImpl.PIN_IS_INCORRECT;
import static com.g2.personalaccount.services.impl.AccountServiceImpl.THE_ACCOUNT_IS_CLOSED;
import static com.g2.personalaccount.services.impl.AccountServiceImpl.THE_ACCOUNT_NUMBER_IS_ON_CONFIRMATION;

import com.g2.personalaccount.config.ServiceConfig;
import com.g2.personalaccount.dto.requests.AccountCloseRequest;
import com.g2.personalaccount.dto.requests.AccountRequest;
import com.g2.personalaccount.dto.requests.AccountUpdateRequest;
import com.g2.personalaccount.dto.requests.AuthenticationRequest;
import com.g2.personalaccount.exceptions.InvalidArgumentsException;
import com.g2.personalaccount.exceptions.ResourceNotFoundException;
import com.g2.personalaccount.model.Account;
import com.g2.personalaccount.model.enumerated.StatusEnum;
import com.g2.personalaccount.repositories.AccountRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-11-02 00:29
 */
@Component
public class EditionValidator {

  private AccountRepository accountRepository;
  private ServiceConfig serviceConfig;

  public EditionValidator(AccountRepository accountRepository, ServiceConfig serviceConfig) {
    this.accountRepository = accountRepository;
    this.serviceConfig = serviceConfig;
  }

  public void validateCreation(AccountRequest request) {
    Optional<Account> foundSSNAccount =
        accountRepository.findByAccountHolder_AccountHolderId_SsnAndStatusIn(
            request.getSsn(), Arrays.asList(StatusEnum.ACTIVE, StatusEnum.ON_CONFIRM));

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
        accountRepository.findByAccountHolder_EmailAndStatusIn(
            request.getEmail(), Arrays.asList(StatusEnum.ACTIVE, StatusEnum.ON_CONFIRM));

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
    Account foundAccount =
        validateExistConfirmation(foundAccountOptional, request.getAccountNumber());

    if (foundAccount.getStatus().equals(StatusEnum.INACTIVE)) {
      throw new InvalidArgumentsException(
          String.format(THE_ACCOUNT_IS_CLOSED, request.getAccountNumber()));
    }

    Optional<Account> foundEmailAccountOptional =
        accountRepository.findByAccountHolder_EmailAndStatusIn(
            request.getEmail(), Arrays.asList(StatusEnum.ACTIVE, StatusEnum.ON_CONFIRM));

    if (foundEmailAccountOptional.isPresent()
        && !foundAccount.getId().equals(foundEmailAccountOptional.get().getId())) {
      throw new InvalidArgumentsException(
          String.format(EMAIL_ALREADY_EXISTS_IN_ANOTHER_ACCOUNT, request.getEmail()));
    }

    validateLockingAccount(foundAccount);

    if (Objects.isNull(foundAccount.getAccountAccess().getAuthenticationExpiration())
        || foundAccount
            .getAccountAccess()
            .getAuthenticationExpiration()
            .isBefore(LocalDateTime.now())) {
      throw new InvalidArgumentsException(IS_NOT_AUTHENTICATED_TO_PERFORM_THIS_OPERATION);
    }

    return foundAccount;
  }

  private Account validateExistConfirmation(Optional<Account> foundAccountOptional, Long id) {
    if (!foundAccountOptional.isPresent()) {
      throw new ResourceNotFoundException(String.format(ACCOUNT_NUMBER_DOESNT_EXISTS, id));
    }

    Account foundAccount = foundAccountOptional.get();

    if (foundAccount.getStatus().equals(StatusEnum.ON_CONFIRM)) {
      throw new InvalidArgumentsException(String.format(THE_ACCOUNT_NUMBER_IS_ON_CONFIRMATION, id));
    }
    return foundAccount;
  }

  public Account validateAuthentication(
      AuthenticationRequest request, Optional<Account> foundAccountOptional) {
    Account foundAccount =
        validateExistConfirmation(foundAccountOptional, request.getAccountNumber());

    if (foundAccount.getStatus().equals(StatusEnum.INACTIVE)) {
      throw new InvalidArgumentsException(
          String.format(THE_ACCOUNT_IS_CLOSED, request.getAccountNumber()));
    }

    if (!foundAccount.getAccountAccess().getPin().equals(request.getPin())) {
      validateIncorrectPin(request, foundAccount);
    } else {
      validateLockingAccount(foundAccount);
    }

    return foundAccount;
  }

  private void validateIncorrectPin(AuthenticationRequest request, Account foundAccount) {
    LocalDateTime now = LocalDateTime.now();

    LocalDateTime lockingDateTime = foundAccount.getAccountAccess().getAuthenticationLocking();

    Integer numberOfRetries = retrieveNumberEntries(foundAccount, lockingDateTime, now);

    foundAccount.getAccountAccess().setNumberRetries(numberOfRetries);

    if (foundAccount.getAccountAccess().getNumberRetries()
        >= Integer.valueOf(serviceConfig.getNumberPinRetries())) {
      foundAccount
          .getAccountAccess()
          .setAuthenticationLocking(
              now.plusSeconds(Integer.valueOf(serviceConfig.getAccountLockingSeconds())));
    }

    accountRepository.save(foundAccount);
    validateLockingAccount(foundAccount);

    throw new InvalidArgumentsException(
        String.format(PIN_IS_INCORRECT, request.getAccountNumber()));
  }

  private Integer retrieveNumberEntries(
      Account foundAccount, LocalDateTime lockingDateTime, LocalDateTime now) {
    Integer numberEntries;

    if (!Objects.isNull(foundAccount.getAccountAccess().getNumberRetries())) {
      if (!Objects.isNull(lockingDateTime) && (lockingDateTime.isBefore(now))) {
        numberEntries = 1;
        foundAccount.getAccountAccess().setAuthenticationLocking(null);
      } else {
        numberEntries = foundAccount.getAccountAccess().getNumberRetries() + 1;
      }
    } else {
      numberEntries = 1;
    }

    return numberEntries;
  }

  public Account closeAccountValidations(
      AccountCloseRequest request, Optional<Account> foundAccountOptional) {
    Account foundAccount =
        validateExistConfirmation(foundAccountOptional, request.getAccountNumber());

    if (foundAccount.getStatus().equals(StatusEnum.INACTIVE)) {
      throw new InvalidArgumentsException(
          String.format(ALREADY_CLOSED_ACCOUNT, request.getAccountNumber()));
    }

    validateLockingAccount(foundAccount);

    if (Objects.isNull(foundAccount.getAccountAccess().getAuthenticationExpiration())
        || foundAccount
            .getAccountAccess()
            .getAuthenticationExpiration()
            .isBefore(LocalDateTime.now())) {
      throw new InvalidArgumentsException(IS_NOT_AUTHENTICATED_TO_PERFORM_THIS_OPERATION);
    }
    return foundAccount;
  }

  private void validateLockingAccount(Account foundAccount) {
    LocalDateTime now = LocalDateTime.now();

    LocalDateTime lockingDateTime = foundAccount.getAccountAccess().getAuthenticationLocking();

    if (Objects.nonNull(lockingDateTime) && (lockingDateTime.isAfter(now))) {
      throw new InvalidArgumentsException(
          String.format(
              ACCOUNT_IS_LOCKED, foundAccount.getAccountAccess().getAuthenticationLocking()));
    }
  }
}
