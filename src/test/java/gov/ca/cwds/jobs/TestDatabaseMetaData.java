package gov.ca.cwds.jobs;

import java.sql.SQLException;

import com.ibm.db2.jcc.am.Agent;
import com.ibm.db2.jcc.am.Connection;
import com.ibm.db2.jcc.am.DatabaseMetaData;
import com.ibm.db2.jcc.am.ProductLevel;

public class TestDatabaseMetaData extends DatabaseMetaData {

  protected TestDatabaseMetaData(Agent arg0, Connection arg1, ProductLevel arg2) {
    super(arg0, arg1, arg2);
  }

  @Override
  public String getURL_() throws SQLException {
    return null;
  }

  @Override
  protected boolean supportsMixedCasePackageCollectionName_() {
    return false;
  }

  @Override
  protected void computeFeatureSet_() {}

}
