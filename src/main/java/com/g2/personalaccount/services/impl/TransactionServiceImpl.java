package com.g2.personalaccount.services.impl;

import static com.g2.personalaccount.services.impl.AccountServiceImpl.THE_ACCOUNT_IS_CLOSED;

import com.g2.personalaccount.dto.mappers.TransactionMapper;
import com.g2.personalaccount.dto.responses.AccountLastTransactionsResponse;
import com.g2.personalaccount.model.Account;
import com.g2.personalaccount.model.Transaction;
import com.g2.personalaccount.model.enumerated.TransactionStatusEnum;
import com.g2.personalaccount.model.enumerated.TypeEnum;
import com.g2.personalaccount.repositories.AccountRepository;
import com.g2.personalaccount.repositories.TransactionRepository;
import com.g2.personalaccount.services.TransactionService;
import com.g2.personalaccount.transaction.TransactionLogging;
import com.g2.personalaccount.validators.EditionValidator;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-31 09:41
 */
@Service
public class TransactionServiceImpl implements TransactionService {

  private AccountRepository accountRepository;
  private TransactionRepository transactionRepository;
  private TransactionMapper transactionMapper;
  private EditionValidator editionValidator;

  public TransactionServiceImpl(
      AccountRepository accountRepository,
      TransactionRepository transactionRepository,
      TransactionMapper transactionMapper,
      EditionValidator editionValidator) {
    this.accountRepository = accountRepository;
    this.transactionRepository = transactionRepository;
    this.transactionMapper = transactionMapper;
    this.editionValidator = editionValidator;
  }

  @Override
  @TransactionLogging(TypeEnum.LAST_TRANSACTIONS)
  public AccountLastTransactionsResponse retrieveLastTransactions(Long accountNumber) {

    Optional<Account> foundAccountOptional = accountRepository.findById(accountNumber);

    editionValidator.validateAccount(foundAccountOptional, accountNumber, THE_ACCOUNT_IS_CLOSED);

    Pageable pageable = PageRequest.of(0, 4, Sort.by(Sort.Direction.ASC, "createDateTime"));

    List<Transaction> lastTransactions =
        transactionRepository.findByAccount_IdAndStatusAndTypeIn(
            accountNumber,
            TransactionStatusEnum.CORRECT,
            Arrays.asList(TypeEnum.CHECKS, TypeEnum.DEBIT, TypeEnum.DEPOSIT, TypeEnum.WITHDRAWAL),
            pageable);

    AccountLastTransactionsResponse response = new AccountLastTransactionsResponse();
    response.setTransactionResponses(transactionMapper.toList(lastTransactions));

    return response;
  }
}
