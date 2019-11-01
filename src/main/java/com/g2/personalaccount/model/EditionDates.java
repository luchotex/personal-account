package com.g2.personalaccount.model;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-30 19:12
 */
@MappedSuperclass
@Data
public abstract class EditionDates {

  @Column(name = "create_dateTime")
  @CreationTimestamp
  private LocalDateTime createDateTime;

  @Column(name = "update_dateTime")
  @UpdateTimestamp
  private LocalDateTime updateDateTime;
}
