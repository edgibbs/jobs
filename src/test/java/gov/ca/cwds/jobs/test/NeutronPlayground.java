package gov.ca.cwds.jobs.test;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.ca.cwds.data.std.ApiObjectIdentity;

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

  public void jsonToMap(String json) {
    try {
      final ObjectMapper mapper = new ObjectMapper();

      // convert JSON string to Map
      final Map<String, Object> map =
          mapper.readValue(json, new TypeReference<Map<String, String>>() {});
      System.out.println(map);

    } catch (JsonGenerationException e) {
      e.printStackTrace();
    } catch (JsonMappingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    final NeutronPlayground playground = new NeutronPlayground();
    final String json = "{\"name\":\"dork\", \"age\":97}";
    playground.jsonToMap(json);

    // playground.streamTest1();
    // playground.streamTest2();

    final Instant inst = Instant.ofEpochSecond(1_280_512_800L);
    LOGGER.info("instant: {}", inst);
  }

}
