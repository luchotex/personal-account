package com.g2.personalaccount.model.enumerated;

import java.util.stream.Stream;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-31 20:21
 */
@Converter(autoApply = true)
public class StatusConverter implements AttributeConverter<StatusEnum, String> {

  @Override
  public String convertToDatabaseColumn(StatusEnum status) {
    if (status == null) {
      return null;
    }
    return status.getName();
  }

  @Override
  public StatusEnum convertToEntityAttribute(String code) {
    if (code == null) {
      return null;
    }

    return Stream.of(StatusEnum.values())
        .filter(c -> c.getName().equals(code))
        .findFirst()
        .orElseThrow(IllegalArgumentException::new);
  }
}
