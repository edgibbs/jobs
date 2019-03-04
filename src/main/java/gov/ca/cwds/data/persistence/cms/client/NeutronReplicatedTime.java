package gov.ca.cwds.data.persistence.cms.client;

public interface NeutronReplicatedTime {

  long calcReplicationTime();

  boolean hasAddedTime();

}
