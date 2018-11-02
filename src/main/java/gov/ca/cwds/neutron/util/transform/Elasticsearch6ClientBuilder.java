package gov.ca.cwds.neutron.util.transform;

import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.rest.ElasticsearchConfiguration;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Elasticsearch6ClientBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(Elasticsearch6ClientBuilder.class);

  public Elasticsearch6ClientBuilder() {
    // Default, no-op
  }

  public RestHighLevelClient createAndConfigureESClient(ElasticsearchConfiguration config)
      throws NeutronCheckedException {
    RestHighLevelClient ret = null;
    try {
      HttpHost[] httpHosts = getHttpHosts(parseNodes(config.getElasticsearchNodes()));
      final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
      for (HttpHost httpHost : httpHosts) {
        credentialsProvider.setCredentials(
            new AuthScope(httpHost.getHostName(), httpHost.getPort(), null, null),
            new UsernamePasswordCredentials(config.getUser(), config.getPassword()));
      }
      ret = new RestHighLevelClient(RestClient.builder(httpHosts).setHttpClientConfigCallback(
          httpClientBuilder -> httpClientBuilder
              .setDefaultCredentialsProvider(credentialsProvider)));

    } catch (Exception e) {
      LOGGER.error("ERROR INITIALIZING ELASTICSEARCH CLIENT: {}", e.getMessage(), e);
      if (ret != null) {
        try {
          ret.close();
        } catch (IOException e1) {
          LOGGER.error("FAILED TO CLOSE ELASTICSEARCH CLIENT", e);
        }
      }

      throw CheeseRay.checked(LOGGER, e, "FAILED TO CREATE ELASTICSEARCH CLIENT! {}",
          e.getMessage(), e);
    }

    return ret;
  }

  protected List<String> parseNodes(String delimNodes) {
    return Arrays.stream(delimNodes.trim().split(",")).map(String::trim)
        .collect(Collectors.toList());
  }

  public HttpHost[] getHttpHosts(List<String> nodes) {
    final List<HttpHost> nodesList = new ArrayList<>();
    for (String node : nodes) {
      final String[] hostPortPair = node.split(":");
      final String host = getHost(hostPortPair);
      final int port = getPort(hostPortPair);
      if (StringUtils.isNotEmpty(host)) {
        nodesList.add(new HttpHost(host, port));
      } else {
        LOGGER.warn("EMPTY HOST FOR PORT {}", port);
      }
    }

    return nodesList.toArray(new HttpHost[0]);
  }

  @SuppressWarnings("fb-contrib:CLI_CONSTANT_LIST_INDEX")
  private int getPort(String[] hostPortPair) {
    return hostPortPair.length > 1 && StringUtils.isNotEmpty(hostPortPair[1])
        ? Integer.parseInt(hostPortPair[1].trim())
        : -1;
  }

  private String getHost(String[] hostPortPair) {
    return hostPortPair.length > 0 ? hostPortPair[0].trim() : "";
  }

}
