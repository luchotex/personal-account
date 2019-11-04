package com.g2.personalaccount.transaction;

import com.g2.personalaccount.model.Account;
import com.g2.personalaccount.model.AccountConfirmation;
import com.g2.personalaccount.model.Transaction;
import com.g2.personalaccount.model.enumerated.TransactionStatusEnum;
import com.g2.personalaccount.repositories.AccountConfirmationRepository;
import com.g2.personalaccount.repositories.AccountRepository;
import com.g2.personalaccount.repositories.TransactionRepository;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-11-03 08:55
 */
@Aspect
@Component
public class TransactionAspect {

  public static final String ACCOUNT_NUMBER_FIELD_NAME = "accountNumber";
  public static final int UUID_LENGTH = 36;
  public static final String SET_TRANSACTION_ID_METHOD_NAME = "setTransactionId";
  public static final String TRANSACTION_ID_FIELD_NAME = "transactionId";

  private TransactionRepository transactionRepository;
  private AccountRepository accountRepository;
  private AccountConfirmationRepository accountConfirmationRepository;

  public TransactionAspect(
      TransactionRepository transactionRepository,
      AccountRepository accountRepository,
      AccountConfirmationRepository accountConfirmationRepository) {
    this.transactionRepository = transactionRepository;
    this.accountRepository = accountRepository;
    this.accountConfirmationRepository = accountConfirmationRepository;
  }

  @Around("@annotation(transactionLogging)")
  public Object logExecutionTime(
      ProceedingJoinPoint joinPoint, TransactionLogging transactionLogging) throws Throwable {
    final long start = System.currentTimeMillis();
    Object proceed = null;
    TransactionStatusEnum status = TransactionStatusEnum.CORRECT;
    String message = null;
    Exception savedException = new Exception();
    try {
      proceed = joinPoint.proceed();
    } catch (Exception exception) {
      savedException = exception;
      message = exception.getMessage();
      status = TransactionStatusEnum.ERROR;
      throw exception;
    } finally {
      final long executionTime = System.currentTimeMillis() - start;

      Transaction transaction = new Transaction();
      transaction.setType(transactionLogging.value());
      transaction.setElapsedSeconds((int) executionTime);
      transaction.setErrorMessage(message);
      transaction.setStatus(status);

      Account foundAccount = retrieveAccount(joinPoint, proceed);

      transaction.setAccount(foundAccount);

      transaction = transactionRepository.save(transaction);

      setTransactionId(proceed, savedException, transaction);
    }

    return proceed;
  }

  private Account retrieveAccount(ProceedingJoinPoint joinPoint, Object proceed)
      throws IllegalAccessException {
    Account foundAccount = null;
    Object request = joinPoint.getArgs()[0];

    Long accountNumber = getAccountNumber(request);

    Optional<Account> optionalAccount = Optional.empty();

    if (Objects.isNull(accountNumber)) {
      accountNumber = getAccountNumber(proceed);
    }

    if (Objects.isNull(accountNumber) && Objects.nonNull(request) && request instanceof Long) {
      accountNumber = (Long) request;
    }

    optionalAccount = retrieveOptionalAccount(accountNumber, optionalAccount);

    if (optionalAccount.isPresent()) {
      foundAccount = optionalAccount.get();
    }

    foundAccount = retrieveAccountFromConfirmation(foundAccount, request);

    return foundAccount;
  }

  private Optional<Account> retrieveOptionalAccount(
      Long accountNumber, Optional<Account> optionalAccount) {
    if (Objects.nonNull(accountNumber)) {
      optionalAccount = accountRepository.findById(accountNumber);
    }
    return optionalAccount;
  }

  private Account retrieveAccountFromConfirmation(Account foundAccount, Object request) {
    if (Objects.nonNull(request) && request instanceof String) {
      String confirmationId = (String) request;
      if (confirmationId.length() == UUID_LENGTH) {
        Optional<AccountConfirmation> accountConfirmationOptional =
            accountConfirmationRepository.findByConfirmationId(confirmationId);
        if (accountConfirmationOptional.isPresent()) {
          foundAccount = accountConfirmationOptional.get().getAccount();
        }
      }
    }
    return foundAccount;
  }

  private Long getAccountNumber(Object object) throws IllegalAccessException {
    if (Objects.isNull(object)) {
      return null;
    }

    Long accountNumber = null;
    Class<?> objClass = object.getClass();
    Field[] fields = objClass.getDeclaredFields();

    for (Field field : fields) {
      field.setAccessible(true);
      if (field.getName().equals(ACCOUNT_NUMBER_FIELD_NAME)) {
        accountNumber = (Long) field.get(object);
        break;
      }
      field.setAccessible(false);
    }
    return accountNumber;
  }

  private void setTransactionId(Object proceed, Exception savedException, Transaction transaction)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    Method setTransactionIdMethod;
    if (validateFieldExistence(proceed, TRANSACTION_ID_FIELD_NAME)) {
      if (Objects.nonNull(proceed)) {
        setTransactionIdMethod =
            proceed.getClass().getMethod(SET_TRANSACTION_ID_METHOD_NAME, Long.class);
        setTransactionIdMethod.invoke(proceed, transaction.getId());
      } else {
        setTransactionIdMethod =
            savedException.getClass().getMethod(SET_TRANSACTION_ID_METHOD_NAME, Long.class);
        setTransactionIdMethod.invoke(savedException, transaction.getId());
      }
    }
  }

  private boolean validateFieldExistence(Object object, String fieldName) {
    boolean exist = false;
    if (Objects.isNull(object)) {
      return false;
    }

    Class<?> objClass = object.getClass();
    Field[] fields = objClass.getDeclaredFields();

    for (Field field : fields) {
      field.setAccessible(true);
      if (field.getName().equals(fieldName)) {
        exist = true;
        break;
      }
      field.setAccessible(false);
    }
    return exist;
  }
}
