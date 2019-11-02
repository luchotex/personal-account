package com.g2.personalaccount.controllers;

import com.g2.personalaccount.dto.requests.AccountCloseRequest;
import com.g2.personalaccount.dto.requests.AccountRequest;
import com.g2.personalaccount.dto.requests.AccountUpdateRequest;
import com.g2.personalaccount.dto.requests.AuthenticationRequest;
import com.g2.personalaccount.dto.responses.AccountCloseResponse;
import com.g2.personalaccount.dto.responses.AccountResponse;
import com.g2.personalaccount.dto.responses.AuthenticationResponse;
import com.g2.personalaccount.services.AccountService;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-30 17:41
 */
@RestController
@RequestMapping(value = "/accounts")
public class AccountController {

  private AccountService accountService;

  public AccountController(AccountService accountService) {
    this.accountService = accountService;
  }

  @PostMapping
  public ResponseEntity<AccountResponse> createAccount(
      @RequestBody @Valid AccountRequest accountRequest) {
    return new ResponseEntity<>(accountService.create(accountRequest), HttpStatus.OK);
  }

  @PutMapping(value = "/personal-data")
  public ResponseEntity<AccountResponse> updatePersonalData(
      @RequestBody @Valid AccountUpdateRequest updateRequest) {
    return new ResponseEntity<>(accountService.updatePersonalData(updateRequest), HttpStatus.OK);
  }

  @PostMapping("/authenticate")
  public ResponseEntity<AuthenticationResponse> authenticateAccount(
      @RequestBody @Valid AuthenticationRequest authenticationRequest) {
    return new ResponseEntity<>(
        accountService.authenticateAccount(authenticationRequest), HttpStatus.OK);
  }

  @DeleteMapping("/close")
  public ResponseEntity<AccountCloseResponse> closeAccount(
      @RequestBody @Valid AccountCloseRequest request) {
    return new ResponseEntity<>(accountService.closeAccount(request), HttpStatus.OK);
  }
}
