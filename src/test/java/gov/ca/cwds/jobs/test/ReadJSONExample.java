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
    final JSONObject jo = (JSONObject) obj;
    final JSONObject outerHits = (JSONObject) jo.get("hits");
    final JSONArray hits = (JSONArray) outerHits.get("hits");
    // System.out.println(hits);

    // Iterate hits:
    Iterator iter = hits.iterator();
    Iterator<Map.Entry> itr1;

    while (iter.hasNext()) {
      final Map map = (Map) iter.next();
      final JSONArray matchedQueries = (JSONArray) map.get("matched_queries");
      System.out.println(matchedQueries);

      // itr1 = ((Map) iter.next()).entrySet().iterator();
      // while (itr1.hasNext()) {
      // Map.Entry pair = itr1.next();
      // System.out.println(pair.getKey() + " : " + pair.getValue());
      // System.out.println(pair.getValue().getClass());
      // }
    }

    // class org.json.simple.JSONObject
    // Collections.sort(hits, c);

  }

}

