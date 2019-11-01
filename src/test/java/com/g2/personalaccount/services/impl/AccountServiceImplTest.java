package com.g2.personalaccount.services.impl;

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
      "MAIL_PORT=587"
    })
public class AccountServiceImplTest {

  @MockBean private AccountService accountService;
  @MockBean private AccountRepository accountRepository;
  @Autowired private AccountMapper accountMapper;
  @MockBean private EmailProxy emailProxy;
  private PinGenerator pinGenerator;
  private ServiceConfig serviceConfig;

  @Before
  public void setUp() {
    pinGenerator = new PinGenerator();
    serviceConfig = new ServiceConfig();
    serviceConfig.setHostname("hostname");
    serviceConfig.setExpirationSeconds("1000");
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
  public void updatePersonalData_updateSuccessfully() {

    // given
    AccountUpdateRequest request = AccountTestUtils.createAccountUpdateRequest();

    Account returnedAccount = AccountTestUtils.createUpdateAccount(request);

    Account foundAccount = AccountTestUtils.createUpdateAccount(request);

    when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));

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
}
