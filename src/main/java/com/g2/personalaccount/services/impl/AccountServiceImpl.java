package com.g2.personalaccount.services.impl;

import com.g2.personalaccount.config.ServiceConfig;
import com.g2.personalaccount.dto.mappers.AccountMapper;
import com.g2.personalaccount.dto.requests.AccountRequest;
import com.g2.personalaccount.dto.requests.AccountUpdateRequest;
import com.g2.personalaccount.dto.responses.AccountResponse;
import com.g2.personalaccount.exceptions.InvalidArgumentsException;
import com.g2.personalaccount.model.Account;
import com.g2.personalaccount.proxy.EmailProxy;
import com.g2.personalaccount.repositories.AccountRepository;
import com.g2.personalaccount.services.AccountService;
import com.g2.personalaccount.utils.PinGenerator;
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
  public static final String EMAIL_CORRUPTED_DATA =
      "There is corrupted data related with the email";
  private Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

  private AccountMapper accountMapper;
  private AccountRepository accountRepository;
  private EmailProxy emailProxy;
  private PinGenerator pinGenerator;
  private ServiceConfig serviceConfig;

  public AccountServiceImpl(
      AccountMapper accountMapper,
      AccountRepository accountRepository,
      EmailProxy emailProxy,
      PinGenerator pinGenerator,
      ServiceConfig serviceConfig) {
    this.accountMapper = accountMapper;
    this.accountRepository = accountRepository;
    this.emailProxy = emailProxy;
    this.pinGenerator = pinGenerator;
    this.serviceConfig = serviceConfig;
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

    Optional<Account> foundSSNAccount =
        accountRepository.findByAccountHolder_AccountHolderId_Ssn(request.getSsn());

    if (foundSSNAccount.isPresent()) {
      throw new InvalidArgumentsException(
          String.format(ACCOUNT_WITH_SAME_SSN_EXIST, request.getSsn()));
    }

    Optional<Account> foundEmailAccount =
        accountRepository.findByAccountHolder_Email(request.getEmail());

    if (foundEmailAccount.isPresent()) {
      throw new InvalidArgumentsException(
          String.format(ACCOUNT_WITH_SAME_EMAIL_EXISTS, request.getEmail()));
    }

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

    if (!foundAccountOptional.isPresent()) {
      throw new InvalidArgumentsException(
          String.format(ACCOUNT_NUMBER_DOESNT_EXISTS, request.getId()));
    }

    Account foundAccount = foundAccountOptional.get();

    Optional<Account> foundEmailAccountOptional =
        accountRepository.findByAccountHolder_Email(request.getEmail());

    if (!foundEmailAccountOptional.isPresent()) {
      throw new InvalidArgumentsException(String.format(EMAIL_CORRUPTED_DATA));
    }

    if (!foundAccount.getId().equals(foundEmailAccountOptional.get().getId())) {
      throw new InvalidArgumentsException(
          String.format(EMAIL_ALREADY_EXISTS_IN_ANOTHER_ACCOUNT, request.getEmail()));
    }

    accountMapper.toEntity(request, foundAccount);

    foundAccount = accountRepository.save(foundAccount);

    return accountMapper.toResponse(foundAccount);
  }
}
