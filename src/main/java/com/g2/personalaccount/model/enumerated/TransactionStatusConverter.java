package com.g2.personalaccount.model.enumerated;

import java.util.Objects;
import java.util.stream.Stream;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-13-03 13:41
 */
@Converter(autoApply = true)
public class TransactionStatusConverter
    implements AttributeConverter<TransactionStatusEnum, String> {

  @Override
  public String convertToDatabaseColumn(TransactionStatusEnum status) {
    if (Objects.isNull(status)) {
      return null;
    }
    return status.getName();
  }

  @Override
  public TransactionStatusEnum convertToEntityAttribute(String code) {
    if (Objects.isNull(code)) {
      return null;
    }

    return Stream.of(TransactionStatusEnum.values())
        .filter(c -> c.getName().equals(code))
        .findFirst()
        .orElseThrow(IllegalArgumentException::new);
  }
}
