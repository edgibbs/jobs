package gov.ca.cwds.jobs.test;

import java.io.FileReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

// ${workspace_loc}/jobs/src/test/resources/fixtures/arbitrary.json
public class ReadJSONExample {

  public static void main(String[] args) throws Exception {
    final Object obj = new JSONParser().parse(new FileReader(args[0]));

    // typecasting obj to JSONObject
    final JSONObject jo = (JSONObject) obj;
    final JSONObject outerHits = (JSONObject) jo.get("hits");

    // getting firstName and lastName
    // final String firstName = (String) jo.get("firstName");
    // final String lastName = (String) jo.get("lastName");
    //
    // System.out.println(firstName);
    // System.out.println(lastName);
    //
    // // getting age
    // final Long age = (Long) jo.get("age");
    // System.out.println(age);

    // getting address
    // final Map hits = ((Map) outerHits.get("hits"));
    //
    // // iterating address Map
    // Iterator<Map.Entry> itr1 = hits.entrySet().iterator();
    // while (itr1.hasNext()) {
    // Map.Entry pair = itr1.next();
    // System.out.println(pair.getKey() + " : " + pair.getValue());
    // }

    // getting phoneNumbers
    final JSONArray hits = (JSONArray) outerHits.get("hits");
    System.out.println(hits);

    // // iterating phoneNumbers
    // Iterator itr2 = ja.iterator();
    // Iterator<Map.Entry> itr1;
    //
    // while (itr2.hasNext()) {
    // itr1 = ((Map) itr2.next()).entrySet().iterator();
    // while (itr1.hasNext()) {
    // Map.Entry pair = itr1.next();
    // System.out.println(pair.getKey() + " : " + pair.getValue());
    // }
    // }

  }

}

