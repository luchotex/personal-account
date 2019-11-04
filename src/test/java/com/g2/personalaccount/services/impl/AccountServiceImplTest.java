package com.g2.personalaccount.services.impl;

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
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.g2.personalaccount.config.ServiceConfig;
import com.g2.personalaccount.dto.mappers.AccountMapper;
import com.g2.personalaccount.dto.requests.AccountCloseRequest;
import com.g2.personalaccount.dto.requests.AccountRequest;
import com.g2.personalaccount.dto.requests.AccountUpdateRequest;
import com.g2.personalaccount.dto.requests.AuthenticationRequest;
import com.g2.personalaccount.dto.responses.AccountCloseResponse;
import com.g2.personalaccount.dto.responses.AccountResponse;
import com.g2.personalaccount.dto.responses.AuthenticationResponse;
import com.g2.personalaccount.exceptions.InvalidArgumentsException;
import com.g2.personalaccount.exceptions.ResourceNotFoundException;
import com.g2.personalaccount.model.Account;
import com.g2.personalaccount.model.enumerated.StatusEnum;
import com.g2.personalaccount.proxy.EmailProxy;
import com.g2.personalaccount.repositories.AccountRepository;
import com.g2.personalaccount.services.AccountService;
import com.g2.personalaccount.utils.AccountTestUtils;
import com.g2.personalaccount.utils.PinGenerator;
import com.g2.personalaccount.validators.EditionValidator;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/** This Kind of test was necessary due to using of decorators, so we need to autowire the mapper */
@ActiveProfiles("dev")
@RunWith(SpringRunner.class)
@SpringBootTest(
    properties = {
      "DATABASE_CONNECTION=jdbc:h2:mem:g2db;DB_CLOSE_ON_EXIT=FALSE",
      "DATABASE_USERNAME=sa",
      "DATABASE_PASSWORD=password",
      "MAIL_HOST=smtp.gmail.com",
      "MAIL_USERNAME=g2testservices@gmail.com",
      "MAIL_PASSWORD=Testg2123@",
      "MAIL_PORT=587",
      "SERVICE_URL=http://192.168.0.101:8080",
      "CONFIRMATION_EXPIRATION_SECONDS=8640",
      "PIN_LENGTH=4",
      "PIN_EXPIRATION_SECONDS=180",
      "NUMBER_PIN_RETRIES=5",
      "ACCOUNT_LOCKING_SECONDS=86400"
    })
public class AccountServiceImplTest {

  @MockBean private AccountService accountService;
  @MockBean private AccountRepository accountRepository;
  @Autowired private AccountMapper accountMapper;
  @MockBean private EmailProxy emailProxy;
  private PinGenerator pinGenerator;
  @Autowired private ServiceConfig serviceConfig;
  private Validator validator;
  private EditionValidator editionValidator;

  @Before
  public void setUp() {
    editionValidator = new EditionValidator(accountRepository, serviceConfig);
    pinGenerator = new PinGenerator();
    accountService =
        new AccountServiceImpl(
            accountMapper,
            accountRepository,
            emailProxy,
            pinGenerator,
            serviceConfig,
            editionValidator,
            null,
            null,
            null);

    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  public void create_saveSuccessfully() {

    // given
    AccountRequest request = AccountTestUtils.createAccountRequest();

    Account returnedAccount = AccountTestUtils.createAccount(request);

    when(accountRepository.findByAccountHolder_AccountHolderId_SsnAndStatusIn(anyLong(), any()))
        .thenReturn(Optional.empty());
    when(accountRepository.findByAccountHolder_EmailAndStatusIn(anyString(), any()))
        .thenReturn(Optional.empty());
    when(accountRepository.save(any())).thenReturn(returnedAccount);
    doNothing().when(emailProxy).sendPin(anyString(), anyInt());
    doNothing().when(emailProxy).sendConfirmation(anyString(), anyString(), anyString());

    // when

    AccountResponse response = accountService.create(request);

    // then
    Set<ConstraintViolation<AccountRequest>> violations = validator.validate(request);
    assertTrue(violations.isEmpty());

    assertNotNull(response);

    ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);

    verify(accountRepository, times(1))
        .findByAccountHolder_AccountHolderId_SsnAndStatusIn(anyLong(), any());
    verify(accountRepository, times(1)).findByAccountHolder_EmailAndStatusIn(anyString(), any());
    verify(emailProxy, times(1)).sendPin(anyString(), anyInt());
    verify(emailProxy, times(1)).sendConfirmation(anyString(), anyString(), anyString());
    verify(accountRepository, times(1)).save(accountArgumentCaptor.capture());

    Account savingValue = accountArgumentCaptor.getValue();

    assertNotNull(savingValue.getAccountAccess().getPin());
    assertEquals(32, savingValue.getAccountAccess().getPin().length());

    assertEquals(request.getHolderFirstName(), savingValue.getAccountHolder().getFirstName());
    assertEquals(request.getHolderLastName(), savingValue.getAccountHolder().getLastName());
    assertEquals(request.getEmail(), savingValue.getAccountHolder().getEmail());

    assertEquals(request.getSsn(), savingValue.getAccountHolder().getAccountHolderId().getSsn());
    assertEquals(
        request.getVoterCardId(),
        savingValue.getAccountHolder().getAccountHolderId().getVoterCardId());
    assertEquals(StatusEnum.ON_CONFIRM, savingValue.getStatus());

    assertNull(savingValue.getCreateDateTime());
    assertNull(savingValue.getUpdateDateTime());
    assertNull(savingValue.getAccountAccess().getCreateDateTime());
    assertNull(savingValue.getAccountAccess().getUpdateDateTime());

    assertNotNull(response.getAccountNumber());
    assertEquals(returnedAccount.getId(), response.getAccountNumber());

    assertNotNull(response.getHolderFirstName());
    assertEquals(returnedAccount.getAccountHolder().getFirstName(), response.getHolderFirstName());

    assertNotNull(response.getHolderLastName());
    assertEquals(returnedAccount.getAccountHolder().getLastName(), response.getHolderLastName());

    assertNotNull(response.getEmail());
    assertEquals(returnedAccount.getAccountHolder().getEmail(), response.getEmail());

    assertNotNull(response.getSsn());
    assertEquals(
        returnedAccount.getAccountHolder().getAccountHolderId().getSsn(), response.getSsn());

    assertNotNull(response.getVoterCardId());
    assertEquals(
        returnedAccount.getAccountHolder().getAccountHolderId().getVoterCardId(),
        response.getVoterCardId());
  }

