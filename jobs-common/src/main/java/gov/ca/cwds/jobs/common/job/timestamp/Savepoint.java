package gov.ca.cwds.jobs.common.job.timestamp;

import java.time.LocalDateTime;

public class Savepoint {

  private Integer id;

  private LocalDateTime timestamp;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }
}
