package com.g2.personalaccount.dto.mappers;

import com.g2.personalaccount.dto.responses.TransactionResponse;
import com.g2.personalaccount.model.Transaction;
import java.util.List;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-11-03 23:28
 */
@Mapper
public interface TransactionMapper {

  @Mapping(target = "localDateTime", source = "createDateTime")
  @Mapping(target = "transactionId", source = "id")
  TransactionResponse toLastTransactionsResponse(Transaction transaction);

  @InheritConfiguration
  List<TransactionResponse> toList(Iterable<Transaction> entity);
}
