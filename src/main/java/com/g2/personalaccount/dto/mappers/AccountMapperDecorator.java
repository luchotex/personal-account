package com.g2.personalaccount.dto.mappers;

import com.g2.personalaccount.dto.requests.AccountRequest;
import com.g2.personalaccount.model.Account;
import com.g2.personalaccount.model.AccountAccess;
import com.g2.personalaccount.model.enumerated.StatusEnum;
import java.util.Objects;
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

  @Override
  public Account toEntity(AccountRequest request, Integer pin) {
    Account account = delegate.toEntity(request, pin);

    if (Objects.isNull(account)) {
      return null;
    }

    account.setStatus(StatusEnum.ACTIVE);
    account.setAccountAccess(new AccountAccess());
    // TODO define pin length configuration

    String md5Pin = DigestUtils.md5Hex(String.valueOf(pin));

    account.getAccountAccess().setPin(md5Pin);

    return account;
  }
}
