package com.g2.personalaccount.model.generator;

import com.g2.personalaccount.model.Account;
import com.g2.personalaccount.repositories.AccountRepository;
import java.io.Serializable;
import java.util.Optional;
import java.util.Properties;
import org.apache.commons.lang.RandomStringUtils;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;
import org.springframework.stereotype.Component;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-31 12:15
 */
@Component
public class AccountNumberGenerator implements IdentifierGenerator, Configurable {

  private String prefix;
  private AccountRepository accountRepository;

  public AccountNumberGenerator(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  public void configure(Type type, Properties properties, ServiceRegistry serviceRegistry)
      throws MappingException {
    prefix = properties.getProperty("prefix");
  }

  @Override
  public Serializable generate(SharedSessionContractImplementor session, Object object)
      throws HibernateException {

    boolean isNotUnique = true;
    String shortId = "";

    while (isNotUnique) {
      shortId = RandomStringUtils.random(10, "0123456789");
      shortId = prefix + shortId;
      Optional<Account> optionalAccount = accountRepository.findById(Long.valueOf(shortId));
      if (!optionalAccount.isPresent()) {
        isNotUnique = false;
      }
    }

    return shortId;
  }
}
