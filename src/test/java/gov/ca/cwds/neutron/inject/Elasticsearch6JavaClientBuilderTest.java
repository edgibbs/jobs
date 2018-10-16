package gov.ca.cwds.neutron.inject;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.elasticsearch.client.transport.TransportClient;
import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.jobs.test.TestDenormalizedEntity;
import gov.ca.cwds.jobs.test.TestNormalizedEntity;

public class Elasticsearch6JavaClientBuilderTest
    extends Goddard<TestNormalizedEntity, TestDenormalizedEntity> {

  Elasticsearch6JavaClientBuilder target;

  @Before
  @Override
  public void setup() throws Exception {
    super.setup();

    target = new Elasticsearch6JavaClientBuilder();
  }

  @Test
  public void type() throws Exception {
    assertThat(Elasticsearch6JavaClientBuilder.class, notNullValue());
  }

  @Test
  public void buildElasticsearchClient_A$ElasticsearchConfiguration() throws Exception {
    TransportClient actual = target.buildElasticsearchClient(esConfig);
    TransportClient expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

}
