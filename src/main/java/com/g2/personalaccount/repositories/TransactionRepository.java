package com.g2.personalaccount.repositories;

import com.g2.personalaccount.model.Transaction;
import com.g2.personalaccount.model.enumerated.TransactionStatusEnum;
import com.g2.personalaccount.model.enumerated.TypeEnum;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-30 19:53
 */
@Repository
public interface TransactionRepository extends CrudRepository<Transaction, Long> {

  List<Transaction> findByAccount_IdAndStatusAndTypeIn(
      Long accountNumber,
      TransactionStatusEnum statusEnum,
      List<TypeEnum> typeList,
      Pageable pageable);
}
