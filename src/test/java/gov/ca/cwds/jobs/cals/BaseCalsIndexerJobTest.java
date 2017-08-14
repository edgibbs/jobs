package gov.ca.cwds.jobs.cals;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * @author CWDS TPT-2
 */
public abstract class BaseCalsIndexerJobTest {

  protected static final String CALSNS_USER = "sa";
  protected static final String CALSNS_PASSWORD = "";
  protected static final String TIME_FILES_DIR = "./";

  protected static RestClient anonymousRestClient;
  protected static RestClient restClient;

  protected static String createTestDbFile(String schema) throws Exception {
    File temp = File.createTempFile(schema + "_db_", ".tmp");
    temp.deleteOnExit();
    return temp.getAbsolutePath();
  }

  @BeforeClass
  public static void initClients() {
    anonymousRestClient = RestClient.builder(
        new HttpHost("192.168.99.100", 9200, "http")).build();

    // create authorized REST client
    final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(AuthScope.ANY,
        new UsernamePasswordCredentials("elastic", "changeme"));
    restClient = RestClient.builder(new HttpHost("192.168.99.100", 9200))
        .setHttpClientConfigCallback(
            httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
        .build();
  }

  @AfterClass
  public static void closeClients() throws IOException {
    try {
      anonymousRestClient.close();
    } finally {
      restClient.close();
    }
  }

  protected String readResource(String file) throws URISyntaxException, IOException {
    URL url = Resources.getResource(file);
    return FileUtils.readFileToString(new File(url.toURI()), "UTF-8");
  }

  protected void assertTotalHits(Response response, int expectedTotalHits) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> jsonMap = mapper.readValue(response.getEntity().getContent(), Map.class);
    assertFalse(jsonMap.isEmpty());
    assertNotNull(jsonMap.get("hits"));
    Map<String, Object> hits = (Map<String, Object>) jsonMap.get("hits");
    assertEquals(expectedTotalHits, hits.get("total"));
  }
}
