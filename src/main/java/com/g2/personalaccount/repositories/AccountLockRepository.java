package com.g2.personalaccount.repositories;

import com.g2.personalaccount.model.AccountLock;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-11-04 08:09
 */
@Repository
public interface AccountLockRepository extends CrudRepository<AccountLock, Long> {

  Optional<AccountLock> findByAccount_Id(Long accountId);
}
