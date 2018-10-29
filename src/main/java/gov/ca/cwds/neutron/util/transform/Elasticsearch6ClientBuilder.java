package gov.ca.cwds.neutron.util.transform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.ca.cwds.rest.ElasticsearchConfiguration;
import gov.ca.cwds.rest.api.ApiException;

public class Elasticsearch6ClientBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(Elasticsearch6ClientBuilder.class);

  private Elasticsearch6ClientBuilder() {}

  public RestHighLevelClient createAndConfigureESClient(ElasticsearchConfiguration config) {
    RestHighLevelClient client = null;
    try {
      client = new RestHighLevelClient(
          RestClient.builder(getHttpHosts(parseNodes(config.getElasticsearchNodes()))));
      return client;
    } catch (RuntimeException e) {
      LOGGER.error("Error initializing Elasticsearch client: {}", e.getMessage(), e);
      if (client != null) {
        try {
          client.close();
        } catch (IOException e1) {
          LOGGER.error("FAILED to close Elasticsearch client", e);
        }
      }
      throw new ApiException("Error initializing Elasticsearch client: " + e.getMessage(), e);
    }
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
        LOGGER.warn("There is an empty host for port {}", port);
      }
    }

    return nodesList.toArray(new HttpHost[0]);
  }

  @SuppressWarnings("fb-contrib:CLI_CONSTANT_LIST_INDEX")
  private int getPort(String[] hostPortPair) {
    return hostPortPair.length > 1 && hostPortPair[1] != null ? Integer.parseInt(hostPortPair[1])
        : -1;
  }

  private String getHost(String[] hostPortPair) {
    return hostPortPair.length > 0 ? hostPortPair[0] : "";
  }

}
