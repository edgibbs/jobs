package gov.ca.cwds.jobs.test;

import java.io.FileReader;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

// ${workspace_loc}/jobs/src/test/resources/fixtures/arbitrary.json
public class CustomEsQuerySort {

  protected static class HitSorter
      implements Serializable, Comparable<HitSorter>, Comparator<HitSorter> {

    private static final long serialVersionUID = 1L;

    protected String id;
    protected String lastName;
    protected String firstMatch;
    protected JSONObject map; // raw hit

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected HitSorter(final JSONObject hit) {
      this.map = hit;
      this.id = (String) hit.get("_id");
      this.lastName = (String) ((Map) hit.get("_source")).get("last_name");
      final JSONArray matchedQueries = (JSONArray) hit.get("matched_queries");

      matchedQueries.sort((o1, o2) -> {
        return StringUtils.trimToEmpty((String) o1).compareTo(StringUtils.trimToEmpty((String) o2));
      });

      firstMatch = (String) matchedQueries.get(0);
    }

    @Override
    public int compare(HitSorter h1, HitSorter h2) {
      int ret = 0;

      ret =
          StringUtils.trimToEmpty(h1.firstMatch).compareTo(StringUtils.trimToEmpty(h2.firstMatch));
      if (ret == 0) {
        ret = StringUtils.trimToEmpty(h1.lastName).compareTo(StringUtils.trimToEmpty(h2.lastName));
      }

      return ret;
    }

    @Override
    public int compareTo(HitSorter o) {
      return compare(this, o);
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((firstMatch == null) ? 0 : firstMatch.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      HitSorter other = (HitSorter) obj;
      if (firstMatch == null) {
        if (other.firstMatch != null)
          return false;
      } else if (!firstMatch.equals(other.firstMatch))
        return false;
      if (id == null) {
        if (other.id != null)
          return false;
      } else if (!id.equals(other.id))
        return false;
      if (lastName == null) {
        if (other.lastName != null)
          return false;
      } else if (!lastName.equals(other.lastName))
        return false;
      return true;
    }

    @Override
    public String toString() {
      return "HitSorter [id=" + id + ", lastName=" + lastName + ", firstMatch=" + firstMatch + "]";
    }

  }

  public static void main(String[] args) throws Exception {
    final Object obj = new JSONParser().parse(new FileReader(args[0]));
    final JSONObject jo = (JSONObject) obj;
    final JSONObject outerHits = (JSONObject) jo.get("hits");
    final JSONArray hits = (JSONArray) outerHits.get("hits");

    final SortedSet<HitSorter> sortedHits = new TreeSet<>();
    for (Object object : hits) {
      final JSONObject o = (JSONObject) object;
      sortedHits.add(new HitSorter(o));
    }

    final JSONArray newHits = new JSONArray();
    for (HitSorter hs : sortedHits) {
      newHits.add(hs.map);
    }

    outerHits.put("hits", newHits);
    System.out.println(jo);
  }

}

