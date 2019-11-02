package com.g2.personalaccount.services.impl;

import static com.g2.personalaccount.services.impl.AccountConfirmationServiceImpl.CONFIRMATION_ALREADY_EXECUTED_TEMPLATE;
import static com.g2.personalaccount.services.impl.AccountConfirmationServiceImpl.CONFIRMATION_DOESNT_EXIST_TEMPLATE;
import static com.g2.personalaccount.services.impl.AccountConfirmationServiceImpl.CONFIRMATION_HAS_EXPIRED_TEMPLATE;
import static com.g2.personalaccount.services.impl.AccountConfirmationServiceImpl.CONFIRMATION_SUCCESSFUL_TEMPLATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.g2.personalaccount.model.AccountConfirmation;
import com.g2.personalaccount.model.enumerated.ConfirmationStatusEnum;
import com.g2.personalaccount.model.enumerated.StatusEnum;
import com.g2.personalaccount.repositories.AccountConfirmationRepository;
import com.g2.personalaccount.services.AccountConfirmationService;
import com.g2.personalaccount.utils.AccountConfirmationTestUtils;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AccountConfirmationServiceImplTest {

  private AccountConfirmationService service;
  @Mock AccountConfirmationRepository repository;

  @Before
  public void setUp() throws Exception {
    service = new AccountConfirmationServiceImpl(repository);
  }

  @Test
  public void confirmation_successfullyExecuted() {
    // given

    String confirmId = "sdfsdf123";

    AccountConfirmation confirmation = AccountConfirmationTestUtils.createAccountConfirmation();

    // when
    when(repository.findByConfirmationId(anyString())).thenReturn(Optional.of(confirmation));
    String response = service.confirmCreation(confirmId);

    // then
    assertNotNull(response);
    assertEquals(CONFIRMATION_SUCCESSFUL_TEMPLATE, response);

    ArgumentCaptor<AccountConfirmation> accountArgumentCaptor =
        ArgumentCaptor.forClass(AccountConfirmation.class);

    verify(repository, times(1)).save(accountArgumentCaptor.capture());

    AccountConfirmation savingValue = accountArgumentCaptor.getValue();

    assertEquals(ConfirmationStatusEnum.CONFIRMED, savingValue.getConfirmationStatusEnum());
    assertEquals(StatusEnum.ACTIVE.ACTIVE, savingValue.getAccount().getStatus());
  }

  @Test
  public void confirmation_notExist() {
    // given

    String confirmId = "sdfsdf123";

    // when
    when(repository.findByConfirmationId(anyString())).thenReturn(Optional.empty());
    String response = service.confirmCreation(confirmId);

    // then
    assertNotNull(response);
    assertEquals(CONFIRMATION_DOESNT_EXIST_TEMPLATE, response);
  }

  @Test
  public void confirmation_alreadyConfirmed() {
    // given

    String confirmId = "sdfsdf123";

    AccountConfirmation confirmation = AccountConfirmationTestUtils.createAccountConfirmation();
    confirmation.setConfirmationStatusEnum(ConfirmationStatusEnum.CONFIRMED);

    // when
    when(repository.findByConfirmationId(anyString())).thenReturn(Optional.of(confirmation));
    String response = service.confirmCreation(confirmId);

    // then
    assertNotNull(response);
    assertEquals(CONFIRMATION_ALREADY_EXECUTED_TEMPLATE, response);
  }

  @Test
  public void confirmation_hasExpired() {
    // given

    String confirmId = "sdfsdf123";

    AccountConfirmation confirmation = AccountConfirmationTestUtils.createAccountConfirmation();
    confirmation.setExpirationDate(LocalDateTime.now().minusSeconds(1000));

    // when
    when(repository.findByConfirmationId(anyString())).thenReturn(Optional.of(confirmation));
    String response = service.confirmCreation(confirmId);

    // then
    assertNotNull(response);
    assertEquals(CONFIRMATION_HAS_EXPIRED_TEMPLATE, response);
  }
}
