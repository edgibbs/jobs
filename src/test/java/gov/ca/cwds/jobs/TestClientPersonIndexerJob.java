package gov.ca.cwds.jobs;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.ca.cwds.dao.cms.ReplicatedClientDao;
import gov.ca.cwds.data.es.NeutronElasticSearchDao;
import gov.ca.cwds.neutron.flight.FlightPlan;
import gov.ca.cwds.neutron.launch.FlightRecorder;

class TestClientPersonIndexerJob extends ClientPersonIndexerJob {

  private static final long serialVersionUID = 1L;

  private Transaction txn;

  public TestClientPersonIndexerJob(ReplicatedClientDao dao, NeutronElasticSearchDao esDao,
      String lastJobRunTimeFilename, ObjectMapper mapper, SessionFactory sessionFactory,
      FlightRecorder jobHistory, FlightPlan opts) {
    super(dao, esDao, lastJobRunTimeFilename, mapper, opts, null);
  }

  @Override
  public boolean isLargeDataSet() {
    return false;
  }

  @Override
  public Transaction grabTransaction() {
    return txn;
  }

  public Transaction getTxn() {
    return txn;
  }

  public void setTxn(Transaction txn) {
    this.txn = txn;
  }

  @Override
  public boolean isDB2OnZOS() {
    return false;
  }

}