  @Test
  public void create_saveSuccessfullyClosedBoth() {

    // given
    AccountRequest request = AccountTestUtils.createAccountRequest();

    Account returnedAccount = AccountTestUtils.createAccount(request);

    Account returnedSSNAccount = AccountTestUtils.createAccount(request);
    returnedSSNAccount.setStatus(StatusEnum.INACTIVE);
    Account returnedEmailAccount = AccountTestUtils.createAccount(request);
    returnedEmailAccount.setStatus(StatusEnum.INACTIVE);

    when(accountRepository.findByAccountHolder_AccountHolderId_SsnAndStatusIn(anyLong(), any()))
        .thenReturn(Optional.of(returnedSSNAccount));
    when(accountRepository.findByAccountHolder_EmailAndStatusIn(anyString(), any()))
        .thenReturn(Optional.of(returnedEmailAccount));
    when(accountRepository.save(any())).thenReturn(returnedAccount);
    doNothing().when(emailProxy).sendPin(anyString(), anyInt());
    doNothing().when(emailProxy).sendConfirmation(anyString(), anyString(), anyString());

    // when

    AccountResponse response = accountService.create(request);

    // then
    Set<ConstraintViolation<AccountRequest>> violations = validator.validate(request);
    assertTrue(violations.isEmpty());

    assertNotNull(response);

    ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);

    verify(accountRepository, times(1))
        .findByAccountHolder_AccountHolderId_SsnAndStatusIn(anyLong(), any());
    verify(accountRepository, times(1)).findByAccountHolder_EmailAndStatusIn(anyString(), any());
    verify(emailProxy, times(1)).sendPin(anyString(), anyInt());
    verify(emailProxy, times(1)).sendConfirmation(anyString(), anyString(), anyString());
    verify(accountRepository, times(1)).save(accountArgumentCaptor.capture());

    Account savingValue = accountArgumentCaptor.getValue();

    assertNotNull(savingValue.getAccountAccess().getPin());
    assertEquals(32, savingValue.getAccountAccess().getPin().length());

    assertEquals(request.getHolderFirstName(), savingValue.getAccountHolder().getFirstName());
    assertEquals(request.getHolderLastName(), savingValue.getAccountHolder().getLastName());
    assertEquals(request.getEmail(), savingValue.getAccountHolder().getEmail());

    assertEquals(request.getSsn(), savingValue.getAccountHolder().getAccountHolderId().getSsn());
    assertEquals(
        request.getVoterCardId(),
        savingValue.getAccountHolder().getAccountHolderId().getVoterCardId());
    assertEquals(StatusEnum.ON_CONFIRM, savingValue.getStatus());

    assertNull(savingValue.getCreateDateTime());
    assertNull(savingValue.getUpdateDateTime());
    assertNull(savingValue.getAccountAccess().getCreateDateTime());
    assertNull(savingValue.getAccountAccess().getUpdateDateTime());

    assertNotNull(response.getAccountNumber());
    assertEquals(returnedAccount.getId(), response.getAccountNumber());

    assertNotNull(response.getHolderFirstName());
    assertEquals(returnedAccount.getAccountHolder().getFirstName(), response.getHolderFirstName());

    assertNotNull(response.getHolderLastName());
    assertEquals(returnedAccount.getAccountHolder().getLastName(), response.getHolderLastName());

    assertNotNull(response.getEmail());
    assertEquals(returnedAccount.getAccountHolder().getEmail(), response.getEmail());

    assertNotNull(response.getSsn());
    assertEquals(
        returnedAccount.getAccountHolder().getAccountHolderId().getSsn(), response.getSsn());

