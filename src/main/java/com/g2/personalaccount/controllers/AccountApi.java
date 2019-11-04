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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-11-04 14:07
 */
@Api(value = "/account")
@RequestMapping(value = "/accounts")
public interface AccountApi {

  @PostMapping
  @ApiOperation(value = "Create an bank account", response = AccountResponse.class)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 400,
            message =
                "- First name cannot be null (must have a value) or empty. \n"
                    + "- Last name cannot be null (must have a value) or empty. \n"
                    + "- Email cannot be null (must have a value) or empty or must have a valid email format. \n"
                    + "- ssn cannot be null and must have 9 length value and positive integer. \n"
                    + "- Voter card Id cannot be null and positive integer. \n"),
        @ApiResponse(
            code = 422,
            message =
                "- Exist the same ssn on another account. \n"
                    + "- Exist the same email on another account.")
      })
  ResponseEntity<AccountResponse> createAccount(@RequestBody @Valid AccountRequest accountRequest);

  @PutMapping(value = "/personal-data")
  @ApiOperation(
      value = "Update the personal Data. Is needed an authentication to perform this operation.",
      response = AccountResponse.class)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 400,
            message =
                "- Account number cannot be null (must have a value). \n"
                    + "- First name cannot be null (must have a value) or empty. \n"
                    + "- Last name cannot be null (must have a value) or empty. \n"
                    + "- Email cannot be null (must have a value) or empty or must have a valid email format. \n"
                    + "- Voter card Id cannot be null and positive integer. \n"),
        @ApiResponse(
            code = 422,
            message =
                "- Check if the account exists. \n"
                    + "- Check if the account has already confirmed their creation. \n"
                    + "- Check if the account is closed. \n"
                    + "- Check if the account is locked due to retries. \n"
                    + "- Check if the account has been authenticated. \n"
                    + "- Exist the same email on another account.")
      })
  ResponseEntity<AccountResponse> updatePersonalData(
      @RequestBody @Valid AccountUpdateRequest updateRequest);

  @PostMapping("/authenticate")
  @ApiOperation(
      value = "Authenticate the account to perform other operations.",
      response = AuthenticationResponse.class)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 400,
            message =
                "- Account number cannot be null (must have a value). \n"
                    + "- PIN name cannot be null (must have a value) or empty. \n"),
        @ApiResponse(
            code = 422,
            message =
                "- Check if the account exists. \n"
                    + "- Check if the account has already confirmed their creation. \n"
                    + "- Check if the account is closed. \n"
                    + "- Check if the account is locked due to retries. \n"
                    + "- Check if the account has been authenticated. \n"
                    + "- Check if the pin matches, if the max number of retries is reached the account is locked, when "
                    + "the lock expires the account can be reauthenticated. The expiration authentication is defined on configurations, "
                    + "the account expiration is defined as well")
      })
  ResponseEntity<AuthenticationResponse> authenticateAccount(
      @RequestBody @Valid AuthenticationRequest authenticationRequest);

  @DeleteMapping("/close")
  @ApiOperation(value = "Closes or inactivate the account.", response = AccountCloseResponse.class)
  @ApiResponses(
      value = {
        @ApiResponse(code = 400, message = "- Account number cannot be null (must have a value). "),
        @ApiResponse(
            code = 422,
            message =
                "- Check if the account exists. \n"
                    + "- Check if the account has already confirmed their creation. \n"
                    + "- Check if the account was already closed. \n"
                    + "- Check if the account is locked due to retries. \n"
                    + "- Check if the account has been authenticated.")
      })
  ResponseEntity<AccountCloseResponse> close(@RequestBody @Valid AccountCloseRequest request);

  @ApiOperation(
      value =
          "Retrieve the last transactions (the number is defined Closes or inactivate the account.",
      response = AccountLastTransactionsResponse.class)
  @ApiResponses(
      value = {
        @ApiResponse(code = 400, message = "- Account number cannot be null (must have a value). "),
        @ApiResponse(
            code = 422,
            message =
                "- Check if the account exists. \n"
                    + "- Check if the account has already confirmed their creation. \n"
                    + "- Check if the account was already closed. \n"
                    + "- Check if the account is locked due to retries. \n"
                    + "- Check if the account has been authenticated.")
      })
  @GetMapping("/last-transactions/{accountNumber}")
  ResponseEntity<AccountLastTransactionsResponse> lastTransactions(
      @PathVariable("accountNumber") @NotNull Long accountNumber);

  @PostMapping(value = "/deposit")
  @ApiOperation(value = "Perform a deposit on the account.", response = MoneyMovementResponse.class)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 400,
            message =
                "- Account number cannot be null (must have a value). "
                    + "- Amount name cannot be null (must have a value). \n"
                    + "- Description cannot be null (must have a value) or empty. \n"),
        @ApiResponse(
            code = 422,
            message =
                "- Check if the account exists. \n"
                    + "- Check if the account has already confirmed their creation. \n"
                    + "- Check if the account was already closed. \n"
                    + "- Check if the account is locked due to retries. \n"
                    + "- Check if the account has been authenticated."
                    + "- Check if the account is locked to make transfers.")
      })
  ResponseEntity<MoneyMovementResponse> deposit(@RequestBody @Valid MoneyMovementRequest request);

  @PostMapping(value = "/withdrawal")
  @ApiOperation(
      value = "Perform a withdraw on the account.",
      response = MoneyMovementResponse.class)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 400,
            message =
                "- Account number cannot be null (must have a value). "
                    + "- Amount name cannot be null (must have a value). \n"
                    + "- Description cannot be null (must have a value) or empty. \n"),
        @ApiResponse(
            code = 422,
            message =
                "- Check if the account exists. \n"
                    + "- Check if the account has already confirmed their creation. \n"
                    + "- Check if the account was already closed. \n"
                    + "- Check if the account is locked due to retries. \n"
                    + "- Check if the account has been authenticated."
                    + "- Check if the account is locked to make transfers."
                    + "- Check if the account has sufficient founds.")
      })
  ResponseEntity<MoneyMovementResponse> withdrawal(
      @RequestBody @Valid MoneyMovementRequest request);

  @ApiOperation(
      value = "Perform a charge check on the account.",
      response = MoneyMovementResponse.class)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 400,
            message =
                "- Account number cannot be null (must have a value). "
                    + "- Amount name cannot be null (must have a value). \n"
                    + "- Description cannot be null (must have a value) or empty. \n"
                    + "- The pin cannot be null (must have a value) or empty. \n"),
        @ApiResponse(
            code = 422,
            message =
                "- Check if the account exists. \n"
                    + "- Check if the account has already confirmed their creation. \n"
                    + "- Check if the account was already closed. \n"
                    + "- Check if the account is locked due to retries. \n"
                    + "- Check if the account has been authenticated."
                    + "- Check if the account is locked to make transfers."
                    + "- Check if the account has sufficient founds.")
      })
  @PostMapping(value = "/check-charge")
  ResponseEntity<MoneyMovementResponse> checkCharge(
      @RequestBody @Valid ExternalMoneyMovementRequest request);

  @PostMapping(value = "/debit")
  @ApiOperation(value = "Perform a debit on the account.", response = MoneyMovementResponse.class)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 400,
            message =
                "- Account number cannot be null (must have a value). "
                    + "- Amount name cannot be null (must have a value). \n"
                    + "- Description cannot be null (must have a value) or empty. \n"
                    + "- The pin cannot be null (must have a value) or empty. \n"),
        @ApiResponse(
            code = 422,
            message =
                "- Check if the account exists. \n"
                    + "- Check if the account has already confirmed their creation. \n"
                    + "- Check if the account was already closed. \n"
                    + "- Check if the account is locked due to retries. \n"
                    + "- Check if the account has been authenticated."
                    + "- Check if the account is locked to make transfers."
                    + "- Check if the account has sufficient founds.")
      })
  ResponseEntity<MoneyMovementResponse> debit(
      @RequestBody @Valid ExternalMoneyMovementRequest request);

  @ApiOperation(
      value = "Retrieve the current Balance in the account.",
      response = CurrentBalanceResponse.class)
  @ApiResponses(
      value = {
        @ApiResponse(code = 400, message = "- Account number cannot be null (must have a value). "),
        @ApiResponse(
            code = 422,
            message =
                "- Check if the account exists. \n"
                    + "- Check if the account has already confirmed their creation. \n"
                    + "- Check if the account was already closed. \n"
                    + "- Check if the account is locked due to retries. \n"
                    + "- Check if the account has been authenticated.")
      })
  @GetMapping("/current-balance/{accountNumber}")
  ResponseEntity<CurrentBalanceResponse> currentBalance(
      @PathVariable("accountNumber") @NotNull Long accountNumber);
}
