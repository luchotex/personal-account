package com.g2.personalaccount.model.enumerated;

import java.util.stream.Stream;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-01 15:47
 */
@Converter(autoApply = true)
public class ConfirmationStatusConverter
    implements AttributeConverter<ConfirmationStatusEnum, String> {

  @Override
  public String convertToDatabaseColumn(ConfirmationStatusEnum status) {
    if (status == null) {
      return null;
    }
    return status.getName();
  }

  @Override
  public ConfirmationStatusEnum convertToEntityAttribute(String code) {
    if (code == null) {
      return null;
    }

    return Stream.of(ConfirmationStatusEnum.values())
        .filter(c -> c.getName().equals(code))
        .findFirst()
        .orElseThrow(IllegalArgumentException::new);
  }
}
