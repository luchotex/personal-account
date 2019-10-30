package com.g2.personalaccount;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.validation.annotation.Validated;

@SpringBootApplication
@Validated
public class PersonalAccountApplication {

  public static void main(String[] args) {
    SpringApplication.run(PersonalAccountApplication.class, args);
  }
}
