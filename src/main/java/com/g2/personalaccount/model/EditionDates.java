package com.g2.personalaccount.model;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * @author Luis M. Kupferberg Ruiz (lkupferberg@overactive.com)
 * @created 2019-10-30 19:12
 */
@MappedSuperclass
public abstract class EditionDates {

  @Column(name = "create_dateTime")
  @CreationTimestamp
  private LocalDateTime createDateTime;

  @Column(name = "update_dateTime")
  @UpdateTimestamp
  private LocalDateTime updateDateTime;

  public LocalDateTime getCreateDateTime() {
    return createDateTime;
  }

  public void setCreateDateTime(LocalDateTime createDateTime) {
    this.createDateTime = createDateTime;
  }

  public LocalDateTime getUpdateDateTime() {
    return updateDateTime;
  }

  public void setUpdateDateTime(LocalDateTime updateDateTime) {
    this.updateDateTime = updateDateTime;
  }
}
