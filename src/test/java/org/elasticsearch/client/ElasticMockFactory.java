package org.elasticsearch.client;

public class ElasticMockFactory {

  RestHighLevelClient client;

  public ElasticMockFactory(RestHighLevelClient client) {
    this.client = client;
  }

  public IndicesClient makeIndicesClient() {
    return new IndicesClient(client);
  }

}
