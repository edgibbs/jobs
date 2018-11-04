package gov.ca.cwds.neutron.util.jdbc;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.data.persistence.cms.client.RawClient;
import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.neutron.atom.AtomLoadStepHandler;

public class WorkSecondaryResultsTest extends Goddard {

  AtomLoadStepHandler<RawClient> handler;
  WorkSecondaryResults target;

  @Before
  @Override
  public void setup() throws Exception {
    super.setup();

    handler = mock(AtomLoadStepHandler.class);
    target = new WorkSecondaryResults(handler);
  }

  @Test
  public void type() throws Exception {
    assertThat(WorkSecondaryResults.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void execute_A$Connection() throws Exception {
    target.execute(con);
  }

  @Test(expected = SQLException.class)
  public void execute_A$Connection_T$SQLException() throws Exception {
    doThrow(SQLException.class).when(con).setSchema(any(String.class));
    target.execute(con);
  }

}
