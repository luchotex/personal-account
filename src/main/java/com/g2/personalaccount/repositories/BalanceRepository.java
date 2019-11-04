package com.g2.personalaccount.repositories;

import com.g2.personalaccount.model.Balance;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-30 19:53
 */
@Repository
public interface BalanceRepository extends CrudRepository<Balance, Long> {

  @Query(
      value =
          "SELECT null as balance_id, null as create_date_time , null as update_date_time, "
              + "null as amount, null as locking_thread_name, null as locking_date_time, null as account_id, null as total"
              + " FROM dual WHERE (@total\\:=0) "
              + " union SELECT *, "
              + "(@total\\:=@total + amount) AS total "
              + "FROM balance WHERE @total < :amount and account_id = :accountNumber "
              + "and (locking_date_time is null or locking_date_time < now(3))",
      nativeQuery = true)
  List<Balance> retrieveBalances(
      @Param("accountNumber") Long accountNumber, @Param("amount") BigDecimal amount);

  @Modifying
  @Query(
      "update Balance b SET b.lockingThreadName = null, b.lockingDateTime = null "
          + "WHERE b.lockingThreadName=:threadName")
  void releaseBalances(@Param("threadName") String threadName);
}
