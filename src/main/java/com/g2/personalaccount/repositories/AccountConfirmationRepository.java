package com.g2.personalaccount.repositories;

import com.g2.personalaccount.model.AccountConfirmation;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-11-01 15:02
 */
public interface AccountConfirmationRepository extends CrudRepository<AccountConfirmation, Long> {

  Optional<AccountConfirmation> findByConfirmationId(String confirmId);
}
