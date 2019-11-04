package com.g2.personalaccount.services;

import com.g2.personalaccount.dto.requests.AccountCloseRequest;
import com.g2.personalaccount.dto.requests.AccountRequest;
import com.g2.personalaccount.dto.requests.AccountUpdateRequest;
import com.g2.personalaccount.dto.requests.AuthenticationRequest;
import com.g2.personalaccount.dto.requests.ExternalMoneyMovementRequest;
import com.g2.personalaccount.dto.requests.MoneyMovementRequest;
import com.g2.personalaccount.dto.responses.AccountCloseResponse;
import com.g2.personalaccount.dto.responses.AccountResponse;
import com.g2.personalaccount.dto.responses.AuthenticationResponse;
import com.g2.personalaccount.dto.responses.MoneyMovementResponse;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-31 09:26
 */
public interface AccountService {

  AccountResponse create(AccountRequest request);

  AccountResponse updatePersonalData(AccountUpdateRequest request);

  AuthenticationResponse authenticateAccount(AuthenticationRequest authenticationRequest);

  // TODO implement java doc

  AccountCloseResponse close(AccountCloseRequest request);

  MoneyMovementResponse deposit(MoneyMovementRequest request);

  MoneyMovementResponse withDrawal(MoneyMovementRequest request);

  MoneyMovementResponse debit(ExternalMoneyMovementRequest request);

  MoneyMovementResponse checkCharge(ExternalMoneyMovementRequest request);
}
