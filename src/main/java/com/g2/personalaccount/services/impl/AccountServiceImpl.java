package com.g2.personalaccount.services.impl;

import com.g2.personalaccount.config.ServiceConfig;
import com.g2.personalaccount.dto.mappers.AccountMapper;
import com.g2.personalaccount.dto.requests.AccountRequest;
import com.g2.personalaccount.dto.requests.AccountUpdateRequest;
import com.g2.personalaccount.dto.requests.AuthenticationRequest;
import com.g2.personalaccount.dto.responses.AccountResponse;
import com.g2.personalaccount.dto.responses.AuthenticationResponse;
import com.g2.personalaccount.model.Account;
import com.g2.personalaccount.proxy.EmailProxy;
import com.g2.personalaccount.repositories.AccountRepository;
import com.g2.personalaccount.services.AccountService;
import com.g2.personalaccount.utils.PinGenerator;
import com.g2.personalaccount.validators.EditionValidator;
import java.time.LocalDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-31 09:41
 */
@Service
public class AccountServiceImpl implements AccountService {

  public static final String ACCOUNT_WITH_SAME_SSN_EXIST = "The SSN %s already exists";
  public static final String ACCOUNT_WITH_SAME_EMAIL_EXISTS = "The email %s already exists";
  public static final String ACCOUNT_NUMBER_DOESNT_EXISTS = "The account number %s doesn't exists";
  public static final String EMAIL_ALREADY_EXISTS_IN_ANOTHER_ACCOUNT =
      "The email %s already exists in another account";

  public static final String PIN_IS_INCORRECT = "The PIN is incorrect for account number: %s";
  public static final String THE_ACCOUNT_NUMBER_IS_NOT_ACTIVE =
      "The account number %s has not active state";
  public static final String ACCOUNT_SSN_ON_CONFIRMATION =
      "The account number %s with SSN %s is waiting for confirmation";
  public static final String ACCOUNT_EMAIL_ON_CONFIRMATION =
      "The account number %s with email %s is waiting for confirmation";
  private Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

  private AccountMapper accountMapper;
  private AccountRepository accountRepository;
  private EmailProxy emailProxy;
  private PinGenerator pinGenerator;
  private ServiceConfig serviceConfig;
  private EditionValidator editionValidator;

  public AccountServiceImpl(
      AccountMapper accountMapper,
      AccountRepository accountRepository,
      EmailProxy emailProxy,
      PinGenerator pinGenerator,
      ServiceConfig serviceConfig,
      EditionValidator editionValidator) {
    this.accountMapper = accountMapper;
    this.accountRepository = accountRepository;
    this.emailProxy = emailProxy;
    this.pinGenerator = pinGenerator;
    this.serviceConfig = serviceConfig;
    this.editionValidator = editionValidator;
  }

  @Override
  public AccountResponse create(AccountRequest request) {

    logger.info(
        "Starting creation first Name: {}, last Name: {}, email: {}, ssn: {},  voterCardId: {}",
        request.getHolderFirstName(),
        request.getHolderLastName(),
        request.getEmail(),
        request.getSsn(),
        request.getVoterCardId());

    editionValidator.validateCreation(request);

    Integer generatedPin =
        pinGenerator.generateRandom(Integer.valueOf(serviceConfig.getPinLength()));

    Account account = accountMapper.toEntity(request, generatedPin);
    logger.info(
        "Account already created for first Name: {}, last Name: {}, email: {}, ssn: {},  voterCardId: {}",
        request.getHolderFirstName(),
        request.getHolderLastName(),
        request.getEmail(),
        request.getSsn(),
        request.getVoterCardId());

    account = accountRepository.save(account);

    emailProxy.sendPin(account.getAccountHolder().getEmail(), generatedPin);
    logger.info("Pin already sent for email: {}", account.getAccountHolder().getEmail());
    emailProxy.sendConfirmation(
        account.getAccountHolder().getEmail(),
        serviceConfig.getHostname(),
        account.getAccountConfirmation().getConfirmationId());
    logger.info("Confirmation already sent for email: {}", account.getAccountHolder().getEmail());

    return accountMapper.toResponse(account);
  }

  @Override
  public AccountResponse updatePersonalData(AccountUpdateRequest request) {

    Optional<Account> foundAccountOptional = accountRepository.findById(request.getId());

    Account foundAccount = editionValidator.validateUpdate(request, foundAccountOptional);

    accountMapper.toEntity(request, foundAccount);

    foundAccount = accountRepository.save(foundAccount);

    return accountMapper.toResponse(foundAccount);
  }

  @Override
  public AuthenticationResponse authenticateAccount(AuthenticationRequest request) {
    Optional<Account> foundAccountOptional = accountRepository.findById(request.getAccountNumber());

    Account foundAccount = editionValidator.validateAuthentication(request, foundAccountOptional);

    foundAccount
        .getAccountAccess()
        .setAuthenticationExpiration(
            LocalDateTime.now()
                .plusSeconds(Integer.valueOf(serviceConfig.getPinExpirationSeconds())));

    foundAccount = accountRepository.save(foundAccount);

    return accountMapper.toExpirationResponse(foundAccount);
  }
}
