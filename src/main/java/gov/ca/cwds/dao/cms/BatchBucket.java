package gov.ca.cwds.dao.cms;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class BatchBucket implements Serializable {

  @Id
  private int bucket;
  private String minId;
  private String maxId;
  private int bucketCount;

  public String getMinId() {
    return minId;
  }

  public void setMinId(String minId) {
    this.minId = minId;
  }

  public String getMaxId() {
    return maxId;
  }

  public void setMaxId(String maxId) {
    this.maxId = maxId;
  }

  public int getBucketCount() {
    return bucketCount;
  }

  public void setBucketCount(int bucketCount) {
    this.bucketCount = bucketCount;
  }

  public int getBucket() {
    return bucket;
  }

  public void setBucket(int bucket) {
    this.bucket = bucket;
  }

}
