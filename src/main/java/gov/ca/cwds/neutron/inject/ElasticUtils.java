package gov.ca.cwds.neutron.inject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.elasticsearch.xpack.client.PreBuiltXPackTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.rest.ElasticsearchConfiguration;
import gov.ca.cwds.rest.api.ApiException;

/**
 * @author CWDS API Team
 */
public class ElasticUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(ElasticUtils.class);

  private ElasticUtils() {}

  public static TransportClient buildElasticsearchClient(final ElasticsearchConfiguration config) {
    TransportClient client = null;
    try {
      client = makeESTransportClient(config);

      for (TransportAddress address : getValidatedESNodes(config)) {
        client.addTransportAddress(address);
      }
      return client;
    } catch (Exception e) {
      LOGGER.error("Error initializing Elasticsearch client: {}", e.getMessage(), e);
      if (client != null) {
        client.close();
      }
      throw new ApiException(e);
    }
  }

  private static TransportClient makeESTransportClient(final ElasticsearchConfiguration config) {
    TransportClient client;
    final String cluster = config.getElasticsearchCluster();
    final String user = config.getUser();
    final String password = config.getPassword();
    final boolean secureClient = StringUtils.isNotBlank(user) && StringUtils.isNotBlank(password);

    final Settings.Builder settings = Settings.builder().put("cluster.name", cluster);
    settings.put("client.transport.sniff", true);

    if (secureClient) {
      LOGGER.info("ENABLE X-PACK - cluster: {}", cluster);
      settings.put("xpack.security.user", user + ":" + password);
      client = new PreBuiltXPackTransportClient(settings.build());
    } else {
      LOGGER.info("DISABLE X-PACK - cluster: {}", cluster);
      client = new PreBuiltTransportClient(settings.build());
    }

    return client;
  }

  @SuppressWarnings("fb-contrib:CLI_CONSTANT_LIST_INDEX")
  private static List<TransportAddress> getValidatedESNodes(ElasticsearchConfiguration config) {
    final List<TransportAddress> addressList = new ArrayList<>();
    final List<String> nodesList = new ArrayList<>();

    // If provided use Host and Port as a first node. For backward compatibility
    if (config.getElasticsearchHost() != null && config.getElasticsearchPort() != null) {
      nodesList
          .add(config.getElasticsearchHost().concat(":").concat(config.getElasticsearchPort()));
    }

    // Comma-separated List of host:port pairs provided in configuration file.
    // Example: host1:port1,host2:port2,...etc.
    if (StringUtils.isBlank(config.getElasticsearchNodes())) {
      nodesList.addAll(Arrays.asList(config.getElasticsearchNodes().trim().split(",")));
    }

    // Remove duplicates if any.
    final Map<String, String[]> nodesMap = new HashMap<>(nodesList.size());
    for (String node : nodesList) {
      nodesMap.put(node, node.split(":"));
    }

    for (Map.Entry<String, String[]> entry : nodesMap.entrySet()) {
      final String[] hostPort = entry.getValue();
      if (hostPort.length >= 2) {
        LOGGER.info("Adding new ES Node host:[{}] port:[{}] to elasticsearch client", hostPort[0],
            hostPort[1]);
        try {
          addressList.add(new TransportAddress(InetAddress.getByName(hostPort[0]),
              Integer.parseInt(hostPort[1])));
        } catch (UnknownHostException e) {
          throw CheeseRay.runtime(LOGGER, e, "ERROR ADDING ES NODE! {}", e.getMessage());
        } // end catch
      } // end if
    } // end for

    return addressList;
  }

}
