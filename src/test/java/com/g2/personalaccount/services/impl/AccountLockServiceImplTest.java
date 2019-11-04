package com.g2.personalaccount.services.impl;

import static com.g2.personalaccount.services.impl.AccountLockServiceImpl.ACCOUNT_TRANSFERING_LOCKED;
import static com.g2.personalaccount.services.impl.AccountServiceImpl.ACCOUNT_IS_LOCKED;
import static com.g2.personalaccount.services.impl.AccountServiceImpl.ACCOUNT_NUMBER_DOESNT_EXISTS;
import static com.g2.personalaccount.services.impl.AccountServiceImpl.IS_NOT_AUTHENTICATED_TO_PERFORM_THIS_OPERATION;
import static com.g2.personalaccount.services.impl.AccountServiceImpl.THE_ACCOUNT_IS_CLOSED;
import static com.g2.personalaccount.services.impl.AccountServiceImpl.THE_ACCOUNT_NUMBER_IS_ON_CONFIRMATION;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.g2.personalaccount.config.ServiceConfig;
import com.g2.personalaccount.dto.requests.AccountUpdateRequest;
import com.g2.personalaccount.exceptions.InvalidArgumentsException;
import com.g2.personalaccount.exceptions.ResourceNotFoundException;
import com.g2.personalaccount.model.Account;
import com.g2.personalaccount.model.AccountLock;
import com.g2.personalaccount.model.enumerated.StatusEnum;
import com.g2.personalaccount.repositories.AccountLockRepository;
import com.g2.personalaccount.repositories.AccountRepository;
import com.g2.personalaccount.services.AccountLockService;
import com.g2.personalaccount.utils.AccountLockTestUtils;
import com.g2.personalaccount.utils.AccountTestUtils;
import com.g2.personalaccount.validators.EditionValidator;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AccountLockServiceImplTest {

  @Mock private AccountRepository accountRepository;
  @Mock private AccountLockRepository accountLockRepository;
  private EditionValidator editionValidator;
  private ServiceConfig serviceConfig;

  private AccountLockService service;

  @Before
  public void setUp() throws Exception {
    serviceConfig = new ServiceConfig();
    serviceConfig.setLockingRegistriesSeconds("30");

    editionValidator = new EditionValidator(accountRepository, serviceConfig);

    service =
        new AccountLockServiceImpl(
            accountRepository, accountLockRepository, editionValidator, serviceConfig);
  }

  @Test
  public void lockAccount_inexistentLocking() {

    // given
    Long accountNumber = 234235436L;
    String threadNumber = "threadNumber";

    AccountUpdateRequest request = AccountTestUtils.createAccountUpdateRequest();
    Account foundAccount = AccountTestUtils.createUpdateAccount(request);

    foundAccount
        .getAccountAccess()
        .setAuthenticationExpiration(LocalDateTime.now().plusSeconds(100));

    // when
    when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));
    when(accountLockRepository.findByAccount_Id(anyLong())).thenReturn(Optional.empty());

    service.lockAccount(accountNumber, threadNumber);
    // then
    verify(accountRepository, times(1)).findById(anyLong());
    verify(accountLockRepository, times(1)).findByAccount_Id(anyLong());

    ArgumentCaptor<AccountLock> accountArgumentCaptor = ArgumentCaptor.forClass(AccountLock.class);

    verify(accountLockRepository, times(1)).save(accountArgumentCaptor.capture());

    AccountLock savingValue = accountArgumentCaptor.getValue();

    assertNotNull(savingValue.getThreadName());
    assertEquals(threadNumber, savingValue.getThreadName());
    assertNotNull(savingValue.getAccount());
    assertNotNull(savingValue.getExpirationDate());
    assertTrue(savingValue.getExpirationDate().isAfter(LocalDateTime.now()));
  }

  @Test
  public void lockAccount_existentExpiredLocking() {

    // given
    Long accountNumber = 234235436L;
    String threadNumber = "threadName";

    AccountUpdateRequest request = AccountTestUtils.createAccountUpdateRequest();
    Account foundAccount = AccountTestUtils.createUpdateAccount(request);

    foundAccount
        .getAccountAccess()
        .setAuthenticationExpiration(LocalDateTime.now().plusSeconds(100));

    AccountLock accountLockFound =
        AccountLockTestUtils.createAccountLock(foundAccount, LocalDateTime.now().minusSeconds(100));

    // when
    when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));
    when(accountLockRepository.findByAccount_Id(anyLong()))
        .thenReturn(Optional.of(accountLockFound));

    service.lockAccount(accountNumber, threadNumber);
    // then
    verify(accountRepository, times(1)).findById(anyLong());
    verify(accountLockRepository, times(1)).findByAccount_Id(anyLong());

    ArgumentCaptor<AccountLock> accountArgumentCaptor = ArgumentCaptor.forClass(AccountLock.class);

    verify(accountLockRepository, times(1)).save(accountArgumentCaptor.capture());

    AccountLock savingValue = accountArgumentCaptor.getValue();

    assertNotNull(savingValue.getThreadName());
    assertEquals(threadNumber, savingValue.getThreadName());
    assertNotNull(savingValue.getAccount());
    assertNotNull(savingValue.getExpirationDate());
    assertTrue(savingValue.getExpirationDate().isAfter(LocalDateTime.now()));
  }

  @Test
  public void lockAccount_existentLockingAccount() {

    // given
    Long accountNumber = 234235436L;
    String threadNumber = "threadName";

    AccountUpdateRequest request = AccountTestUtils.createAccountUpdateRequest();
    Account foundAccount = AccountTestUtils.createUpdateAccount(request);

    foundAccount
        .getAccountAccess()
        .setAuthenticationExpiration(LocalDateTime.now().plusSeconds(100));

    AccountLock accountLockFound =
        AccountLockTestUtils.createAccountLock(foundAccount, LocalDateTime.now().plusSeconds(100));

    // when
    when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));
    when(accountLockRepository.findByAccount_Id(anyLong()))
        .thenReturn(Optional.of(accountLockFound));

    try {
      service.lockAccount(accountNumber, threadNumber);
    } catch (InvalidArgumentsException ex) {
      // then
      verify(accountRepository, times(1)).findById(anyLong());
      verify(accountLockRepository, times(1)).findByAccount_Id(anyLong());
      assertEquals(String.format(ACCOUNT_TRANSFERING_LOCKED, accountNumber), ex.getMessage());
    }
  }

  @Test
  public void lockAccount_accountNotfound() {

    // given
    Long accountNumber = 234235436L;
    String threadNumber = "threadNumber";

    try {
      // when
      when(accountRepository.findById(anyLong())).thenReturn(Optional.empty());
      service.lockAccount(accountNumber, threadNumber);
    } catch (ResourceNotFoundException ex) {
      // then
      verify(accountRepository, times(1)).findById(anyLong());
      assertEquals(String.format(ACCOUNT_NUMBER_DOESNT_EXISTS, accountNumber), ex.getMessage());
    }
  }

  @Test
  public void lockAccount_accountIsOnConfirm() {

    // given
    Long accountNumber = 234235436L;
    String threadNumber = "threadNumber";

    AccountUpdateRequest request = AccountTestUtils.createAccountUpdateRequest();
    Account foundAccount = AccountTestUtils.createUpdateAccount(request);
    foundAccount.setStatus(StatusEnum.ON_CONFIRM);

    try {
      // when
      when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));

      service.lockAccount(accountNumber, threadNumber);
    } catch (InvalidArgumentsException ex) {
      // then
      verify(accountRepository, times(1)).findById(anyLong());
      assertEquals(
          String.format(THE_ACCOUNT_NUMBER_IS_ON_CONFIRMATION, accountNumber), ex.getMessage());
    }
  }

  @Test
  public void lockAccount_accountIsClosed() {

    // given
    Long accountNumber = 234235436L;
    String threadNumber = "threadNumber";

    AccountUpdateRequest request = AccountTestUtils.createAccountUpdateRequest();
    Account foundAccount = AccountTestUtils.createUpdateAccount(request);
    foundAccount.setStatus(StatusEnum.INACTIVE);

    try {
      // when
      when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));

      service.lockAccount(accountNumber, threadNumber);
    } catch (InvalidArgumentsException ex) {
      // then
      verify(accountRepository, times(1)).findById(anyLong());
      assertEquals(String.format(THE_ACCOUNT_IS_CLOSED, accountNumber), ex.getMessage());
    }
  }

  @Test
  public void lockAccount_notYetAuthenticated() {

    // given
    Long accountNumber = 234235436L;
    String threadNumber = "threadNumber";

    AccountUpdateRequest request = AccountTestUtils.createAccountUpdateRequest();
    Account foundAccount = AccountTestUtils.createUpdateAccount(request);
    try {
      // when

      when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));
      service.lockAccount(accountNumber, threadNumber);
    } catch (InvalidArgumentsException ex) {
      // then
      verify(accountRepository, times(1)).findById(anyLong());
      assertEquals(IS_NOT_AUTHENTICATED_TO_PERFORM_THIS_OPERATION, ex.getMessage());
    }
  }

  @Test
  public void lockAccount_accountLocked() {

    // given
    Long accountNumber = 234235436L;
    String threadNumber = "threadNumber";

    AccountUpdateRequest request = AccountTestUtils.createAccountUpdateRequest();
    Account foundAccount = AccountTestUtils.createUpdateAccount(request);

    foundAccount.getAccountAccess().setAuthenticationLocking(LocalDateTime.now().plusSeconds(100));

    try {
      // when

      when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));

      service.lockAccount(accountNumber, threadNumber);
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
  public void lockAccount_authenticationExpired() {

    // given
    Long accountNumber = 234235436L;
    String threadNumber = "threadNumber";

    AccountUpdateRequest request = AccountTestUtils.createAccountUpdateRequest();
    Account foundAccount = AccountTestUtils.createUpdateAccount(request);

    foundAccount
        .getAccountAccess()
        .setAuthenticationExpiration(LocalDateTime.now().minusSeconds(100));

    try {
      // when

      when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));
      service.lockAccount(accountNumber, threadNumber);
    } catch (InvalidArgumentsException ex) {
      // then
      verify(accountRepository, times(1)).findById(anyLong());
      assertEquals(IS_NOT_AUTHENTICATED_TO_PERFORM_THIS_OPERATION, ex.getMessage());
    }
  }
}
