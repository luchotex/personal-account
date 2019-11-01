package com.g2.personalaccount.model.generator;

import java.io.Serializable;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Stream;
import org.apache.commons.lang.RandomStringUtils;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-31 12:15
 */
public class AccountNumberGenerator implements IdentifierGenerator, Configurable {

  private String prefix;

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

      Stream ids = retrieveIds(session, object, shortId);
      if (!Objects.isNull(ids)) {
        isNotUnique = false;
      }
    }

    return Long.valueOf(shortId);
  }

  private Stream retrieveIds(SharedSessionContractImplementor session, Object object, String id) {
    String query =
        String.format(
            "select %s from %s where id=%s",
            session
                .getEntityPersister(object.getClass().getName(), object)
                .getIdentifierPropertyName(),
            object.getClass().getSimpleName(),
            id);

    return session.createQuery(query).stream();
  }
}
