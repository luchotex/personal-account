package com.g2.personalaccount.transaction;

import static com.g2.personalaccount.services.impl.AccountServiceImpl.THE_ACCOUNT_IS_CLOSED;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.g2.personalaccount.dto.requests.AuthenticationRequest;
import com.g2.personalaccount.dto.responses.AuthenticationResponse;
import com.g2.personalaccount.exceptions.InvalidArgumentsException;
import com.g2.personalaccount.model.Account;
import com.g2.personalaccount.model.AccountConfirmation;
import com.g2.personalaccount.model.Transaction;
import com.g2.personalaccount.model.enumerated.TransactionStatusEnum;
import com.g2.personalaccount.model.enumerated.TypeEnum;
import com.g2.personalaccount.repositories.AccountConfirmationRepository;
import com.g2.personalaccount.repositories.AccountRepository;
import com.g2.personalaccount.repositories.TransactionRepository;
import com.g2.personalaccount.utils.AccountConfirmationTestUtils;
import com.g2.personalaccount.utils.AccountTestUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Optional;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransactionAspectTest {

  @Mock private TransactionRepository transactionRepository;
  @Mock private AccountRepository accountRepository;
  @Mock private AccountConfirmationRepository accountConfirmationRepository;

  private TransactionAspect transactionAspect;

  @Before
  public void setUp() throws Exception {
    transactionAspect =
        new TransactionAspect(
            transactionRepository, accountRepository, accountConfirmationRepository);
  }

  @TransactionLogging(TypeEnum.AUTHENTICATE)
  public void testCreationMethod() {}

  @TransactionLogging(TypeEnum.CONFIRMATION_CREATION)
  public void testConfirmationMethod() {}

  @Test
  public void logExecutionTime_inExistentAccount() throws Throwable {

    // given
    Method m = this.getClass().getMethod("testCreationMethod");
    Annotation[] annos = m.getAnnotations();

    Annotation annotation = annos[0];
    TransactionLogging transactionLogging = (TransactionLogging) annotation;
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

    AuthenticationRequest request = new AuthenticationRequest();
    request.setAccountNumber(1233252351L);
    request.setPin("234235432asds13sdf");

    Transaction transaction = new Transaction();
    transaction.setId(123324L);

    AuthenticationResponse response = new AuthenticationResponse();
    response.setExpirationDateTime(LocalDateTime.now().plusSeconds(123));

    // when
    when(joinPoint.getArgs()).thenReturn(new AuthenticationRequest[] {request});
    when(joinPoint.proceed()).thenReturn(response);
    when(accountRepository.findById(anyLong())).thenReturn(Optional.empty());
    when(transactionRepository.save(any())).thenReturn(transaction);
    try {
      Object object = transactionAspect.logExecutionTime(joinPoint, transactionLogging);
    } catch (NoSuchMethodException ex) {
      // then
      // Defined in this way because there is some problems with method reflection, the
      // transactionId defining is not testeable
      ArgumentCaptor<Transaction> transactionArgumentCaptor =
          ArgumentCaptor.forClass(Transaction.class);
      verify(accountRepository, times(1)).findById(anyLong());
      verify(transactionRepository, times(1)).save(transactionArgumentCaptor.capture());

      Transaction savedValue = transactionArgumentCaptor.getValue();

      assertEquals(TransactionStatusEnum.CORRECT, savedValue.getStatus());
      assertNull(savedValue.getAccount());
      assertNull(savedValue.getAmount());
      assertNull(savedValue.getDescription());
      assertNull(savedValue.getCreateDateTime());
      assertNull(savedValue.getUpdateDateTime());
      assertNull(savedValue.getErrorMessage());
      assertNotNull(savedValue.getElapsedSeconds());
      assertEquals(TypeEnum.AUTHENTICATE, savedValue.getType());
    }
  }

  @Test
  public void logExecutionTime_existentAccount() throws Throwable {

    // given
    Method m = this.getClass().getMethod("testCreationMethod");
    Annotation[] annos = m.getAnnotations();

    Annotation annotation = annos[0];
    TransactionLogging transactionLogging = (TransactionLogging) annotation;
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

    AuthenticationRequest request = new AuthenticationRequest();
    request.setAccountNumber(1233252351L);
    request.setPin("234235432asds13sdf");

    Account account =
        AccountTestUtils.createUpdateAccount(AccountTestUtils.createAccountUpdateRequest());

    Transaction transaction = new Transaction();
    transaction.setId(123324L);

    AuthenticationResponse response = new AuthenticationResponse();
    response.setExpirationDateTime(LocalDateTime.now().plusSeconds(123));

    // when
    when(joinPoint.getArgs()).thenReturn(new AuthenticationRequest[] {request});
    when(joinPoint.proceed()).thenReturn(response);
    when(accountRepository.findById(anyLong())).thenReturn(Optional.of(account));
    when(transactionRepository.save(any())).thenReturn(transaction);
    try {
      Object object = transactionAspect.logExecutionTime(joinPoint, transactionLogging);
    } catch (NoSuchMethodException ex) {
      // then
      // Defined in this way because there is some problems with method reflection, the
      // transactionId defining is not testeable
      ArgumentCaptor<Transaction> transactionArgumentCaptor =
          ArgumentCaptor.forClass(Transaction.class);
      verify(accountRepository, times(1)).findById(anyLong());
      verify(transactionRepository, times(1)).save(transactionArgumentCaptor.capture());

      Transaction savedValue = transactionArgumentCaptor.getValue();

      assertEquals(TransactionStatusEnum.CORRECT, savedValue.getStatus());
      assertNotNull(savedValue.getAccount());
      assertNull(savedValue.getAmount());
      assertNull(savedValue.getDescription());
      assertNull(savedValue.getCreateDateTime());
      assertNull(savedValue.getUpdateDateTime());
      assertNull(savedValue.getErrorMessage());
      assertNotNull(savedValue.getElapsedSeconds());
      assertEquals(TypeEnum.AUTHENTICATE, savedValue.getType());
    }
  }

  @Test
  public void logExecutionTime_inExistentAccountButConfirmation() throws Throwable {

    // given
    Method m = this.getClass().getMethod("testConfirmationMethod");
    Annotation[] annos = m.getAnnotations();

    Annotation annotation = annos[0];
    TransactionLogging transactionLogging = (TransactionLogging) annotation;
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

    AccountConfirmation accountConfirmation =
        AccountConfirmationTestUtils.createAccountConfirmation();

    Transaction transaction = new Transaction();
    transaction.setId(123324L);

    // when
    when(joinPoint.getArgs()).thenReturn(new Object[] {"343gsdr24325132fsdf12312sg3423dg2342"});
    when(joinPoint.proceed()).thenReturn("response");
    when(accountConfirmationRepository.findByConfirmationId(anyString()))
        .thenReturn(Optional.of(accountConfirmation));
    when(transactionRepository.save(any())).thenReturn(transaction);
    try {
      Object object = transactionAspect.logExecutionTime(joinPoint, transactionLogging);
    } catch (NoSuchMethodException ex) {
      // then
      // Defined in this way because there is some problems with method reflection, the
      // transactionId defining is not testeable
      ArgumentCaptor<Transaction> transactionArgumentCaptor =
          ArgumentCaptor.forClass(Transaction.class);
      verify(accountConfirmationRepository, times(1)).findByConfirmationId(anyString());
      verify(transactionRepository, times(1)).save(transactionArgumentCaptor.capture());

      Transaction savedValue = transactionArgumentCaptor.getValue();

      assertEquals(TransactionStatusEnum.CORRECT, savedValue.getStatus());
      assertNotNull(savedValue.getAccount());
      assertNull(savedValue.getAmount());
      assertNull(savedValue.getDescription());
      assertNull(savedValue.getCreateDateTime());
      assertNull(savedValue.getUpdateDateTime());
      assertNull(savedValue.getErrorMessage());
      assertNotNull(savedValue.getElapsedSeconds());
      assertEquals(TypeEnum.CONFIRMATION_CREATION, savedValue.getType());
    }
  }

  @Test
  public void logExecutionTime_throwingException() throws Throwable {

    // given
    Method m = this.getClass().getMethod("testCreationMethod");
    Annotation[] annos = m.getAnnotations();

    Annotation annotation = annos[0];
    TransactionLogging transactionLogging = (TransactionLogging) annotation;
    ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

    AuthenticationRequest request = new AuthenticationRequest();
    request.setAccountNumber(1233252351L);
    request.setPin("234235432asds13sdf");

    Account account =
        AccountTestUtils.createUpdateAccount(AccountTestUtils.createAccountUpdateRequest());

    Transaction transaction = new Transaction();
    transaction.setId(123324L);

    InvalidArgumentsException exception =
        new InvalidArgumentsException(
            String.format(THE_ACCOUNT_IS_CLOSED, request.getAccountNumber()));

    // when
    when(joinPoint.getArgs()).thenReturn(new AuthenticationRequest[] {request});
    when(joinPoint.proceed()).thenThrow(exception);
    when(accountRepository.findById(anyLong())).thenReturn(Optional.of(account));
    when(transactionRepository.save(any())).thenReturn(transaction);
    try {
      Object object = transactionAspect.logExecutionTime(joinPoint, transactionLogging);
    } catch (InvalidArgumentsException ex) {
      // then
      // Defined in this way because there is some problems with method reflection, the
      // transactionId defining is not testeable
      ArgumentCaptor<Transaction> transactionArgumentCaptor =
          ArgumentCaptor.forClass(Transaction.class);
      verify(accountRepository, times(1)).findById(anyLong());
      verify(transactionRepository, times(1)).save(transactionArgumentCaptor.capture());

      Transaction savedValue = transactionArgumentCaptor.getValue();

      assertEquals(TransactionStatusEnum.ERROR, savedValue.getStatus());
      assertNotNull(savedValue.getAccount());
      assertNull(savedValue.getAmount());
      assertNull(savedValue.getDescription());
      assertNull(savedValue.getCreateDateTime());
      assertNull(savedValue.getUpdateDateTime());
      assertNotNull(savedValue.getErrorMessage());
      assertEquals(
          String.format(THE_ACCOUNT_IS_CLOSED, request.getAccountNumber()),
          savedValue.getErrorMessage());
      assertNotNull(savedValue.getElapsedSeconds());
      assertEquals(TypeEnum.AUTHENTICATE, savedValue.getType());
    }
  }
}
