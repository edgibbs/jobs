package gov.ca.cwds.jobs;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import gov.ca.cwds.jobs.ReportFaultCANSClientIdsJob.CansClient;

public class ReportFaultCANSClientIdsJobTest extends Goddard {

  ReportFaultCANSClientIdsJob target;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();
    target = new ReportFaultCANSClientIdsJob(sessionFactory, sessionFactory);
  }

  @Test
  public void type() throws Exception {
    assertThat(ReportFaultCANSClientIdsJob.class, notNullValue());
  }

  @Test
  @Ignore
  public void main_A$StringArray() throws Exception {
    final String[] args = new String[] {};
    ReportFaultCANSClientIdsJob.main(args);
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void mapNQResults_A$List$Class() throws Exception {
    List objectArrayList = new ArrayList();
    List<Object> actual =
        ReportFaultCANSClientIdsJob.mapNQResults(objectArrayList, CansClient.class);
    List<Object> expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getNQResultColumnAnnotatedFields_A$Class() throws Exception {
    Class<Object> genericType = mock(Class.class);
    List<Field> actual = ReportFaultCANSClientIdsJob.getNQResultColumnAnnotatedFields(genericType);
    List<Field> expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setObjectProperty_A$Object$Field$Object() throws Exception {
    Object object = null;
    Field field = mock(Field.class);
    Object fieldValue = null;
    ReportFaultCANSClientIdsJob.setObjectProperty(object, field, fieldValue);
  }

  @Test
  public void grabCansSession_A$() throws Exception {
    ReportFaultCANSClientIdsJob target = new ReportFaultCANSClientIdsJob();
    Session actual = target.grabCansSession();
    Session expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void grabCmsSession_A$() throws Exception {
    ReportFaultCANSClientIdsJob target = new ReportFaultCANSClientIdsJob();
    Session actual = target.grabCmsSession();
    Session expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void buildClientList_A$() throws Exception {
    ReportFaultCANSClientIdsJob target = new ReportFaultCANSClientIdsJob();
    target.buildClientList();
  }

  @Test
  public void generateReport_A$() throws Exception {
    ReportFaultCANSClientIdsJob target = new ReportFaultCANSClientIdsJob();
    target.generateReport();
  }

  @Test
  public void initReport_A$() throws Exception {
    ReportFaultCANSClientIdsJob target = new ReportFaultCANSClientIdsJob();
    target.initReport();
  }

  @Test
  public void finalizeReport_A$() throws Exception {
    ReportFaultCANSClientIdsJob target = new ReportFaultCANSClientIdsJob();
    target.finalizeReport();
  }

  @Test
  public void isValidClientId_A$Object() throws Exception {
    ReportFaultCANSClientIdsJob target = new ReportFaultCANSClientIdsJob();
    CansClient clientDto = null;
    boolean actual = target.isValidClientId(clientDto);
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void attemptToFix_A$Object() throws Exception {
    ReportFaultCANSClientIdsJob target = new ReportFaultCANSClientIdsJob();
    CansClient clientDto = null;
    target.attemptToFix(clientDto);
  }

  @Test
  public void buildReportFileName_A$() throws Exception {
    ReportFaultCANSClientIdsJob target = new ReportFaultCANSClientIdsJob();
    target.buildReportFileName();
  }

  @Test
  public void reportClient_A$Object() throws Exception {
    ReportFaultCANSClientIdsJob target = new ReportFaultCANSClientIdsJob();
    CansClient clientPojo = null;
    target.reportClient(clientPojo);
  }

}
