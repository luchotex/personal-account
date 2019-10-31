package com.g2.personalaccount.repositories;

import com.g2.personalaccount.model.Balance;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-30 19:53
 */
@Repository
public interface BalanceRepository extends CrudRepository<Balance, Long> {}
