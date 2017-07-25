package gov.ca.cwds.jobs;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.jobs.util.AsyncReadWriteJob;

/**
 * @author CWDS TPT-2
 */
public class AsyncReadWriteJobTest {

  private List<Integer> input = new ArrayList<>();
  private List<String> output = new ArrayList<>();

  @Before
  public void before() {
    input.add(0);
    input.add(1);
    input.add(2);
  }

  @After
  public void after() {
    input.clear();
    output.clear();
  }

  public static class Input implements PersistentObject {
    private Integer id;

    public Input(Integer id) {
      this.id = id;
    }

    @Override
    public Integer getPrimaryKey() {
      return id;
    }
  }

  @Test
  public void genericTest() {
    AsyncReadWriteJob job = new AsyncReadWriteJob(() -> {
      if (!input.isEmpty()) {
        return input.remove(0);
      }
      return null;
    }, String::valueOf, output::addAll);
    job.run();
    Assert.assertEquals(3, output.size());
    for (int i = 0; i < output.size(); i++) {
      Assert.assertEquals(String.valueOf(i), output.get(i));
    }
  }

  @Test
  public void testReaderException() {
    AsyncReadWriteJob job = new AsyncReadWriteJob(() -> {
      if (input.size() == 3) {
        return input.remove(0);
      } else {
        try {
          Thread.sleep(1000); // NOSONAR
          throw new RuntimeException("failed on second!");
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }, String::valueOf, output::addAll

    );
    job.run();
    Assert.assertEquals(1, output.size());
    for (int i = 0; i < output.size(); i++) {
      Assert.assertEquals(String.valueOf(i), output.get(i));
    }
  }
}
