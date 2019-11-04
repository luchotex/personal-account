package com.g2.personalaccount.dto.mappers;

import com.g2.personalaccount.config.ServiceConfig;
import com.g2.personalaccount.dto.requests.AccountRequest;
import com.g2.personalaccount.model.Account;
import com.g2.personalaccount.model.AccountAccess;
import com.g2.personalaccount.model.AccountConfirmation;
import com.g2.personalaccount.model.enumerated.ConfirmationStatusEnum;
import com.g2.personalaccount.model.enumerated.StatusEnum;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-31 17:52
 */
public abstract class AccountMapperDecorator implements AccountMapper {

  @Autowired
  @Qualifier("delegate")
  private AccountMapper delegate;

  @Autowired private ServiceConfig serviceConfig;

  @Override
  public Account toEntity(AccountRequest request, Integer pin) {
    Account account = delegate.toEntity(request, pin);

    if (Objects.isNull(account)) {
      return null;
    }

    account.setStatus(StatusEnum.ON_CONFIRM);
    account.setAccountAccess(new AccountAccess());
    String md5Pin = DigestUtils.md5Hex(String.valueOf(pin));

    account.getAccountAccess().setPin(md5Pin);

    AccountConfirmation confirmation = new AccountConfirmation();
    confirmation.setConfirmationId(UUID.randomUUID().toString());
    confirmation.setExpirationDate(
        LocalDateTime.now()
            .plusSeconds(Integer.valueOf(serviceConfig.getConfirmationExpirationSeconds())));
    confirmation.setConfirmationStatusEnum(ConfirmationStatusEnum.ACTIVE);

    account.setAccountConfirmation(confirmation);

    return account;
  }
}
