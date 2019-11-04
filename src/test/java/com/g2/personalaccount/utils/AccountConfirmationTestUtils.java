package com.g2.personalaccount.utils;

import com.g2.personalaccount.model.Account;
import com.g2.personalaccount.model.AccountConfirmation;
import com.g2.personalaccount.model.AccountHolder;
import com.g2.personalaccount.model.enumerated.ConfirmationStatusEnum;
import com.g2.personalaccount.model.enumerated.StatusEnum;
import java.time.LocalDateTime;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-11-01 22:48
 */
public class AccountConfirmationTestUtils {

  private AccountConfirmationTestUtils() {}

  public static AccountConfirmation createAccountConfirmation() {
    AccountConfirmation confirmation = new AccountConfirmation();
    confirmation.setExpirationDate(LocalDateTime.now().plusSeconds(10000));
    confirmation.setConfirmationStatusEnum(ConfirmationStatusEnum.ACTIVE);

    Account account = new Account();
    account.setId(123456789123L);
    account.setStatus(StatusEnum.ON_CONFIRM);

    AccountHolder accountHolder = new AccountHolder();
    accountHolder.setFirstName("test first name");
    accountHolder.setLastName("test last name");
    accountHolder.setEmail("test email");
    account.setAccountHolder(accountHolder);

    confirmation.setAccount(account);

    return confirmation;
  }
}
