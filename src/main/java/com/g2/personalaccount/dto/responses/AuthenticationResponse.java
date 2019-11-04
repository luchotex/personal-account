package com.g2.personalaccount.dto.responses;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-11-01 23:39
 */
@Data
public class AuthenticationResponse {

  private Long transactionId;
  private LocalDateTime expirationDateTime;
}
