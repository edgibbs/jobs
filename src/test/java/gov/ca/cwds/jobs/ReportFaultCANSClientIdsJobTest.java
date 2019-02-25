package gov.ca.cwds.jobs;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import gov.ca.cwds.jobs.ReportFaultCANSClientIdsJob.CansClient;
import gov.ca.cwds.neutron.exception.NeutronRuntimeException;

public class ReportFaultCANSClientIdsJobTest extends Goddard {

  ReportFaultCANSClientIdsJob target;
  CansClient cansClient;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();

    when(rs.getLong(1)).thenReturn(3777502L);
    when(rs.getString(2)).thenReturn("AdE0PWu0X5");
    when(rs.getString(3)).thenReturn("See");
    when(rs.getString(4)).thenReturn("K");
    when(rs.getString(5)).thenReturn("Abbott");
    when(rs.getString(6)).thenReturn("");
    when(rs.getDate(7)).thenReturn(new java.sql.Date(parseDate("2005-08-14").getTime()));
    when(rs.getString(8)).thenReturn("");
    when(rs.getString(9)).thenReturn("Madera");
    when(rs.getString(10)).thenReturn("IN_PROGRESS");
    when(rs.getDate(11)).thenReturn(new java.sql.Date(parseDate("2019-02-12").getTime()));
    when(rs.getString(12)).thenReturn("RACFID");
    when(rs.getString(13)).thenReturn("Anna");
    when(rs.getString(14)).thenReturn("Smith");

    int i = 0;
    final Object[] objs = new Object[14];
    objs[i++] = "3777502";
    objs[i++] = "AdE0PWu0X5";
    objs[i++] = "See";
    objs[i++] = "K";
    objs[i++] = "Abbott";
    objs[i++] = "";
    objs[i++] = new java.sql.Date(parseDate("2005-08-14").getTime());
    objs[i++] = "";
    objs[i++] = "Madera";
    objs[i++] = "IN_PROGRESS";
    objs[i++] = new java.sql.Date(parseDate("2019-02-12").getTime());
    objs[i++] = "RACFID";
    objs[i++] = "Anna";
    objs[i++] = "Smith";

    final List<Object[]> list = new ArrayList<>();
    list.add(objs);
    when(nq.getResultList()).thenReturn(list);

    target = new ReportFaultCANSClientIdsJob(sessionFactory, sessionFactory);
    target.initReport();

    cansClient = new CansClient();
    cansClient.id = 12345L;
  }

  @Test
  public void type() throws Exception {
    assertThat(ReportFaultCANSClientIdsJob.class, notNullValue());
  }

  @Ignore
  @Test(expected = NeutronRuntimeException.class)
  public void main_A$StringArray() throws Exception {
    final String[] args = new String[] {};
    bombResultSet();
    // runKillThreadWait(target, 1000L);
    ReportFaultCANSClientIdsJob.main(args);
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void mapNQResults_A$List$Class() throws Exception {
    final List objectArrayList = new ArrayList();
    final List<Object> actual =
        ReportFaultCANSClientIdsJob.mapNQResults(objectArrayList, CansClient.class);
    final List<Object> expected = new ArrayList();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getNQResultColumnAnnotatedFields_A$Class() throws Exception {
    final List<Field> actual =
        ReportFaultCANSClientIdsJob.getNQResultColumnAnnotatedFields(CansClient.class);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setObjectProperty_A$Object$Field$Object() throws Exception {
    Object object = cansClient;
    Field field = object.getClass().getDeclaredField("lastName");
    Object fieldValue = "Smith";
    ReportFaultCANSClientIdsJob.setObjectProperty(object, field, fieldValue);
  }

  @Test
  public void grabCansSession_A$() throws Exception {
    Session actual = target.grabCansSession();
    Session expected = session;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void grabCmsSession_A$() throws Exception {
    Session actual = target.grabCmsSession();
    Session expected = session;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void buildClientList_A$() throws Exception {
    target.buildClientList();
  }

  @Test
  public void generateReport_A$() throws Exception {
    final File fakeBaseDir = tempFolder.newFolder();
    target.setBaseDir(fakeBaseDir.getAbsolutePath());
    target.initReport();
    target.generateReport();
  }

  @Test
  public void initReport_A$() throws Exception {
    target.initReport();
  }

  @Test
  public void finalizeReport_A$() throws Exception {
    final File fakeBaseDir = tempFolder.newFolder();
    target.setBaseDir(fakeBaseDir.getAbsolutePath());
    target.initReport();
    target.generateReport();
    target.finalizeReport();
  }

  @Test
  public void isValidClientId_A$Object() throws Exception {
    final CansClient clientDto = new CansClient();
    clientDto.setCmsKey(DEFAULT_CLIENT_ID);
    clientDto.externalId = DEFAULT_CLIENT_ID;

    final boolean actual = target.isValidClientId(clientDto);
    final boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void attemptToFix_A$Object() throws Exception {
    final CansClient clientDto = new CansClient();
    clientDto.setCmsKey(DEFAULT_CLIENT_ID);
    clientDto.externalId = DEFAULT_CLIENT_ID;
    clientDto.comment = "Client Not found in CMS.";

    final List<Object> list = new ArrayList<>();
    list.add(DEFAULT_CLIENT_ID);
    when(nq.getResultList()).thenReturn(list);

    target.attemptToFix(clientDto);
  }

  @Test
  public void buildReportFileName_A$() throws Exception {
    final File fakeBaseDir = tempFolder.newFolder();
    target.setBaseDir(fakeBaseDir.getAbsolutePath());
    target.buildReportFileName();
  }

  @Test
  public void reportClient_A$Object() throws Exception {
    final File fakeBaseDir = tempFolder.newFolder();
    target.setBaseDir(fakeBaseDir.getAbsolutePath());
    target.initReport();

    final CansClient clientPojo = cansClient;
    clientPojo.comment = "Sink me!";
    target.reportClient(clientPojo);
  }

}
