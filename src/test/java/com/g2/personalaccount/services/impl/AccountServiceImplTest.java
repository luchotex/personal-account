package com.g2.personalaccount.services.impl;

import static com.g2.personalaccount.services.impl.AccountServiceImpl.ACCOUNT_NUMBER_DOESNT_EXISTS;
import static com.g2.personalaccount.services.impl.AccountServiceImpl.ACCOUNT_WITH_SAME_EMAIL_EXISTS;
import static com.g2.personalaccount.services.impl.AccountServiceImpl.ACCOUNT_WITH_SAME_SSN_EXIST;
import static com.g2.personalaccount.services.impl.AccountServiceImpl.EMAIL_ALREADY_EXISTS_IN_ANOTHER_ACCOUNT;
import static com.g2.personalaccount.services.impl.AccountServiceImpl.EMAIL_CORRUPTED_DATA;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
import com.g2.personalaccount.dto.requests.AccountRequest;
import com.g2.personalaccount.dto.requests.AccountUpdateRequest;
import com.g2.personalaccount.dto.responses.AccountResponse;
import com.g2.personalaccount.exceptions.InvalidArgumentsException;
import com.g2.personalaccount.model.Account;
import com.g2.personalaccount.model.enumerated.StatusEnum;
import com.g2.personalaccount.proxy.EmailProxy;
import com.g2.personalaccount.repositories.AccountRepository;
import com.g2.personalaccount.services.AccountService;
import com.g2.personalaccount.utils.AccountTestUtils;
import com.g2.personalaccount.utils.PinGenerator;
import java.util.Optional;
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
      "EXPIRATION_SECONDS=8640",
      "PIN_LENGTH=4"
    })
public class AccountServiceImplTest {

  @MockBean private AccountService accountService;
  @MockBean private AccountRepository accountRepository;
  @Autowired private AccountMapper accountMapper;
  @MockBean private EmailProxy emailProxy;
  private PinGenerator pinGenerator;
  @Autowired private ServiceConfig serviceConfig;

  @Before
  public void setUp() {
    pinGenerator = new PinGenerator();
    accountService =
        new AccountServiceImpl(
            accountMapper, accountRepository, emailProxy, pinGenerator, serviceConfig);
  }

  @Test
  public void create_saveSuccessfully() {

    // given
    AccountRequest request = AccountTestUtils.createAccountRequest();

    Account returnedAccount = AccountTestUtils.createAccount(request);

    when(accountRepository.save(any())).thenReturn(returnedAccount);
    doNothing().when(emailProxy).sendPin(anyString(), anyInt());
    doNothing().when(emailProxy).sendConfirmation(anyString(), anyString(), anyString());

    // when

    AccountResponse response = accountService.create(request);

    // then
    assertNotNull(response);

    ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);

    verify(accountRepository, times(1)).save(accountArgumentCaptor.capture());
    verify(emailProxy, times(1)).sendPin(anyString(), anyInt());

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
  public void create_foundSsnAccount() {

    // given
    AccountRequest request = AccountTestUtils.createAccountRequest();

    Account returnedAccount = AccountTestUtils.createAccount(request);

    // when
    try {
      when(accountRepository.findByAccountHolder_AccountHolderId_Ssn(anyLong()))
          .thenReturn(Optional.of(returnedAccount));
      // when

      AccountResponse response = accountService.create(request);
    } catch (InvalidArgumentsException ex) {
      // then
      assertEquals(String.format(ACCOUNT_WITH_SAME_SSN_EXIST, request.getSsn()), ex.getMessage());
    }
  }

  @Test
  public void create_foundEmailAccount() {

    // given
    AccountRequest request = AccountTestUtils.createAccountRequest();

    Account returnedAccount = AccountTestUtils.createAccount(request);

    // when
    try {
      when(accountRepository.findByAccountHolder_AccountHolderId_Ssn(anyLong()))
          .thenReturn(Optional.empty());

      when(accountRepository.findByAccountHolder_Email(anyString()))
          .thenReturn(Optional.of(returnedAccount));
      // when

      AccountResponse response = accountService.create(request);
    } catch (InvalidArgumentsException ex) {
      // then
      assertEquals(
          String.format(ACCOUNT_WITH_SAME_EMAIL_EXISTS, request.getEmail()), ex.getMessage());
    }
  }

  @Test
  public void updatePersonalData_updateSuccessfully() {

    // given
    AccountUpdateRequest request = AccountTestUtils.createAccountUpdateRequest();

    Account returnedAccount = AccountTestUtils.createUpdateAccount(request);

    Account foundAccount = AccountTestUtils.createUpdateAccount(request);

    when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));

    when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));
    when(accountRepository.findByAccountHolder_Email(anyString()))
        .thenReturn(Optional.of(foundAccount));

    when(accountRepository.save(any())).thenReturn(returnedAccount);

    // when
    AccountResponse response = accountService.updatePersonalData(request);

    // then
    assertNotNull(response);

    ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);

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

    assertNotNull(request.getId());
    assertEquals(returnedAccount.getId(), request.getId());

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
      // when
    } catch (InvalidArgumentsException ex) {
      // then
      assertEquals(String.format(ACCOUNT_NUMBER_DOESNT_EXISTS, request.getId()), ex.getMessage());
    }
  }

  @Test
  public void update_emailDataCorrupted() {

    // given
    AccountUpdateRequest request = AccountTestUtils.createAccountUpdateRequest();
    Account foundAccount = AccountTestUtils.createUpdateAccount(request);

    try {
      // when

      when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));

      when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));
      when(accountRepository.findByAccountHolder_Email(anyString())).thenReturn(Optional.empty());
      AccountResponse response = accountService.updatePersonalData(request);
      // when
    } catch (InvalidArgumentsException ex) {
      // then
      assertEquals(String.format(EMAIL_CORRUPTED_DATA, request.getEmail()), ex.getMessage());
    }
  }

  @Test
  public void update_emailExistsInAnotherAccount() {

    // given
    AccountUpdateRequest request = AccountTestUtils.createAccountUpdateRequest();
    Account foundAccount = AccountTestUtils.createUpdateAccount(request);
    Account emailFoundAccount = AccountTestUtils.createUpdateAccount(request);
    emailFoundAccount.setId(34324123L);

    try {
      // when

      when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));

      when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));
      when(accountRepository.findByAccountHolder_Email(anyString()))
          .thenReturn(Optional.of(emailFoundAccount));
      AccountResponse response = accountService.updatePersonalData(request);
      // when
    } catch (InvalidArgumentsException ex) {
      // then
      assertEquals(
          String.format(EMAIL_ALREADY_EXISTS_IN_ANOTHER_ACCOUNT, request.getEmail()),
          ex.getMessage());
    }
  }
}
