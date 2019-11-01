package com.g2.personalaccount.utils;

import java.util.Random;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Component;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-31 12:54
 */
@Component
public class PinGenerator {

  public Integer generateRandom(Integer length) {
    Random rand = new Random();

    int firstDigit = rand.nextInt(9) + 1;

    String remainDigits = RandomStringUtils.random(length - 1, "0123456789");

    return Integer.valueOf(firstDigit + remainDigits);
  }
}
