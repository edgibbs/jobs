package gov.ca.cwds.data.es;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.any;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.ca.cwds.data.persistence.cms.client.RawClient;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.rest.ElasticsearchConfiguration;

public class NeutronElasticSearchDaoTest
    extends Goddard<RawClient, ApiGroupNormalizer<ReplicatedClient>> {

  private static final String TEST_IDX = "people-summary_2018.10.31.15.01.11";

  private static final String FILE_JSON_SETTINGS =
      "/neutron/elasticsearch/setting/people-index-settings.json";
  private static final String FILE_JSON_MAPPING =
      "/neutron/elasticsearch/mapping/map_person_summary.json";

  String id = DEFAULT_CLIENT_ID;
  String index = TEST_IDX;
  String type = "person-summary";
  String settingsJsonFile = FILE_JSON_SETTINGS;
  String mappingJsonFile = FILE_JSON_MAPPING;
  String alias = "people-summary";
  int numShards = 1;
  int numReplicas = 0;
  String indexOrAlias = TEST_IDX;

  NeutronElasticSearchDao target;

  @Before
  @Override
  public void setup() throws Exception {
    super.setup();
    target = new NeutronElasticSearchDao(client, esConfig);
  }

  @Test
  public void type() throws Exception {
    assertThat(NeutronElasticSearchDao.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void getConfig_A$() throws Exception {
    final ElasticsearchConfiguration actual = target.getConfig();
    assertThat(actual, is(notNullValue()));
  }

  @Test(expected = NeutronCheckedException.class)
  public void createIndex_A$String$int$int() throws Exception {
    target.createIndex(index, numShards, numReplicas);
  }

  @Test(expected = NeutronCheckedException.class)
  public void createIndex_A$String$int$int_T$IOException() throws Exception {
    target.createIndex(index, numShards, numReplicas);
  }

  @Test(expected = NeutronCheckedException.class)
  public void createIndex_A$String$String$String$String() throws Exception {
    target.createIndex(index, type, settingsJsonFile, mappingJsonFile);
  }

  @Test(expected = NeutronCheckedException.class)
  public void createIndex_A$String$String$String$String_T$NeutronCheckedException()
      throws Exception {
    target.createIndex(index, type, settingsJsonFile, mappingJsonFile);
  }

  @Test
  public void createIndexIfNeeded_A$String$String$String$String() throws Exception {
    target.createIndexIfNeeded(index, type, settingsJsonFile, mappingJsonFile);
  }

  @Test(expected = NeutronCheckedException.class)
  public void createIndexIfNeeded_A$String$String$String$String_T$NeutronCheckedException()
      throws Exception {
    when(client.indices()).thenThrow(IOException.class);
    target.createIndexIfNeeded(index, type, settingsJsonFile, mappingJsonFile);
  }

  @Test
  public void createIndexIfNeeded_A$String() throws Exception {
    target.createIndexIfNeeded(index);
  }

  @Test(expected = NeutronCheckedException.class)
  public void deleteIndex_A$String() throws Exception {
    target.deleteIndex(index);
  }

  @Test(expected = NeutronCheckedException.class)
  public void deleteIndex_A$String_T$NeutronCheckedException() throws Exception {
    target.deleteIndex(index);
  }

  @Test
  @Ignore
  public void createOrSwapAlias_A$String$String() throws Exception {
    boolean actual = target.createOrSwapAlias(alias, index);
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test(expected = NeutronCheckedException.class)
  public void createOrSwapAlias_A$String$String_T$NeutronCheckedException() throws Exception {
    when(client.indices()).thenThrow(IOException.class);
    target.createOrSwapAlias(alias, index);
  }

  @Test
  public void doesIndexExist_A$String() throws Exception {
    boolean actual = target.doesIndexExist(indexOrAlias);
    boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test(expected = NeutronCheckedException.class)
  public void doesIndexExist_A$String_T$NeutronCheckedException() throws Exception {
    when(client.indices()).thenThrow(IOException.class);
    final String indexOrAlias = TEST_IDX;
    target.doesIndexExist(indexOrAlias);
  }

  @Test
  public void doesAliasExist_A$String() throws Exception {
    final boolean actual = target.doesAliasExist(alias);
    final boolean expected = true;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test(expected = NeutronCheckedException.class)
  public void doesAliasExist_A$String_T$NeutronCheckedException() throws Exception {
    when(client.indices()).thenThrow(IOException.class);
    target.doesAliasExist(alias);
  }

  @Test
  public void bulkAdd_A$ObjectMapper$String$Object() throws Exception {
    final Object obj = "hello world";
    final IndexRequest actual = target.bulkAdd(mapper, id, obj);
    assertThat(actual, is(notNullValue()));
  }

  @Test(expected = JsonProcessingException.class)
  public void bulkAdd_A$ObjectMapper$String$Object_T$JsonProcessingException() throws Exception {
    when(mapper.writeValueAsBytes(any(Object.class))).thenThrow(JsonProcessingException.class);
    final Object obj = "hello world";
    target.bulkAdd(mapper, id, obj);
  }

  @Test
  public void bulkDelete_A$String() throws Exception {
    final DeleteRequest actual = target.bulkDelete(id);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void close_A$() throws Exception {
    target.close();
  }

  @Test(expected = IOException.class)
  public void close_A$_T$IOException() throws Exception {
    doThrow(IOException.class).when(client).close();
    target.close();
  }

  @Test
  public void getClient_A$() throws Exception {
    final RestHighLevelClient actual = target.getClient();
    assertThat(actual, is(notNullValue()));
  }

}