package gov.ca.cwds.neutron.rocket;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Queue;

import org.junit.Test;

import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.jobs.test.TestDenormalizedEntity;
import gov.ca.cwds.jobs.test.TestNormalizedEntity;
import gov.ca.cwds.neutron.util.NeutronThreadUtils;

public class NeutronManagedBlockerTest
    extends Goddard<TestNormalizedEntity, TestDenormalizedEntity> {

  Queue<TestNormalizedEntity> queue;
  NeutronManagedBlocker target;

  @Override
  public void setup() throws Exception {
    super.setup();

    queue = mock(Queue.class);
    when(queue.size()).thenReturn(100);
    NeutronThreadUtils.nameThread("test_extract_3");

    target = new NeutronManagedBlocker(queue, 10);
  }

  @Test
  public void type() throws Exception {
    assertThat(NeutronManagedBlocker.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void block_A$() throws Exception {
    boolean actual = target.block();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test(expected = InterruptedException.class)
  public void block_A$_T$InterruptedException() throws Exception {
    when(queue.size()).thenThrow(InterruptedException.class);
    target.block();
  }

  @Test
  public void isReleasable_A$() throws Exception {
    boolean actual = target.isReleasable();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getMaxSizeBeforeBlocking_A$() throws Exception {
    int actual = target.getMaxSizeBeforeBlocking();
    int expected = 10;
    assertThat(actual, is(equalTo(expected)));
  }

}

