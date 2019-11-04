package com.g2.personalaccount.dto.responses;

import com.g2.personalaccount.model.enumerated.TypeEnum;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-11-03 22:49
 */
@Data
public class TransactionResponse {
  private Long transactionId;
  private LocalDateTime localDateTime;
  private BigDecimal amount;
  private String description;
  private TypeEnum type;
}
