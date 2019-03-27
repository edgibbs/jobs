package gov.ca.cwds.jobs.test;

import java.io.FileReader;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

// ${workspace_loc}/jobs/src/test/resources/fixtures/arbitrary.json
public class ReadJSONExample {

  protected static class HitSorter
      implements Serializable, Comparable<HitSorter>, Comparator<HitSorter> {

    private static final long serialVersionUID = 1L;

    protected String id;
    protected String lastName;
    protected String firstMatch;

    @SuppressWarnings("unchecked")
    protected HitSorter(final Map map) {
      this.id = (String) map.get("_id");
      this.lastName = (String) map.get("last_name");
      final JSONArray matchedQueries = (JSONArray) map.get("matched_queries");

      matchedQueries.sort((o1, o2) -> {
        final String s1 = (String) o1;
        final String s2 = (String) o2;
        return StringUtils.trimToEmpty(s1).compareTo(StringUtils.trimToEmpty(s2));
      });

      // System.out.println(matchedQueries);
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
    // System.out.println(hits);

    final SortedSet<HitSorter> sortedHits = new TreeSet<>();

    // Iterate hits:
    for (Iterator iter = hits.iterator(); iter.hasNext();) {
      final Map map = (Map) iter.next();
      sortedHits.add(new HitSorter(map));
    }

    System.out.println(sortedHits);
  }

}

