package gov.ca.cwds.jobs.test;

import java.io.FileReader;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

// ${workspace_loc}/jobs/src/test/resources/fixtures/arbitrary.json
public class ReadJSONExample {

  public static void main(String[] args) throws Exception {
    final Object obj = new JSONParser().parse(new FileReader(args[0]));

    // typecasting obj to JSONObject
    final JSONObject jo = (JSONObject) obj;

    // getting firstName and lastName
    final String firstName = (String) jo.get("firstName");
    final String lastName = (String) jo.get("lastName");

    System.out.println(firstName);
    System.out.println(lastName);

    // getting age
    final long age = (long) jo.get("age");
    System.out.println(age);

    // getting address
    final Map address = ((Map) jo.get("address"));

    // iterating address Map
    Iterator<Map.Entry> itr1 = address.entrySet().iterator();
    while (itr1.hasNext()) {
      Map.Entry pair = itr1.next();
      System.out.println(pair.getKey() + " : " + pair.getValue());
    }

    // getting phoneNumbers
    final JSONArray ja = (JSONArray) jo.get("phoneNumbers");

    // iterating phoneNumbers
    Iterator itr2 = ja.iterator();

    while (itr2.hasNext()) {
      itr1 = ((Map) itr2.next()).entrySet().iterator();
      while (itr1.hasNext()) {
        Map.Entry pair = itr1.next();
        System.out.println(pair.getKey() + " : " + pair.getValue());
      }
    }
  }

}