    assertNotNull(response.getVoterCardId());
    assertEquals(
        returnedAccount.getAccountHolder().getAccountHolderId().getVoterCardId(),
        response.getVoterCardId());
  }

  @Test
  public void create_incorrectNameFormat() {

    // given
    AccountRequest request = AccountTestUtils.createAccountRequest();
    request.setHolderFirstName("test123");

    // then
    Set<ConstraintViolation<AccountRequest>> violations = validator.validate(request);
    assertFalse(violations.isEmpty());
    Optional<ConstraintViolation<AccountRequest>> optionalViolation =
        violations.stream()
            .filter(
                v ->
                    v.getMessage()
                        .contains("The first name must contain only characters or letters"))
            .findFirst();
    assertTrue(optionalViolation.isPresent());
  }

  @Test
  public void create_incorrectEmailFormat() {

    // given
    AccountRequest request = AccountTestUtils.createAccountRequest();
    request.setEmail("test");

    // then
    Set<ConstraintViolation<AccountRequest>> violations = validator.validate(request);
    assertFalse(violations.isEmpty());
    Optional<ConstraintViolation<AccountRequest>> optionalViolation =
        violations.stream()
            .filter(v -> v.getMessage().contains("Email must have valid format"))
            .findFirst();
    assertTrue(optionalViolation.isPresent());
  }

  @Test
  public void create_incorrectSsnLength() {

    // given
    AccountRequest request = AccountTestUtils.createAccountRequest();
    request.setSsn(123L);

    // then
    Set<ConstraintViolation<AccountRequest>> violations = validator.validate(request);
    assertFalse(violations.isEmpty());
    Optional<ConstraintViolation<AccountRequest>> optionalViolation =
        violations.stream()
            .filter(v -> v.getMessage().contains("SSN must be 9 digit number"))
            .findFirst();
    assertTrue(optionalViolation.isPresent());
  }

  @Test
  public void create_foundSsnAccount() {

    // given
    AccountRequest request = AccountTestUtils.createAccountRequest();

    Account returnedAccount = AccountTestUtils.createAccount(request);

    // when
    try {
      when(accountRepository.findByAccountHolder_AccountHolderId_SsnAndStatusIn(anyLong(), any()))
          .thenReturn(Optional.of(returnedAccount));
      // when

      AccountResponse response = accountService.create(request);
    } catch (InvalidArgumentsException ex) {
      // then
      verify(accountRepository, times(1))
          .findByAccountHolder_AccountHolderId_SsnAndStatusIn(anyLong(), any());
      assertEquals(String.format(ACCOUNT_WITH_SAME_SSN_EXIST, request.getSsn()), ex.getMessage());
    }
  }

  @Test
  public void create_foundSsnOnConfirmationAccount() {

    // given
    AccountRequest request = AccountTestUtils.createAccountRequest();

    Account returnedAccount = AccountTestUtils.createAccount(request);
    returnedAccount.setStatus(StatusEnum.ON_CONFIRM);

    // when
    try {
      when(accountRepository.findByAccountHolder_AccountHolderId_SsnAndStatusIn(anyLong(), any()))
          .thenReturn(Optional.of(returnedAccount));
      // when

      AccountResponse response = accountService.create(request);
    } catch (InvalidArgumentsException ex) {
      // then
      verify(accountRepository, times(1))
          .findByAccountHolder_AccountHolderId_SsnAndStatusIn(anyLong(), any());
      assertEquals(
          String.format(ACCOUNT_SSN_ON_CONFIRMATION, returnedAccount.getId(), request.getSsn()),
          ex.getMessage());
    }
  }

  @Test
  public void create_foundEmailAccount() {

    // given
    AccountRequest request = AccountTestUtils.createAccountRequest();

    Account returnedAccount = AccountTestUtils.createAccount(request);

    // when
    try {
      when(accountRepository.findByAccountHolder_AccountHolderId_SsnAndStatusIn(anyLong(), any()))
          .thenReturn(Optional.empty());

      when(accountRepository.findByAccountHolder_EmailAndStatusIn(anyString(), any()))
          .thenReturn(Optional.of(returnedAccount));
      // when

      AccountResponse response = accountService.create(request);
    } catch (InvalidArgumentsException ex) {
      // then
      verify(accountRepository, times(1))
          .findByAccountHolder_AccountHolderId_SsnAndStatusIn(anyLong(), any());
      verify(accountRepository, times(1)).findByAccountHolder_EmailAndStatusIn(anyString(), any());
      assertEquals(
          String.format(ACCOUNT_WITH_SAME_EMAIL_EXISTS, request.getEmail()), ex.getMessage());
    }
  }

  @Test
  public void create_foundEmailOnConfirmationAccount() {

    // given
    AccountRequest request = AccountTestUtils.createAccountRequest();

    Account returnedAccount = AccountTestUtils.createAccount(request);
    returnedAccount.setStatus(StatusEnum.ON_CONFIRM);

    // when
    try {
      when(accountRepository.findByAccountHolder_AccountHolderId_SsnAndStatusIn(anyLong(), any()))
          .thenReturn(Optional.empty());

      when(accountRepository.findByAccountHolder_EmailAndStatusIn(anyString(), any()))
          .thenReturn(Optional.of(returnedAccount));
      // when

      AccountResponse response = accountService.create(request);
    } catch (InvalidArgumentsException ex) {
      // then
      verify(accountRepository, times(1))
          .findByAccountHolder_AccountHolderId_SsnAndStatusIn(anyLong(), any());
      verify(accountRepository, times(1)).findByAccountHolder_EmailAndStatusIn(anyString(), any());
      assertEquals(
          String.format(ACCOUNT_EMAIL_ON_CONFIRMATION, returnedAccount.getId(), request.getEmail()),
          ex.getMessage());
    }
  }

  @Test
  public void updatePersonalData_updateSuccessfully() {

    // given
    AccountUpdateRequest request = AccountTestUtils.createAccountUpdateRequest();

    Account returnedAccount = AccountTestUtils.createUpdateAccount(request);

    Account foundAccount = AccountTestUtils.createUpdateAccount(request);
    foundAccount
        .getAccountAccess()
        .setAuthenticationExpiration(LocalDateTime.now().plusSeconds(30));

    when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));
    when(accountRepository.findByAccountHolder_EmailAndStatusIn(anyString(), any()))
        .thenReturn(Optional.of(foundAccount));

    when(accountRepository.save(any())).thenReturn(returnedAccount);

    // when
    AccountResponse response = accountService.updatePersonalData(request);

    // then
    assertNotNull(response);

    Set<ConstraintViolation<AccountUpdateRequest>> violations = validator.validate(request);
    assertTrue(violations.isEmpty());

    ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);

    verify(accountRepository, times(1)).findById(anyLong());
    verify(accountRepository, times(1)).findByAccountHolder_EmailAndStatusIn(anyString(), any());
    verify(accountRepository, times(1)).save(accountArgumentCaptor.capture());

    Account savingValue = accountArgumentCaptor.getValue();

    assertEquals(request.getHolderFirstName(), savingValue.getAccountHolder().getFirstName());
    assertEquals(request.getHolderLastName(), savingValue.getAccountHolder().getLastName());
    assertEquals(request.getEmail(), savingValue.getAccountHolder().getEmail());

    assertEquals(
        request.getVoterCardId(),
        savingValue.getAccountHolder().getAccountHolderId().getVoterCardId());
    assertEquals(StatusEnum.ACTIVE, savingValue.getStatus());

    assertNotNull(savingValue.getCreateDateTime());
    assertNotNull(savingValue.getUpdateDateTime());
    assertNotNull(savingValue.getAccountAccess().getCreateDateTime());
    assertNotNull(savingValue.getAccountAccess().getUpdateDateTime());

    assertNotNull(request.getAccountNumber());
    assertEquals(returnedAccount.getId(), request.getAccountNumber());

    assertNotNull(response.getHolderFirstName());
    assertEquals(returnedAccount.getAccountHolder().getFirstName(), response.getHolderFirstName());

    assertNotNull(response.getHolderLastName());
    assertEquals(returnedAccount.getAccountHolder().getLastName(), response.getHolderLastName());

    assertNotNull(response.getEmail());
    assertEquals(returnedAccount.getAccountHolder().getEmail(), response.getEmail());

    assertNotNull(response.getSsn());
    assertEquals(
        returnedAccount.getAccountHolder().getAccountHolderId().getSsn(), response.getSsn());

    assertNotNull(response.getVoterCardId());
    assertEquals(
        returnedAccount.getAccountHolder().getAccountHolderId().getVoterCardId(),
        response.getVoterCardId());
  }

  @Test
  public void updatePersonalData_updateSuccessfullyEmailNotFound() {

    // given
    AccountUpdateRequest request = AccountTestUtils.createAccountUpdateRequest();

    Account returnedAccount = AccountTestUtils.createUpdateAccount(request);

    Account foundAccount = AccountTestUtils.createUpdateAccount(request);
    foundAccount
        .getAccountAccess()
        .setAuthenticationExpiration(LocalDateTime.now().plusSeconds(30));

    when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));

    when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));
    when(accountRepository.findByAccountHolder_EmailAndStatusIn(anyString(), any()))
        .thenReturn(Optional.empty());

    when(accountRepository.save(any())).thenReturn(returnedAccount);

    // when
    AccountResponse response = accountService.updatePersonalData(request);

    // then
    assertNotNull(response);

    Set<ConstraintViolation<AccountUpdateRequest>> violations = validator.validate(request);
    assertTrue(violations.isEmpty());

    ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);

    verify(accountRepository, times(1)).findById(anyLong());
    verify(accountRepository, times(1)).findByAccountHolder_EmailAndStatusIn(anyString(), any());
    verify(accountRepository, times(1)).save(accountArgumentCaptor.capture());

    Account savingValue = accountArgumentCaptor.getValue();

    assertEquals(request.getHolderFirstName(), savingValue.getAccountHolder().getFirstName());
    assertEquals(request.getHolderLastName(), savingValue.getAccountHolder().getLastName());
    assertEquals(request.getEmail(), savingValue.getAccountHolder().getEmail());

    assertEquals(
        request.getVoterCardId(),
        savingValue.getAccountHolder().getAccountHolderId().getVoterCardId());
    assertEquals(StatusEnum.ACTIVE, savingValue.getStatus());

    assertNotNull(savingValue.getCreateDateTime());
    assertNotNull(savingValue.getUpdateDateTime());
    assertNotNull(savingValue.getAccountAccess().getCreateDateTime());
    assertNotNull(savingValue.getAccountAccess().getUpdateDateTime());

    assertNotNull(request.getAccountNumber());
    assertEquals(returnedAccount.getId(), request.getAccountNumber());

    assertNotNull(response.getHolderFirstName());
    assertEquals(returnedAccount.getAccountHolder().getFirstName(), response.getHolderFirstName());

    assertNotNull(response.getHolderLastName());
    assertEquals(returnedAccount.getAccountHolder().getLastName(), response.getHolderLastName());

    assertNotNull(response.getEmail());
    assertEquals(returnedAccount.getAccountHolder().getEmail(), response.getEmail());

    assertNotNull(response.getSsn());
    assertEquals(
        returnedAccount.getAccountHolder().getAccountHolderId().getSsn(), response.getSsn());

    assertNotNull(response.getVoterCardId());
    assertEquals(
        returnedAccount.getAccountHolder().getAccountHolderId().getVoterCardId(),
        response.getVoterCardId());
  }

  @Test
  public void update_accountNotfound() {

    // given
    AccountUpdateRequest request = AccountTestUtils.createAccountUpdateRequest();

    try {
      // when
      when(accountRepository.findById(anyLong())).thenReturn(Optional.empty());
      AccountResponse response = accountService.updatePersonalData(request);
    } catch (ResourceNotFoundException ex) {
      // then
      verify(accountRepository, times(1)).findById(anyLong());
      assertEquals(
          String.format(ACCOUNT_NUMBER_DOESNT_EXISTS, request.getAccountNumber()), ex.getMessage());
    }
  }

  @Test
  public void update_accountIsOnConfirm() {

    // given
    AccountUpdateRequest request = AccountTestUtils.createAccountUpdateRequest();
    Account foundAccount = AccountTestUtils.createUpdateAccount(request);
    foundAccount.setStatus(StatusEnum.ON_CONFIRM);
    Account emailFoundAccount = AccountTestUtils.createUpdateAccount(request);
    emailFoundAccount.setId(34324123L);

    try {
      // when
      when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));

      AccountResponse response = accountService.updatePersonalData(request);
    } catch (InvalidArgumentsException ex) {
      // then
      verify(accountRepository, times(1)).findById(anyLong());
      assertEquals(
          String.format(THE_ACCOUNT_NUMBER_IS_ON_CONFIRMATION, request.getAccountNumber()),
          ex.getMessage());
    }
  }

  @Test
  public void update_accountIsClosed() {

    // given
    AccountUpdateRequest request = AccountTestUtils.createAccountUpdateRequest();
    Account foundAccount = AccountTestUtils.createUpdateAccount(request);
    foundAccount.setStatus(StatusEnum.INACTIVE);
    Account emailFoundAccount = AccountTestUtils.createUpdateAccount(request);
    emailFoundAccount.setId(34324123L);

    try {
      // when
      when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));

      AccountResponse response = accountService.updatePersonalData(request);
    } catch (InvalidArgumentsException ex) {
      // then
      verify(accountRepository, times(1)).findById(anyLong());
      assertEquals(
          String.format(THE_ACCOUNT_IS_CLOSED, request.getAccountNumber()), ex.getMessage());
    }
  }

  @Test
  public void update_notYetAuthenticated() {

    // given
    AccountUpdateRequest request = AccountTestUtils.createAccountUpdateRequest();
    Account foundAccount = AccountTestUtils.createUpdateAccount(request);

    try {
      // when

      when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));

      AccountResponse response = accountService.updatePersonalData(request);
    } catch (InvalidArgumentsException ex) {
      // then
      verify(accountRepository, times(1)).findById(anyLong());
      assertEquals(IS_NOT_AUTHENTICATED_TO_PERFORM_THIS_OPERATION, ex.getMessage());
    }
  }

  @Test
  public void update_accountLocked() {

    // given
    AccountUpdateRequest request = AccountTestUtils.createAccountUpdateRequest();
    Account foundAccount = AccountTestUtils.createUpdateAccount(request);
    foundAccount.getAccountAccess().setAuthenticationLocking(LocalDateTime.now().plusSeconds(100));

    try {
      // when

      when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));
      AccountResponse response = accountService.updatePersonalData(request);
    } catch (InvalidArgumentsException ex) {
      // then
      verify(accountRepository, times(1)).findById(anyLong());
      assertEquals(
          String.format(
              ACCOUNT_IS_LOCKED, foundAccount.getAccountAccess().getAuthenticationLocking()),
          ex.getMessage());
    }
  }

  @Test
  public void update_authenticationExpired() {

    // given
    AccountUpdateRequest request = AccountTestUtils.createAccountUpdateRequest();
    Account foundAccount = AccountTestUtils.createUpdateAccount(request);

    try {
      // when

      when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));

      AccountResponse response = accountService.updatePersonalData(request);
    } catch (InvalidArgumentsException ex) {
      // then
      verify(accountRepository, times(1)).findById(anyLong());
      assertEquals(IS_NOT_AUTHENTICATED_TO_PERFORM_THIS_OPERATION, ex.getMessage());
    }
  }

  @Test
  public void update_emailExistsInAnotherAccount() {

    // given
    AccountUpdateRequest request = AccountTestUtils.createAccountUpdateRequest();
    Account foundAccount = AccountTestUtils.createUpdateAccount(request);
    foundAccount
        .getAccountAccess()
        .setAuthenticationExpiration(LocalDateTime.now().plusSeconds(100));

    Account emailFoundAccount = AccountTestUtils.createUpdateAccount(request);
    emailFoundAccount.setId(34324123L);

    try {
      // when

      when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));

      when(accountRepository.findByAccountHolder_EmailAndStatusIn(anyString(), any()))
          .thenReturn(Optional.of(emailFoundAccount));
      AccountResponse response = accountService.updatePersonalData(request);
    } catch (InvalidArgumentsException ex) {
      // then
      verify(accountRepository, times(1)).findById(anyLong());
      verify(accountRepository, times(1)).findByAccountHolder_EmailAndStatusIn(anyString(), any());
      assertEquals(
          String.format(EMAIL_ALREADY_EXISTS_IN_ANOTHER_ACCOUNT, request.getEmail()),
          ex.getMessage());
    }
  }

  @Test
  public void authenticate_successfully() {

    // given
    AuthenticationRequest request = new AuthenticationRequest();
    request.setAccountNumber(1233252351L);
    request.setPin("234235432asds13sdf");

    Account foundAccount =
        AccountTestUtils.createUpdateAccount(AccountTestUtils.createAccountUpdateRequest());
    foundAccount.setStatus(StatusEnum.ACTIVE);
    foundAccount.getAccountAccess().setPin("234235432asds13sdf");

    // when
    when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));
    foundAccount
        .getAccountAccess()
        .setAuthenticationExpiration(LocalDateTime.now().plusSeconds(180));
    when(accountRepository.save(any())).thenReturn(foundAccount);
    AuthenticationResponse response = accountService.authenticateAccount(request);

    assertNotNull(response);
    ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);

    verify(accountRepository, times(1)).findById(anyLong());
    verify(accountRepository, times(1)).save(accountArgumentCaptor.capture());
    Account accountToSave = accountArgumentCaptor.getValue();
    assertTrue(
        accountToSave
            .getAccountAccess()
            .getAuthenticationExpiration()
            .isAfter(LocalDateTime.now()));
  }

  @Test
  public void authenticate_accountNotfound() {

    // given
    AuthenticationRequest request = new AuthenticationRequest();
    request.setAccountNumber(1233252351L);
    request.setPin("234235432asds13sdf");

    try {
      // when
      when(accountRepository.findById(anyLong())).thenReturn(Optional.empty());
      AuthenticationResponse response = accountService.authenticateAccount(request);
      // when
    } catch (ResourceNotFoundException ex) {
      // then
      verify(accountRepository, times(1)).findById(anyLong());
      assertEquals(
          String.format(ACCOUNT_NUMBER_DOESNT_EXISTS, request.getAccountNumber()), ex.getMessage());
    }
  }

  @Test
  public void authenticate_accountNotConfirmed() {

    // given
    AuthenticationRequest request = new AuthenticationRequest();
    request.setAccountNumber(1233252351L);
    request.setPin("234235432asds13sdf");

    Account foundAccount =
        AccountTestUtils.createUpdateAccount(AccountTestUtils.createAccountUpdateRequest());
    foundAccount.setStatus(StatusEnum.ON_CONFIRM);

    try {
      // when
      when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));
      AuthenticationResponse response = accountService.authenticateAccount(request);
      // when
    } catch (InvalidArgumentsException ex) {
      // then
      verify(accountRepository, times(1)).findById(anyLong());
      assertEquals(
          String.format(THE_ACCOUNT_NUMBER_IS_ON_CONFIRMATION, request.getAccountNumber()),
          ex.getMessage());
    }
  }

  @Test
  public void authenticate_accountClosed() {

    // given
    AuthenticationRequest request = new AuthenticationRequest();
    request.setAccountNumber(1233252351L);
    request.setPin("234235432asds13sdf");

    Account foundAccount =
        AccountTestUtils.createUpdateAccount(AccountTestUtils.createAccountUpdateRequest());
    foundAccount.setStatus(StatusEnum.INACTIVE);

    try {
      // when
      when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));
      AuthenticationResponse response = accountService.authenticateAccount(request);
      // when
    } catch (InvalidArgumentsException ex) {
      // then
      verify(accountRepository, times(1)).findById(anyLong());
      assertEquals(
          String.format(THE_ACCOUNT_IS_CLOSED, request.getAccountNumber()), ex.getMessage());
    }
  }

  @Test
  public void authenticate_pinIncorrect() {

    // given
    AuthenticationRequest request = new AuthenticationRequest();
    request.setAccountNumber(1233252351L);
    request.setPin("234235432asds13sdf");

    Account foundAccount =
        AccountTestUtils.createUpdateAccount(AccountTestUtils.createAccountUpdateRequest());
    foundAccount.setStatus(StatusEnum.ACTIVE);
    foundAccount.getAccountAccess().setPin("4324123fdjgkld");

    try {
      // when
      when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));
      when(accountRepository.save(any())).thenReturn(foundAccount);
      AuthenticationResponse response = accountService.authenticateAccount(request);
      // when
    } catch (InvalidArgumentsException ex) {
      // then

      ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);
      verify(accountRepository, times(1)).findById(anyLong());
      verify(accountRepository, times(1)).save(accountArgumentCaptor.capture());

      Account savingValue = accountArgumentCaptor.getValue();

      assertTrue(savingValue.getAccountAccess().getNumberRetries().equals(1));
      assertNull(savingValue.getAccountAccess().getAuthenticationLocking());

      assertEquals(String.format(PIN_IS_INCORRECT, request.getAccountNumber()), ex.getMessage());
    }
  }

  @Test
  public void authenticate_pinIncorrectMoreThanOneRetry() {

    // given
    AuthenticationRequest request = new AuthenticationRequest();
    request.setAccountNumber(1233252351L);
    request.setPin("234235432asds13sdf");

    Account foundAccount =
        AccountTestUtils.createUpdateAccount(AccountTestUtils.createAccountUpdateRequest());
    foundAccount.setStatus(StatusEnum.ACTIVE);
    foundAccount.getAccountAccess().setPin("4324123fdjgkld");
    foundAccount.getAccountAccess().setNumberRetries(3);

    try {
      // when
      when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));
      when(accountRepository.save(any())).thenReturn(foundAccount);
      AuthenticationResponse response = accountService.authenticateAccount(request);
      // when
    } catch (InvalidArgumentsException ex) {
      // then

      ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);
      verify(accountRepository, times(1)).findById(anyLong());
      verify(accountRepository, times(1)).save(accountArgumentCaptor.capture());

      Account savingValue = accountArgumentCaptor.getValue();

      assertTrue(savingValue.getAccountAccess().getNumberRetries().equals(4));
      assertNull(savingValue.getAccountAccess().getAuthenticationLocking());

      assertEquals(String.format(PIN_IS_INCORRECT, request.getAccountNumber()), ex.getMessage());
    }
  }

  @Test
  public void authenticate_pinIncorrectLockingAccount() {

    // given
    AuthenticationRequest request = new AuthenticationRequest();
    request.setAccountNumber(1233252351L);
    request.setPin("234235432asds13sdf");

    Account foundAccount =
        AccountTestUtils.createUpdateAccount(AccountTestUtils.createAccountUpdateRequest());
    foundAccount.setStatus(StatusEnum.ACTIVE);
    foundAccount.getAccountAccess().setPin("4324123fdjgkld");
    foundAccount.getAccountAccess().setNumberRetries(4);

    try {
      // when
      when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));
      when(accountRepository.save(any())).thenReturn(foundAccount);
      AuthenticationResponse response = accountService.authenticateAccount(request);
      // when
    } catch (InvalidArgumentsException ex) {
      // then

      ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);
      verify(accountRepository, times(1)).findById(anyLong());
      verify(accountRepository, times(1)).save(accountArgumentCaptor.capture());

      Account savingValue = accountArgumentCaptor.getValue();

      assertTrue(savingValue.getAccountAccess().getNumberRetries().equals(5));
      assertNotNull(savingValue.getAccountAccess().getAuthenticationLocking());
      assertTrue(
          savingValue.getAccountAccess().getAuthenticationLocking().isAfter(LocalDateTime.now()));
      assertEquals(
          String.format(
              ACCOUNT_IS_LOCKED, foundAccount.getAccountAccess().getAuthenticationLocking()),
          ex.getMessage());
    }
  }

  @Test
  public void authenticate_pinIncorrectLockingDateExpired() {

    // given
    AuthenticationRequest request = new AuthenticationRequest();
    request.setAccountNumber(1233252351L);
    request.setPin("234235432asds13sdf");

    Account foundAccount =
        AccountTestUtils.createUpdateAccount(AccountTestUtils.createAccountUpdateRequest());
    foundAccount.setStatus(StatusEnum.ACTIVE);
    foundAccount.getAccountAccess().setPin("4324123fdjgkld");
    foundAccount.getAccountAccess().setNumberRetries(4);
    foundAccount.getAccountAccess().setAuthenticationLocking(LocalDateTime.now().minusSeconds(100));

    try {
      // when
      when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));
      when(accountRepository.save(any())).thenReturn(foundAccount);
      AuthenticationResponse response = accountService.authenticateAccount(request);
      // when
    } catch (InvalidArgumentsException ex) {
      // then

      ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);
      verify(accountRepository, times(1)).findById(anyLong());
      verify(accountRepository, times(1)).save(accountArgumentCaptor.capture());

      Account savingValue = accountArgumentCaptor.getValue();

      assertTrue(savingValue.getAccountAccess().getNumberRetries().equals(1));
      assertNull(savingValue.getAccountAccess().getAuthenticationLocking());

      assertEquals(String.format(PIN_IS_INCORRECT, request.getAccountNumber()), ex.getMessage());
    }
  }

  @Test
  public void authenticate_accountAlreadyLockedIncorrectPin() {

    // given
    AuthenticationRequest request = new AuthenticationRequest();
    request.setAccountNumber(1233252351L);
    request.setPin("234235432asds13sdf");

    Account foundAccount =
        AccountTestUtils.createUpdateAccount(AccountTestUtils.createAccountUpdateRequest());
    foundAccount.setStatus(StatusEnum.ACTIVE);
    foundAccount.getAccountAccess().setPin("4324123fdjgkld");
    foundAccount.getAccountAccess().setNumberRetries(5);
    foundAccount.getAccountAccess().setAuthenticationLocking(LocalDateTime.now().plusSeconds(100));

    try {
      // when
      when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));
      when(accountRepository.save(any())).thenReturn(foundAccount);
      AuthenticationResponse response = accountService.authenticateAccount(request);
      // when
    } catch (InvalidArgumentsException ex) {
      // then

      ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);
      verify(accountRepository, times(1)).findById(anyLong());
      verify(accountRepository, times(1)).save(accountArgumentCaptor.capture());

      Account savingValue = accountArgumentCaptor.getValue();

      assertTrue(savingValue.getAccountAccess().getNumberRetries().equals(6));
      assertNotNull(savingValue.getAccountAccess().getAuthenticationLocking());
      assertTrue(
          savingValue
              .getAccountAccess()
              .getAuthenticationLocking()
              .equals(foundAccount.getAccountAccess().getAuthenticationLocking()));

      assertEquals(
          String.format(
              ACCOUNT_IS_LOCKED, foundAccount.getAccountAccess().getAuthenticationLocking()),
          ex.getMessage());
    }
  }

  @Test
  public void authenticate_accountAlreadyLockedCorrectPin() {

    // given
    AuthenticationRequest request = new AuthenticationRequest();
    request.setAccountNumber(1233252351L);
    request.setPin("4324123fdjgkld");

    Account foundAccount =
        AccountTestUtils.createUpdateAccount(AccountTestUtils.createAccountUpdateRequest());
    foundAccount.setStatus(StatusEnum.ACTIVE);
    foundAccount.getAccountAccess().setPin("4324123fdjgkld");
    foundAccount.getAccountAccess().setNumberRetries(5);
    foundAccount.getAccountAccess().setAuthenticationLocking(LocalDateTime.now().plusSeconds(100));

    try {
      // when
      when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));
      AuthenticationResponse response = accountService.authenticateAccount(request);
      // when
    } catch (InvalidArgumentsException ex) {
      // then
      verify(accountRepository, times(1)).findById(anyLong());

      assertEquals(
          String.format(
              ACCOUNT_IS_LOCKED, foundAccount.getAccountAccess().getAuthenticationLocking()),
          ex.getMessage());
    }
  }

  @Test
  public void closed_accountNotfound() {

    // given
    AccountCloseRequest request = AccountTestUtils.createAccountClosed();

    try {
      // when
      when(accountRepository.findById(anyLong())).thenReturn(Optional.empty());
      AccountCloseResponse response = accountService.close(request);
      // when
    } catch (ResourceNotFoundException ex) {
      // then
      verify(accountRepository, times(1)).findById(anyLong());
      assertEquals(
          String.format(ACCOUNT_NUMBER_DOESNT_EXISTS, request.getAccountNumber()), ex.getMessage());
    }
  }

  @Test
  public void closed_successfully() {

    // given
    AccountCloseRequest request = AccountTestUtils.createAccountClosed();
    AccountUpdateRequest updateRequest = AccountTestUtils.createAccountUpdateRequest();
    Account foundAccount = AccountTestUtils.createUpdateAccount(updateRequest);
    Account emailFoundAccount = AccountTestUtils.createUpdateAccount(updateRequest);
    foundAccount
        .getAccountAccess()
        .setAuthenticationExpiration(LocalDateTime.now().plusSeconds(100));
    // when

    when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));

    AccountCloseResponse response = accountService.close(request);
    // when
    assertNotNull(response);

    ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);

    verify(accountRepository, times(1)).findById(anyLong());
    verify(accountRepository, times(1)).save(accountArgumentCaptor.capture());

    Account savingValue = accountArgumentCaptor.getValue();

    assertEquals(StatusEnum.INACTIVE, savingValue.getStatus());
  }

  @Test
  public void closed_accountNotConfirmed() {

    // given
    AccountCloseRequest request = AccountTestUtils.createAccountClosed();

    Account foundAccount =
        AccountTestUtils.createUpdateAccount(AccountTestUtils.createAccountUpdateRequest());
    foundAccount.setStatus(StatusEnum.ON_CONFIRM);

    try {
      // when
      when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));
      AccountCloseResponse response = accountService.close(request);
      // when
    } catch (InvalidArgumentsException ex) {
      // then
      verify(accountRepository, times(1)).findById(anyLong());
      assertEquals(
          String.format(THE_ACCOUNT_NUMBER_IS_ON_CONFIRMATION, request.getAccountNumber()),
          ex.getMessage());
    }
  }

  @Test
  public void closed_accountInactive() {

    // given
    AccountCloseRequest request = AccountTestUtils.createAccountClosed();

    Account foundAccount =
        AccountTestUtils.createUpdateAccount(AccountTestUtils.createAccountUpdateRequest());
    foundAccount.setStatus(StatusEnum.INACTIVE);

    try {
      // when
      when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));
      AccountCloseResponse response = accountService.close(request);
      // when
    } catch (InvalidArgumentsException ex) {
      // then
      verify(accountRepository, times(1)).findById(anyLong());
      assertEquals(
          String.format(ALREADY_CLOSED_ACCOUNT, request.getAccountNumber()), ex.getMessage());
    }
  }

  @Test
  public void closed_notYetAuthenticated() {

    // given
    AccountCloseRequest request = AccountTestUtils.createAccountClosed();
    AccountUpdateRequest updateRequest = AccountTestUtils.createAccountUpdateRequest();
    Account foundAccount = AccountTestUtils.createUpdateAccount(updateRequest);

    try {
      // when

      when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));
      AccountCloseResponse response = accountService.close(request);
      // when
    } catch (InvalidArgumentsException ex) {
      // then
      verify(accountRepository, times(1)).findById(anyLong());
      assertEquals(IS_NOT_AUTHENTICATED_TO_PERFORM_THIS_OPERATION, ex.getMessage());
    }
  }

  @Test
  public void closed_lockedAccount() {

    // given
    AccountCloseRequest request = AccountTestUtils.createAccountClosed();
    AccountUpdateRequest updateRequest = AccountTestUtils.createAccountUpdateRequest();
    Account foundAccount = AccountTestUtils.createUpdateAccount(updateRequest);
    foundAccount.getAccountAccess().setAuthenticationLocking(LocalDateTime.now().plusSeconds(100));

    try {
      // when

      when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));

      AccountCloseResponse response = accountService.close(request);
      // when
    } catch (InvalidArgumentsException ex) {
      // then
      verify(accountRepository, times(1)).findById(anyLong());
      assertEquals(
          String.format(
              ACCOUNT_IS_LOCKED, foundAccount.getAccountAccess().getAuthenticationLocking()),
          ex.getMessage());
    }
  }

  @Test
  public void closed_authenticationExpired() {

    // given
    AccountCloseRequest request = AccountTestUtils.createAccountClosed();
    AccountUpdateRequest updateRequest = AccountTestUtils.createAccountUpdateRequest();
    Account foundAccount = AccountTestUtils.createUpdateAccount(updateRequest);
    foundAccount
        .getAccountAccess()
        .setAuthenticationExpiration(LocalDateTime.now().minusSeconds(100));

    try {
      // when

      when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));

      AccountCloseResponse response = accountService.close(request);
      // when
    } catch (InvalidArgumentsException ex) {
      // then
      verify(accountRepository, times(1)).findById(anyLong());
      assertEquals(IS_NOT_AUTHENTICATED_TO_PERFORM_THIS_OPERATION, ex.getMessage());
    }
  }
}
