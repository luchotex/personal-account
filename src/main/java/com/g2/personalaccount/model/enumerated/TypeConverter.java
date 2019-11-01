package com.g2.personalaccount.model.enumerated;

import java.util.stream.Stream;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-31 20:22
 */
@Converter(autoApply = true)
public class TypeConverter implements AttributeConverter<TypeEnum, String> {

  @Override
  public String convertToDatabaseColumn(TypeEnum type) {
    if (type == null) {
      return null;
    }
    return type.getName();
  }

  @Override
  public TypeEnum convertToEntityAttribute(String code) {
    if (code == null) {
      return null;
    }

    return Stream.of(TypeEnum.values())
        .filter(c -> c.getName().equals(code))
        .findFirst()
        .orElseThrow(IllegalArgumentException::new);
  }
}
