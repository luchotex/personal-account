package com.g2.personalaccount.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PinGeneratorTest {

  private PinGenerator pinGenerator;

  @Before
  public void setUp() throws Exception {
    pinGenerator = new PinGenerator();
  }

  @Test
  public void firstDigitIsNotZero() {
    Integer pin = pinGenerator.generateRandom(4);

    String stringPin = String.valueOf(pin);

    assertTrue(stringPin.charAt(0) != 0);
  }

  @Test
  public void testAllDigitsAreNotEqual() {
    Integer pin = pinGenerator.generateRandom(4);

    String stringPin = String.valueOf(pin);

    boolean pinAllDigitsAreEqual = true;

    Integer lastDigit = 0;
    Integer currentDigit;

    for (int i = 1; i <= 4; i++) {

      if (i > 1) {
        currentDigit = Integer.valueOf(stringPin.charAt(i - 1));
        if (!currentDigit.equals(lastDigit)) {
          pinAllDigitsAreEqual = false;
          break;
        }
      }

      lastDigit = Integer.valueOf(stringPin.charAt(i - 1));
    }

    assertFalse(pinAllDigitsAreEqual);
  }

  @Test
  public void testAllDigitsAreNotConsecutive() {
    Integer pin = pinGenerator.generateRandom(4);

    String stringPin = String.valueOf(pin);

    boolean pinDigitsAreConsecutive = true;

    Integer lastDigit = 0;
    Integer currentDigit;

    for (int i = 1; i <= 4; i++) {

      if (i > 1) {
        currentDigit = Integer.valueOf(stringPin.charAt(i - 1));
        if (!currentDigit.equals(lastDigit + 1)) {
          pinDigitsAreConsecutive = false;
          break;
        }
      }

      lastDigit = Integer.valueOf(stringPin.charAt(i - 1));
    }

    assertFalse(pinDigitsAreConsecutive);
  }

  @Test
  public void testAllDigitsAreNotReverseConsecutive() {
    Integer pin = pinGenerator.generateRandom(4);

    String stringPin = String.valueOf(pin);

    boolean pinDigitsAreConsecutive = true;

    Integer lastDigit = 0;
    Integer currentDigit;

    for (int i = 1; i <= 4; i++) {

      if (i > 1) {
        currentDigit = Integer.valueOf(stringPin.charAt(i - 1));
        if (!lastDigit.equals(currentDigit + 1)) {
          pinDigitsAreConsecutive = false;
          break;
        }
      }

      lastDigit = Integer.valueOf(stringPin.charAt(i - 1));
    }

    assertFalse(pinDigitsAreConsecutive);
  }
}
