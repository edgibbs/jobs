package gov.ca.cwds.data.persistence.cms.rep;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class ReplicatedSubstituteCareProviderTest {

  @Test
  public void testReplicationOperation() throws Exception {
    ReplicatedSubstituteCareProvider target = new ReplicatedSubstituteCareProvider();
    target.setReplicationOperation(CmsReplicationOperation.I);
    CmsReplicationOperation actual = target.getReplicationOperation();
    CmsReplicationOperation expected = CmsReplicationOperation.I;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void testReplicationDate() throws Exception {
    ReplicatedSubstituteCareProvider target = new ReplicatedSubstituteCareProvider();
    DateFormat fmt = new SimpleDateFormat("yyyy-mm-dd");
    Date date = fmt.parse("2012-10-31");
    target.setReplicationDate(date);
    Date actual = target.getReplicationDate();
    Date expected = fmt.parse("2012-10-31");
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void type() throws Exception {
    assertThat(ReplicatedSubstituteCareProvider.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    ReplicatedSubstituteCareProvider target = new ReplicatedSubstituteCareProvider();
    assertThat(target, notNullValue());
  }

  @Test
  public void getNormalizationClass_Args__() throws Exception {
    ReplicatedSubstituteCareProvider target = new ReplicatedSubstituteCareProvider();
    // given
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    Class<ReplicatedSubstituteCareProvider> actual = target.getNormalizationClass();
    // then
    // e.g. : verify(mocked).called();
    Class<ReplicatedSubstituteCareProvider> expected = ReplicatedSubstituteCareProvider.class;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void normalize_Args__Map() throws Exception {
    ReplicatedSubstituteCareProvider target = new ReplicatedSubstituteCareProvider();
    // given
    Map<Object, ReplicatedSubstituteCareProvider> map =
        new HashMap<Object, ReplicatedSubstituteCareProvider>();
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    ReplicatedSubstituteCareProvider actual = target.normalize(map);
    // then
    // e.g. : verify(mocked).called();
    ReplicatedSubstituteCareProvider expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getNormalizationGroupKey_Args__() throws Exception {
    ReplicatedSubstituteCareProvider target = new ReplicatedSubstituteCareProvider();
    // given
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    Object actual = target.getNormalizationGroupKey();
    // then
    // e.g. : verify(mocked).called();
    Object expected = "";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getLegacyId_Args__() throws Exception {
    ReplicatedSubstituteCareProvider target = new ReplicatedSubstituteCareProvider();
    // given
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    String actual = target.getLegacyId();
    // then
    // e.g. : verify(mocked).called();
    String expected = "";
    assertThat(actual, is(equalTo(expected)));
  }

}
