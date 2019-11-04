package com.g2.personalaccount.services.impl;

import static com.g2.personalaccount.services.impl.AccountServiceImpl.ACCOUNT_IS_LOCKED;
import static com.g2.personalaccount.services.impl.AccountServiceImpl.ACCOUNT_NUMBER_DOESNT_EXISTS;
import static com.g2.personalaccount.services.impl.AccountServiceImpl.IS_NOT_AUTHENTICATED_TO_PERFORM_THIS_OPERATION;
import static com.g2.personalaccount.services.impl.AccountServiceImpl.THE_ACCOUNT_IS_CLOSED;
import static com.g2.personalaccount.services.impl.AccountServiceImpl.THE_ACCOUNT_NUMBER_IS_ON_CONFIRMATION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.g2.personalaccount.config.ServiceConfig;
import com.g2.personalaccount.dto.mappers.TransactionMapper;
import com.g2.personalaccount.dto.requests.AccountUpdateRequest;
import com.g2.personalaccount.dto.responses.AccountLastTransactionsResponse;
import com.g2.personalaccount.dto.responses.TransactionResponse;
import com.g2.personalaccount.exceptions.InvalidArgumentsException;
import com.g2.personalaccount.exceptions.ResourceNotFoundException;
import com.g2.personalaccount.model.Account;
import com.g2.personalaccount.model.Transaction;
import com.g2.personalaccount.model.enumerated.StatusEnum;
import com.g2.personalaccount.model.enumerated.TransactionStatusEnum;
import com.g2.personalaccount.model.enumerated.TypeEnum;
import com.g2.personalaccount.repositories.AccountRepository;
import com.g2.personalaccount.repositories.TransactionRepository;
import com.g2.personalaccount.services.TransactionService;
import com.g2.personalaccount.utils.AccountTestUtils;
import com.g2.personalaccount.validators.EditionValidator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransactionServiceImplTest {

  @Mock private AccountRepository accountRepository;
  @Mock private TransactionRepository transactionRepository;
  private TransactionMapper transactionMapper = Mappers.getMapper(TransactionMapper.class);
  private EditionValidator editionValidator;

  private TransactionService service;

  @Before
  public void setUp() throws Exception {

    editionValidator = new EditionValidator(accountRepository, new ServiceConfig());
    service =
        new TransactionServiceImpl(
            accountRepository, transactionRepository, transactionMapper, editionValidator);
  }

  @Test
  public void retrieveLastTransactions_successfulEmptyResults() {

    // given
    Long accountNumber = 234235436L;

    AccountUpdateRequest request = AccountTestUtils.createAccountUpdateRequest();
    Account foundAccount = AccountTestUtils.createUpdateAccount(request);

    foundAccount
        .getAccountAccess()
        .setAuthenticationExpiration(LocalDateTime.now().plusSeconds(100));
    // when

    when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));
    when(transactionRepository.findByAccount_IdAndStatusAndTypeIn(
            anyLong(), any(), anyList(), any()))
        .thenReturn(new ArrayList<>());
    AccountLastTransactionsResponse response = service.retrieveLastTransactions(accountNumber);
    assertNotNull(response);
    assertTrue(response.getTransactionResponses().isEmpty());
    // then
    verify(accountRepository, times(1)).findById(anyLong());
    verify(transactionRepository, times(1))
        .findByAccount_IdAndStatusAndTypeIn(anyLong(), any(), anyList(), any());
  }

  @Test
  public void retrieveLastTransactions_successfulNotEmptyResults() {

    // given
    Long accountNumber = 234235436L;

    AccountUpdateRequest request = AccountTestUtils.createAccountUpdateRequest();
    Account foundAccount = AccountTestUtils.createUpdateAccount(request);

    foundAccount
        .getAccountAccess()
        .setAuthenticationExpiration(LocalDateTime.now().plusSeconds(100));

    Transaction transaction = new Transaction();
    transaction.setId(3241124324531L);
    transaction.setCreateDateTime(LocalDateTime.now().minusSeconds(100));
    transaction.setType(TypeEnum.CHECKS);
    transaction.setStatus(TransactionStatusEnum.CORRECT);
    transaction.setAmount(new BigDecimal(10));
    transaction.setDescription("Gasoline");

    Transaction secondTransaction = new Transaction();
    transaction.setId(324143524324531L);
    secondTransaction.setCreateDateTime(LocalDateTime.now().minusSeconds(200));
    secondTransaction.setType(TypeEnum.DEBIT);
    secondTransaction.setStatus(TransactionStatusEnum.CORRECT);
    secondTransaction.setAmount(new BigDecimal(100));
    secondTransaction.setDescription("Shop Purchase");

    // when

    when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));
    when(transactionRepository.findByAccount_IdAndStatusAndTypeIn(
            anyLong(), any(), anyList(), any()))
        .thenReturn(Arrays.asList(transaction, secondTransaction));
    AccountLastTransactionsResponse response = service.retrieveLastTransactions(accountNumber);
    assertNotNull(response);
    assertTrue(!response.getTransactionResponses().isEmpty());

    TransactionResponse firstResponse = response.getTransactionResponses().get(0);

    assertEquals(transaction.getAmount(), firstResponse.getAmount());
    assertEquals(transaction.getDescription(), firstResponse.getDescription());
    assertEquals(transaction.getCreateDateTime(), firstResponse.getLocalDateTime());
    assertEquals(transaction.getId(), firstResponse.getTransactionId());
    assertEquals(transaction.getType(), firstResponse.getType());

    // then
    verify(accountRepository, times(1)).findById(anyLong());
    verify(transactionRepository, times(1))
        .findByAccount_IdAndStatusAndTypeIn(anyLong(), any(), anyList(), any());
  }

  @Test
  public void retrieveLastTransactions_accountNotfound() {

    // given
    Long accountNumber = 234235436L;

    try {
      // when
      when(accountRepository.findById(anyLong())).thenReturn(Optional.empty());
      AccountLastTransactionsResponse response = service.retrieveLastTransactions(accountNumber);
      // when
    } catch (ResourceNotFoundException ex) {
      // then
      verify(accountRepository, times(1)).findById(anyLong());
      assertEquals(String.format(ACCOUNT_NUMBER_DOESNT_EXISTS, accountNumber), ex.getMessage());
    }
  }

  @Test
  public void retrieveLastTransactions_accountIsOnConfirm() {

    // given
    Long accountNumber = 234235436L;

    AccountUpdateRequest request = AccountTestUtils.createAccountUpdateRequest();
    Account foundAccount = AccountTestUtils.createUpdateAccount(request);
    foundAccount.setStatus(StatusEnum.ON_CONFIRM);

    try {
      // when
      when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));

      AccountLastTransactionsResponse response = service.retrieveLastTransactions(accountNumber);
    } catch (InvalidArgumentsException ex) {
      // then
      verify(accountRepository, times(1)).findById(anyLong());
      assertEquals(
          String.format(THE_ACCOUNT_NUMBER_IS_ON_CONFIRMATION, accountNumber), ex.getMessage());
    }
  }

  @Test
  public void retrieveLastTransactions_accountIsClosed() {

    // given
    Long accountNumber = 234235436L;

    AccountUpdateRequest request = AccountTestUtils.createAccountUpdateRequest();
    Account foundAccount = AccountTestUtils.createUpdateAccount(request);
    foundAccount.setStatus(StatusEnum.INACTIVE);

    try {
      // when
      when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));

      AccountLastTransactionsResponse response = service.retrieveLastTransactions(accountNumber);
      // when
    } catch (InvalidArgumentsException ex) {
      // then
      verify(accountRepository, times(1)).findById(anyLong());
      assertEquals(String.format(THE_ACCOUNT_IS_CLOSED, accountNumber), ex.getMessage());
    }
  }

  @Test
  public void retrieveLastTransactions_notYetAuthenticated() {

    // given
    Long accountNumber = 234235436L;

    AccountUpdateRequest request = AccountTestUtils.createAccountUpdateRequest();
    Account foundAccount = AccountTestUtils.createUpdateAccount(request);
    try {
      // when

      when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));
      AccountLastTransactionsResponse response = service.retrieveLastTransactions(accountNumber);
      // when
    } catch (InvalidArgumentsException ex) {
      // then
      verify(accountRepository, times(1)).findById(anyLong());
      assertEquals(IS_NOT_AUTHENTICATED_TO_PERFORM_THIS_OPERATION, ex.getMessage());
    }
  }

  @Test
  public void retrieveLastTransactions_accountLocked() {

    // given
    Long accountNumber = 234235436L;

    AccountUpdateRequest request = AccountTestUtils.createAccountUpdateRequest();
    Account foundAccount = AccountTestUtils.createUpdateAccount(request);

    foundAccount.getAccountAccess().setAuthenticationLocking(LocalDateTime.now().plusSeconds(100));

    try {
      // when

      when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));

      AccountLastTransactionsResponse response = service.retrieveLastTransactions(accountNumber);
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
  public void retrieveLastTransactions_authenticationExpired() {

    // given
    Long accountNumber = 234235436L;

    AccountUpdateRequest request = AccountTestUtils.createAccountUpdateRequest();
    Account foundAccount = AccountTestUtils.createUpdateAccount(request);

    foundAccount
        .getAccountAccess()
        .setAuthenticationExpiration(LocalDateTime.now().minusSeconds(100));

    try {
      // when

      when(accountRepository.findById(anyLong())).thenReturn(Optional.of(foundAccount));
      AccountLastTransactionsResponse response = service.retrieveLastTransactions(accountNumber);
      // when
    } catch (InvalidArgumentsException ex) {
      // then
      verify(accountRepository, times(1)).findById(anyLong());
      assertEquals(IS_NOT_AUTHENTICATED_TO_PERFORM_THIS_OPERATION, ex.getMessage());
    }
  }
}
