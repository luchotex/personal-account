package com.g2.personalaccount.repositories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.g2.personalaccount.dto.requests.AccountRequest;
import com.g2.personalaccount.model.Account;
import com.g2.personalaccount.utils.AccountTestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("dev")
@RunWith(SpringRunner.class)
@SpringBootTest(
    properties = {
      "DATABASE_CONNECTION=jdbc:h2:mem:g2db;DB_CLOSE_ON_EXIT=FALSE",
      "DATABASE_USERNAME=sa",
      "DATABASE_PASSWORD=password",
      "MAIL_HOST=smtp.gmail.com",
      "MAIL_USERNAME=g2testservices@gmail.com",
      "MAIL_PASSWORD=Testg2123@",
      "MAIL_PORT=587"
    })
public class AccountRepositoryTest {

  @Autowired private AccountRepository accountRepository;

  @Test
  public void savingAccount() {
    // given
    AccountRequest request = AccountTestUtils.createAccountRequest();
    Account account = AccountTestUtils.createAccount(request);
    account.setId(null);

    // when
    Account savedAccount = accountRepository.save(account);

    // then
    assertEquals(
        account.getAccountHolder().getFirstName(), savedAccount.getAccountHolder().getFirstName());
    assertEquals(
        account.getAccountHolder().getLastName(), savedAccount.getAccountHolder().getLastName());
    assertEquals(account.getAccountHolder().getEmail(), savedAccount.getAccountHolder().getEmail());
    assertEquals(
        account.getAccountHolder().getAccountHolderId().getSsn(),
        savedAccount.getAccountHolder().getAccountHolderId().getSsn());
    assertEquals(
        account.getAccountHolder().getAccountHolderId().getVoterCardId(),
        savedAccount.getAccountHolder().getAccountHolderId().getVoterCardId());

    assertNotNull(account.getId());
    assertEquals(12, String.valueOf(account.getId()).length());

    assertNotNull(account.getCreateDateTime());
    assertNotNull(account.getUpdateDateTime());

    assertNotNull(account.getAccountAccess().getCreateDateTime());
    assertNotNull(account.getAccountAccess().getUpdateDateTime());
  }

  @Test
  public void updatingAccount() {
    // given
    AccountRequest request = AccountTestUtils.createAccountRequest();
    Account account = AccountTestUtils.createAccount(request);
    account.setId(null);

    // when
    Account savedAccount = accountRepository.save(account);
    savedAccount.getAccountHolder().setFirstName("First name updated");
    savedAccount.getAccountHolder().setLastName("Last name updated");
    savedAccount.getAccountHolder().setEmail("Email updated");
    savedAccount.getAccountHolder().getAccountHolderId().setSsn(564234125);
    savedAccount.getAccountHolder().getAccountHolderId().setVoterCardId(958923393);

    Account updatedAccount = accountRepository.save(savedAccount);

    // then
    assertEquals(
        savedAccount.getAccountHolder().getFirstName(),
        updatedAccount.getAccountHolder().getFirstName());
    assertEquals(
        savedAccount.getAccountHolder().getLastName(),
        updatedAccount.getAccountHolder().getLastName());
    assertEquals(
        savedAccount.getAccountHolder().getEmail(), updatedAccount.getAccountHolder().getEmail());

    assertNotNull(updatedAccount.getId());
    assertEquals(12, String.valueOf(updatedAccount.getId()).length());
    assertEquals(savedAccount.getId(), updatedAccount.getId());

    assertNotNull(updatedAccount.getCreateDateTime());
    assertNotNull(updatedAccount.getUpdateDateTime());
    assertTrue(updatedAccount.getCreateDateTime().isBefore(updatedAccount.getUpdateDateTime()));

    assertNotNull(updatedAccount.getAccountAccess().getCreateDateTime());
    assertNotNull(updatedAccount.getAccountAccess().getUpdateDateTime());
  }
}
