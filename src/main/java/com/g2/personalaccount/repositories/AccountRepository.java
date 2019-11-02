package com.g2.personalaccount.repositories;

import com.g2.personalaccount.model.Account;
import com.g2.personalaccount.model.enumerated.StatusEnum;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-30 19:47
 */
@Repository
public interface AccountRepository extends CrudRepository<Account, Long> {

  Optional<Account> findByAccountHolder_AccountHolderId_SsnAndStatusIn(
      Long ssn, List<StatusEnum> statusList);

  Optional<Account> findByAccountHolder_EmailAndStatusIn(String email, List<StatusEnum> statusList);
}
