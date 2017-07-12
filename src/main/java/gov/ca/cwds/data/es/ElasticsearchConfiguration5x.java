package gov.ca.cwds.data.es;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

/**
 * Represents the configuration settings for {@link Elasticsearch5xDao}.
 *
 * @author CWDS API Team
 */
public class ElasticsearchConfiguration5x {

  @NotNull
  @JsonProperty("elasticsearch.host")
  private String host;

  @NotNull
  @JsonProperty("elasticsearch.port")
  private String port;

  @NotNull
  @JsonProperty("elasticsearch.cluster")
  private String cluster;

  @NotNull
  @JsonProperty("elasticsearch.alias")
  private String alias;

  @NotNull
  @JsonProperty("elasticsearch.doctype")
  private String docType;

  @JsonProperty("job.lis.reader.query")
  private String lisReaderQuery;

  @JsonProperty("elasticsearch.xpack.user")
  private String user;

  @JsonProperty("elasticsearch.xpack.password")
  private String password;

  /**
   * @return the elasticsearch host
   */
  public String getElasticsearchHost() {
    return host;
  }

  /**
   * @return the elasticsearch port
   */
  public String getElasticsearchPort() {
    return port;
  }

  /**
   * @return the elasticsearch cluster
   */
  public String getElasticsearchCluster() {
    return cluster;
  }

  /**
   * @return the elasticsearch index alias
   */
  public String getElasticsearchAlias() {
    return alias;
  }


  /**
   * @return the elasticsearch document type
   */
  public String getElasticsearchDocType() {
    return docType;
  }

  public String getLisReaderQuery() {
    return lisReaderQuery;
  }

  public String getUser() {
    return user;
  }

  public String getPassword() {
    return password;
  }
}
