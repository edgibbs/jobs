package gov.ca.cwds.data.persistence.cms.rep;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.io.Serializable;
import java.util.Date;

import org.junit.Test;

import gov.ca.cwds.data.es.ElasticSearchLegacyDescriptor;

public class CmsReplicatedEntityTest {

  private static class TestCmsReplicatedEntity implements CmsReplicatedEntity {

    private EmbeddableCmsReplicatedEntity embedded = new EmbeddableCmsReplicatedEntity();

    @Override
    public Serializable getPrimaryKey() {
      return null;
    }

    @Override
    public String getId() {
      return null;
    }

    @Override
    public String getLegacyId() {
      return null;
    }

    @Override
    public ElasticSearchLegacyDescriptor getLegacyDescriptor() {
      return new ElasticSearchLegacyDescriptor(getLegacyId(), getLegacyId(), new Date().toString(),
          "GOOBER_T", "test table");
    }

    @Override
    public EmbeddableCmsReplicatedEntity getReplicatedEntity() {
      return embedded;
    }

    public EmbeddableCmsReplicatedEntity getEmbedded() {
      return embedded;
    }

    public void setEmbedded(EmbeddableCmsReplicatedEntity embedded) {
      this.embedded = embedded;
    }

  }

  @Test
  public void type() throws Exception {
    assertThat(CmsReplicatedEntity.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    CmsReplicatedEntity target = new TestCmsReplicatedEntity();
    assertThat(target, notNullValue());
  }

  @Test
  public void isDelete_Args__CmsReplicatedEntity() throws Exception {
    CmsReplicatedEntity t = mock(CmsReplicatedEntity.class);
    boolean actual = CmsReplicatedEntity.isDelete(t);
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void isDelete_Args__CmsReplicatedEntity_null() throws Exception {
    CmsReplicatedEntity t = null;
    boolean actual = CmsReplicatedEntity.isDelete(t);
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getReplicationOperation_Args__() throws Exception {
    CmsReplicatedEntity target = new TestCmsReplicatedEntity();
    CmsReplicationOperation actual = target.getReplicationOperation();
    CmsReplicationOperation expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getReplicationOperation_null() throws Exception {
    TestCmsReplicatedEntity target = new TestCmsReplicatedEntity();
    target.setEmbedded(null);
    CmsReplicationOperation actual = target.getReplicationOperation();
    CmsReplicationOperation expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getReplicationDate_Args__() throws Exception {
    CmsReplicatedEntity target = new TestCmsReplicatedEntity();
    Date actual = target.getReplicationDate();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getReplicationDate_Args__null() throws Exception {
    TestCmsReplicatedEntity target = new TestCmsReplicatedEntity();
    target.setEmbedded(null);
    Date actual = target.getReplicationDate();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setReplicationOperation_Args__CmsReplicationOperation() throws Exception {
    CmsReplicatedEntity target = new TestCmsReplicatedEntity();
    CmsReplicationOperation replicationOperation = CmsReplicationOperation.U;
    target.setReplicationOperation(replicationOperation);
  }

  @Test
  public void setReplicationDate_Args__Date() throws Exception {
    CmsReplicatedEntity target = new TestCmsReplicatedEntity();
    Date replicationDate = new Date();
    target.setReplicationDate(replicationDate);
  }

  @Test
  public void setReplicationDate_Args__Date_null() throws Exception {
    CmsReplicatedEntity target = new TestCmsReplicatedEntity();
    target.setReplicationDate(null);
    Date replicationDate = new Date();
    target.setReplicationDate(replicationDate);
  }

}
