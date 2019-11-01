package com.g2.personalaccount.dto.mappers;

import com.g2.personalaccount.dto.requests.AccountRequest;
import com.g2.personalaccount.model.Account;
import com.g2.personalaccount.model.AccountAccess;
import com.g2.personalaccount.model.enumerated.StatusEnum;
import com.g2.personalaccount.utils.PinGenerator;
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

  @Autowired private PinGenerator pinGenerator;

  @Override
  public Account toEntity(AccountRequest request) {
    Account account = delegate.toEntity(request);

    if (Objects.isNull(account)) {
      return null;
    }

    account.setStatus(StatusEnum.ACTIVE);
    account.setAccountAccess(new AccountAccess());
    // TODO define pin length configuration

    Integer generatedPin = pinGenerator.generateRandom(4);
    String md5Pin = DigestUtils.md5Hex(String.valueOf(generatedPin));

    account.getAccountAccess().setPin(md5Pin);

    return account;
  }
}
