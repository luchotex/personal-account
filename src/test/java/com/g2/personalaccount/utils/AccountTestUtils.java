package com.g2.personalaccount.utils;

import com.g2.personalaccount.dto.requests.AccountRequest;
import com.g2.personalaccount.dto.requests.AccountUpdateRequest;
import com.g2.personalaccount.model.Account;
import com.g2.personalaccount.model.AccountAccess;
import com.g2.personalaccount.model.AccountConfirmation;
import com.g2.personalaccount.model.AccountHolder;
import com.g2.personalaccount.model.AccountHolderId;
import com.g2.personalaccount.model.enumerated.StatusEnum;
import java.time.LocalDateTime;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-11-01 07:43
 */
public class AccountTestUtils {

  public static AccountRequest createAccountRequest() {
    AccountRequest request = new AccountRequest();
    request.setHolderFirstName("Test first Name");
    request.setHolderLastName("Test last Name");
    request.setEmail("email@test.com");
    request.setSsn(12332442L);
    request.setVoterCardId(12332442L);

    return request;
  }

  public static AccountUpdateRequest createAccountUpdateRequest() {
    AccountUpdateRequest request = new AccountUpdateRequest();
    request.setId(123456789123L);
    request.setHolderFirstName("Test first Name update");
    request.setHolderLastName("Test last Name update");
    request.setEmail("emaiupdatedl@test.com");
    request.setEmail("emaiupdatedl@test.com");
    request.setVoterCardId(1233244255L);

    return request;
  }

  public static Account createAccount(AccountRequest accountRequest) {
    Account account = new Account();
    account.setId(123456789123L);
    account.setStatus(StatusEnum.ACTIVE);

    AccountHolder accountHolder = new AccountHolder();
    accountHolder.setFirstName(accountRequest.getHolderFirstName());
    accountHolder.setLastName(accountRequest.getHolderLastName());
    accountHolder.setEmail(accountRequest.getEmail());

    AccountHolderId accountHolderId = new AccountHolderId();
    accountHolderId.setSsn(accountRequest.getSsn());
    accountHolderId.setVoterCardId(accountRequest.getVoterCardId());
    accountHolder.setAccountHolderId(accountHolderId);

    account.setAccountHolder(accountHolder);

    AccountAccess accountAccess = new AccountAccess();
    account.setAccountAccess(accountAccess);

    AccountConfirmation confirmation = new AccountConfirmation();
    account.setAccountConfirmation(confirmation);

    return account;
  }

  public static Account createUpdateAccount(AccountUpdateRequest accountRequest) {
    Account account = new Account();
    account.setId(123456789123L);
    account.setStatus(StatusEnum.ACTIVE);
    account.setCreateDateTime(LocalDateTime.now());
    account.setUpdateDateTime(LocalDateTime.now());

    AccountHolder accountHolder = new AccountHolder();
    accountHolder.setFirstName(accountRequest.getHolderFirstName());
    accountHolder.setLastName(accountRequest.getHolderLastName());
    accountHolder.setEmail(accountRequest.getEmail());

    AccountHolderId accountHolderId = new AccountHolderId();
    accountHolderId.setSsn(343523432l);
    accountHolderId.setVoterCardId(accountRequest.getVoterCardId());
    accountHolder.setAccountHolderId(accountHolderId);

    AccountAccess accountAccess = new AccountAccess();
    accountAccess.setCreateDateTime(LocalDateTime.now());
    accountAccess.setUpdateDateTime(LocalDateTime.now());
    account.setAccountAccess(accountAccess);

    account.setAccountHolder(accountHolder);

    return account;
  }
}
