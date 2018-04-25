package gov.ca.cwds.jobs.common.job.timestamp;

import gov.ca.cwds.jobs.common.identifier.ChangedEntityIdentifier;

/**
 * Created by Alexander Serbin on 2/5/2018.
 */
public interface SavepointOperator {

  boolean savepointExists();

  ChangedEntityIdentifier readSavepoint();

  void writeSavepoint(ChangedEntityIdentifier savepoint);

}
