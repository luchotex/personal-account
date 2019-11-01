package com.g2.personalaccount.controllers;

import com.g2.personalaccount.dto.requests.AccountRequest;
import com.g2.personalaccount.dto.requests.AccountUpdateRequest;
import com.g2.personalaccount.dto.responses.AccountResponse;
import com.g2.personalaccount.services.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
  public ResponseEntity<AccountResponse> createAccount(@RequestBody AccountRequest accountRequest) {
    return new ResponseEntity<>(accountService.create(accountRequest), HttpStatus.OK);
  }

  @PutMapping(value = "/personal-data")
  public ResponseEntity<AccountResponse> updatePersonalData(
      @RequestBody AccountUpdateRequest updateRequest) {
    return new ResponseEntity<>(accountService.updatePersonalData(updateRequest), HttpStatus.OK);
  }
}
