package gov.ca.cwds.jobs.test;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool.ManagedBlocker;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;

import gov.ca.cwds.data.std.ApiObjectIdentity;
import gov.ca.cwds.neutron.enums.NeutronElasticsearchDefaults;
import gov.ca.cwds.neutron.util.shrinkray.NeutronStringUtils;

public class NeutronPlayground {

  private static final Logger LOGGER = LoggerFactory.getLogger(NeutronPlayground.class);

  public static class Item extends ApiObjectIdentity {

    private static final long serialVersionUID = 1L;

    private String name;
    private int qty;
    private BigDecimal price;

    public Item(String name, int qty, BigDecimal price) {
      super();
      this.name = name;
      this.qty = qty;
      this.price = price;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public int getQty() {
      return qty;
    }

    public void setQty(int qty) {
      this.qty = qty;
    }

    public BigDecimal getPrice() {
      return price;
    }

    public void setPrice(BigDecimal price) {
      this.price = price;
    }

    // constructors, getter/setters
  }

  public void streamTest1() {
    // 3 apple, 2 banana, others 1.
    final List<String> items =
        Arrays.asList("apple", "apple", "banana", "apple", "orange", "banana", "papaya");
    final Map<String, Long> result =
        items.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    final Map<String, Long> finalMap = new LinkedHashMap<>();

    // Sort a map and add to finalMap
    result.entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed())
        .forEachOrdered(e -> finalMap.put(e.getKey(), e.getValue()));

    LOGGER.info("streamTest1(): result: {}", finalMap);
  }

  public void streamTest2() {
    // 3 apple, 2 banana, others 1
    final List<Item> items = Arrays.asList(new Item("apple", 10, new BigDecimal("9.99")),
        new Item("banana", 20, new BigDecimal("19.99")),
        new Item("orang", 10, new BigDecimal("29.99")),
        new Item("watermelon", 10, new BigDecimal("29.99")),
        new Item("papaya", 20, new BigDecimal("9.99")),
        new Item("apple", 10, new BigDecimal("9.99")),
        new Item("banana", 10, new BigDecimal("19.99")),
        new Item("apple", 20, new BigDecimal("9.99")));

    // group by price
    final Map<BigDecimal, List<Item>> groupByPriceMap =
        items.stream().collect(Collectors.groupingBy(Item::getPrice));
    System.out.println(groupByPriceMap);

    // group by price, uses 'mapping' to convert List<Item> to Set<String>
    final Map<BigDecimal, Set<String>> result = items.stream().collect(Collectors
        .groupingBy(Item::getPrice, Collectors.mapping(Item::getName, Collectors.toSet())));

    LOGGER.info("streamTest2(): result: {}", result);
  }

  public void junk() throws Exception {
    final String json = IOUtils.resourceToString(
        NeutronElasticsearchDefaults.SETTINGS_PEOPLE_SUMMARY.getValue(), Charset.defaultCharset());
    System.out.println(json);

    final Map<String, Object> map = NeutronStringUtils.jsonToMap(json);
    final Integer replicas = (Integer) map.get("number_of_replicas");
    final String refreshInterval = (String) map.get("refresh_interval");
    LOGGER.info("number_of_replicas: {}, refresh_interval: {}\nmap: {}", replicas, refreshInterval,
        map);

    // playground.streamTest1();
    // playground.streamTest2();
    // final Instant inst = Instant.ofEpochSecond(1_280_512_800L);
    // LOGGER.info("instant: {}", inst);
  }

  public static interface Lame extends Callable<Boolean> {

  }

  public static class TestManagedBlocker implements ManagedBlocker {

    private final int maxQueueSizeBeforeBlocking;
    private Queue<String> queue;

    public TestManagedBlocker(Queue<String> queue, int maxSizeBeforeBlocking) {
      this.queue = queue;
      this.maxQueueSizeBeforeBlocking = maxSizeBeforeBlocking;
    }

    @Override
    public boolean block() throws InterruptedException {
      return false;
    }

    @Override
    public boolean isReleasable() {
      return queue.size() < maxQueueSizeBeforeBlocking;
    }

    public int getMaxSizeBeforeBlocking() {
      return maxQueueSizeBeforeBlocking;
    }

  }

  public static class Foo implements Lame {

    @Override
    public Boolean call() throws Exception {
      LOGGER.info("callable: start");
      Thread.currentThread().yield();
      Thread.sleep(1000L);
      LOGGER.info("callable: end");
      return true; // do something useful here
    }

  };

  public void testRetry(long delayBeforeKill) {
    final ExecutorService executor = Executors.newWorkStealingPool(2);
    final TimeLimiter limiter = SimpleTimeLimiter.create(executor);
    final Foo target = new Foo();
    final Lame proxy = limiter.newProxy(target, Lame.class, delayBeforeKill, TimeUnit.MILLISECONDS);

    try {
      proxy.call();
    } catch (Exception e) {
      LOGGER.error("DIED", e);
    }

  }

  public static void main(String[] args) throws Exception {
    // final NeutronPlayground playground = new NeutronPlayground();
    // playground.testRetry(500);
    // playground.testRetry(2000);

    final Queue<String> queue = new ConcurrentLinkedQueue<>();
    final TestManagedBlocker blocker = new TestManagedBlocker(queue, 10000);

    for (int i = 1; i <= 1000; i++) {
      queue.add("abc" + i);
    }

    System.out.print("releasable: " + blocker.isReleasable());

  }

}
