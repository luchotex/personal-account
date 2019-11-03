package com.g2.personalaccount.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@Configuration
@Component
@ConfigurationProperties(prefix = "g2.service")
@Data
public class ServiceConfig {
  private String hostname;
  private String confirmationExpirationSeconds;
  private String pinLength;
  private String pinExpirationSeconds;
  private String numberPinRetries;
  private String accountLockingSeconds;
}
