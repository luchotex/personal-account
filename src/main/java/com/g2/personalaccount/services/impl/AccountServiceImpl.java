package com.g2.personalaccount.services.impl;

import com.g2.personalaccount.config.ServiceConfig;
import com.g2.personalaccount.dto.mappers.AccountMapper;
import com.g2.personalaccount.dto.mappers.AuthenticationMapper;
import com.g2.personalaccount.dto.requests.AccountCloseRequest;
import com.g2.personalaccount.dto.requests.AccountRequest;
import com.g2.personalaccount.dto.requests.AccountUpdateRequest;
import com.g2.personalaccount.dto.requests.AuthenticationRequest;
import com.g2.personalaccount.dto.requests.ExternalMoneyMovementRequest;
import com.g2.personalaccount.dto.requests.MoneyMovementRequest;
import com.g2.personalaccount.dto.responses.AccountCloseResponse;
import com.g2.personalaccount.dto.responses.AccountResponse;
import com.g2.personalaccount.dto.responses.AuthenticationResponse;
import com.g2.personalaccount.dto.responses.MoneyMovementResponse;
import com.g2.personalaccount.model.Account;
import com.g2.personalaccount.model.Balance;
import com.g2.personalaccount.model.enumerated.StatusEnum;
import com.g2.personalaccount.model.enumerated.TypeEnum;
import com.g2.personalaccount.proxy.EmailProxy;
import com.g2.personalaccount.repositories.AccountRepository;
import com.g2.personalaccount.repositories.BalanceRepository;
import com.g2.personalaccount.services.AccountLockService;
import com.g2.personalaccount.services.AccountService;
import com.g2.personalaccount.services.BalanceService;
import com.g2.personalaccount.transaction.TransactionLogging;
import com.g2.personalaccount.utils.PinGenerator;
import com.g2.personalaccount.validators.EditionValidator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
  public static final String THE_ACCOUNT_NUMBER_IS_ON_CONFIRMATION =
      "The account number %s is waiting for confirmation";
  public static final String THE_ACCOUNT_IS_CLOSED = "The account number %s is closed";
  public static final String ACCOUNT_SSN_ON_CONFIRMATION =
      "The account number %s with SSN %s is waiting for confirmation";
  public static final String ACCOUNT_EMAIL_ON_CONFIRMATION =
      "The account number %s with email %s is waiting for confirmation";
  public static final String IS_NOT_AUTHENTICATED_TO_PERFORM_THIS_OPERATION =
      "The account number is not authenticated to perform this operation";
  public static final String ALREADY_CLOSED_ACCOUNT = "The account number %s is already closed";
  public static final String ACCOUNT_IS_LOCKED = "The account is locked, please wait until %s";
  private Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

  private AccountMapper accountMapper;
  private AccountRepository accountRepository;
  private EmailProxy emailProxy;
  private PinGenerator pinGenerator;
  private ServiceConfig serviceConfig;
  private EditionValidator editionValidator;
  private BalanceRepository balanceRepository;
  private BalanceService balanceService;
  private AccountLockService accountLockService;
  private AuthenticationMapper authenticationMapper;

  public AccountServiceImpl(
      AccountMapper accountMapper,
      AccountRepository accountRepository,
      EmailProxy emailProxy,
      PinGenerator pinGenerator,
      ServiceConfig serviceConfig,
      EditionValidator editionValidator,
      BalanceRepository balanceRepository,
      BalanceService balanceService,
      AccountLockService accountLockService,
      AuthenticationMapper authenticationMapper) {
    this.accountMapper = accountMapper;
    this.accountRepository = accountRepository;
    this.emailProxy = emailProxy;
    this.pinGenerator = pinGenerator;
    this.serviceConfig = serviceConfig;
    this.editionValidator = editionValidator;
    this.balanceRepository = balanceRepository;
    this.balanceService = balanceService;
    this.accountLockService = accountLockService;
    this.authenticationMapper = authenticationMapper;
  }

  @Override
  @TransactionLogging(TypeEnum.CREATION)
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
  @TransactionLogging(TypeEnum.UPDATE_PERSONAL_DATA)
  public AccountResponse updatePersonalData(AccountUpdateRequest request) {

    Optional<Account> foundAccountOptional = accountRepository.findById(request.getAccountNumber());

    Account foundAccount = editionValidator.validateUpdate(request, foundAccountOptional);

    accountMapper.toEntity(request, foundAccount);
    foundAccount.getAccountAccess().setAuthenticationExpiration(null);

    foundAccount = accountRepository.save(foundAccount);

    return accountMapper.toResponse(foundAccount);
  }

  @Override
  @TransactionLogging(TypeEnum.AUTHENTICATE)
  public AuthenticationResponse authenticateAccount(AuthenticationRequest request) {

    List<Balance> balances =
        balanceRepository.retrieveBalances(request.getAccountNumber(), new BigDecimal(10));

    Optional<Account> foundAccountOptional = accountRepository.findById(request.getAccountNumber());

    Account foundAccount = editionValidator.validateAuthentication(request, foundAccountOptional);

    foundAccount
        .getAccountAccess()
        .setAuthenticationExpiration(
            LocalDateTime.now()
                .plusSeconds(Integer.valueOf(serviceConfig.getPinExpirationSeconds())));
    foundAccount.getAccountAccess().setAuthenticationLocking(null);
    foundAccount.getAccountAccess().setNumberRetries(0);

    foundAccount = accountRepository.save(foundAccount);

    return accountMapper.toExpirationResponse(foundAccount);
  }

  @Override
  @TransactionLogging(TypeEnum.CLOSE)
  public AccountCloseResponse close(AccountCloseRequest request) {

    Optional<Account> foundAccountOptional = accountRepository.findById(request.getAccountNumber());

    Account foundAccount =
        editionValidator.validateAccount(
            foundAccountOptional, request.getAccountNumber(), ALREADY_CLOSED_ACCOUNT);

    foundAccount.setStatus(StatusEnum.INACTIVE);
    accountRepository.save(foundAccount);

    // TODO complete response
    return new AccountCloseResponse();
  }

  @Override
  @TransactionLogging(TypeEnum.DEPOSIT)
  public MoneyMovementResponse deposit(MoneyMovementRequest request) {

    String threadName = Thread.currentThread().getName();

    try {
      Account foundAccount = accountLockService.lockAccount(request.getAccountNumber(), threadName);

      balanceService.addBalances(request.getAmount(), foundAccount);
      foundAccount.getAccountAccess().setAuthenticationExpiration(null);
      accountRepository.save(foundAccount);
    } finally {
      balanceService.releaseBalances(threadName);
      accountLockService.releaseAccount(request.getAccountNumber(), threadName);
    }

    return new MoneyMovementResponse();
  }

  @Override
  @TransactionLogging(TypeEnum.WITHDRAWAL)
  public MoneyMovementResponse withDrawal(MoneyMovementRequest request) {

    String threadName = Thread.currentThread().getName();

    performPayment(request, threadName);

    return new MoneyMovementResponse();
  }

  private void performPayment(MoneyMovementRequest request, String threadName) {
    try {
      Account foundAccount = accountLockService.lockAccount(request.getAccountNumber(), threadName);

      List<Balance> balances =
          balanceService.getAndLockBalances(
              request.getAccountNumber(), request.getAmount(), threadName);

      balanceService.substractBalances(balances, request.getAmount());
      foundAccount.getAccountAccess().setAuthenticationExpiration(null);
      accountRepository.save(foundAccount);
    } finally {
      balanceService.releaseBalances(threadName);
      accountLockService.releaseAccount(request.getAccountNumber(), threadName);
    }
  }

  @Override
  @TransactionLogging(TypeEnum.DEBIT)
  public MoneyMovementResponse debit(ExternalMoneyMovementRequest request) {

    return performExternalMoneyMovement(request);
  }

  @Override
  @TransactionLogging(TypeEnum.CHECKS)
  public MoneyMovementResponse checkCharge(ExternalMoneyMovementRequest request) {

    return performExternalMoneyMovement(request);
  }

  private MoneyMovementResponse performExternalMoneyMovement(ExternalMoneyMovementRequest request) {

    authenticateAccount(authenticationMapper.toAuthenticationRequest(request));

    String threadName = Thread.currentThread().getName();

    performPayment(request, threadName);

    return new MoneyMovementResponse();
  }
}
