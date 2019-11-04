package com.g2.personalaccount.controllers;

import com.g2.personalaccount.dto.requests.AccountCloseRequest;
import com.g2.personalaccount.dto.requests.AccountRequest;
import com.g2.personalaccount.dto.requests.AccountUpdateRequest;
import com.g2.personalaccount.dto.requests.AuthenticationRequest;
import com.g2.personalaccount.dto.requests.ExternalMoneyMovementRequest;
import com.g2.personalaccount.dto.requests.MoneyMovementRequest;
import com.g2.personalaccount.dto.responses.AccountCloseResponse;
import com.g2.personalaccount.dto.responses.AccountLastTransactionsResponse;
import com.g2.personalaccount.dto.responses.AccountResponse;
import com.g2.personalaccount.dto.responses.AuthenticationResponse;
import com.g2.personalaccount.dto.responses.CurrentBalanceResponse;
import com.g2.personalaccount.dto.responses.MoneyMovementResponse;
import com.g2.personalaccount.services.AccountService;
import com.g2.personalaccount.services.BalanceService;
import com.g2.personalaccount.services.TransactionService;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-30 17:41
 */
@RestController
public class AccountController implements AccountApi {

  private AccountService accountService;
  private TransactionService transactionService;
  private BalanceService balanceService;

  public AccountController(
      AccountService accountService,
      TransactionService transactionService,
      BalanceService balanceService) {
    this.accountService = accountService;
    this.transactionService = transactionService;
    this.balanceService = balanceService;
  }

  public ResponseEntity<AccountResponse> createAccount(
      @RequestBody @Valid AccountRequest accountRequest) {
    return new ResponseEntity<>(accountService.create(accountRequest), HttpStatus.OK);
  }

  public ResponseEntity<AccountResponse> updatePersonalData(
      @RequestBody @Valid AccountUpdateRequest updateRequest) {
    return new ResponseEntity<>(accountService.updatePersonalData(updateRequest), HttpStatus.OK);
  }

  public ResponseEntity<AuthenticationResponse> authenticateAccount(
      @RequestBody @Valid AuthenticationRequest authenticationRequest) {
    return new ResponseEntity<>(
        accountService.authenticateAccount(authenticationRequest), HttpStatus.OK);
  }

  public ResponseEntity<AccountCloseResponse> close(
      @RequestBody @Valid AccountCloseRequest request) {
    return new ResponseEntity<>(accountService.close(request), HttpStatus.OK);
  }

  public ResponseEntity<AccountLastTransactionsResponse> lastTransactions(
      @PathVariable("accountNumber") @NotNull Long accountNumber) {
    return new ResponseEntity<>(
        transactionService.retrieveLastTransactions(accountNumber), HttpStatus.OK);
  }

  public ResponseEntity<MoneyMovementResponse> deposit(
      @RequestBody @Valid MoneyMovementRequest request) {
    return new ResponseEntity<>(accountService.deposit(request), HttpStatus.OK);
  }

  public ResponseEntity<MoneyMovementResponse> withdrawal(
      @RequestBody @Valid MoneyMovementRequest request) {
    return new ResponseEntity<>(accountService.withDrawal(request), HttpStatus.OK);
  }

  public ResponseEntity<MoneyMovementResponse> checkCharge(
      @RequestBody @Valid ExternalMoneyMovementRequest request) {
    return new ResponseEntity<>(accountService.checkCharge(request), HttpStatus.OK);
  }

  public ResponseEntity<MoneyMovementResponse> debit(
      @RequestBody @Valid ExternalMoneyMovementRequest request) {
    return new ResponseEntity<>(accountService.debit(request), HttpStatus.OK);
  }

  public ResponseEntity<CurrentBalanceResponse> currentBalance(
      @PathVariable("accountNumber") @NotNull Long accountNumber) {
    return new ResponseEntity<>(balanceService.retrieveTotalBalance(accountNumber), HttpStatus.OK);
  }
}
