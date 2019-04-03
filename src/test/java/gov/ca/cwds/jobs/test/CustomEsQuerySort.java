package gov.ca.cwds.jobs.test;

import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Prototype custom Elasticsearch result sorting class for SNAP-1022.
 * 
 * @author CWDS API Team
 */
public class CustomEsQuerySort {

  private static final String HITS = "hits";

  protected static class HitSorter
      implements Serializable, Comparable<HitSorter>, Comparator<HitSorter> {

    private static final long serialVersionUID = 1L;

    protected String id;
    protected String lastName;
    protected String firstName;

    protected String matchCategory;
    protected JSONObject map; // raw hit

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected HitSorter(final JSONObject hit) {
      this.map = hit;
      this.id = (String) hit.get("_id");
      this.lastName = trimToEmpty((String) ((Map) hit.get("_source")).get("last_name"));
      this.firstName = trimToEmpty((String) ((Map) hit.get("_source")).get("first_name"));
      final JSONArray matchedQueries = (JSONArray) hit.get("matched_queries");

      matchedQueries.sort((o1, o2) -> {
        return trimToEmpty((String) o1).compareTo(trimToEmpty((String) o2));
      });

      matchCategory = (String) matchedQueries.get(0);
    }

    @Override
    public int compare(HitSorter h1, HitSorter h2) {
      int ret = 0;

      ret = trimToEmpty(h1.matchCategory).compareTo(trimToEmpty(h2.matchCategory));
      if (ret == 0) {
        ret = trimToEmpty(h1.lastName).compareTo(trimToEmpty(h2.lastName));
      }
      if (ret == 0) {
        ret = trimToEmpty(h1.firstName).compareTo(trimToEmpty(h2.firstName));
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
      result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
      result = prime * result + ((matchCategory == null) ? 0 : matchCategory.hashCode());
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
      if (firstName == null) {
        if (other.firstName != null)
          return false;
      } else if (!firstName.equals(other.firstName))
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
      if (matchCategory == null) {
        if (other.matchCategory != null)
          return false;
      } else if (!matchCategory.equals(other.matchCategory))
        return false;
      return true;
    }

    @Override
    public String toString() {
      return "HitSorter [id=" + id + ", lastName=" + lastName + ", firstName=" + firstName
          + ", matchCategory=" + matchCategory + "]";
    }

  }

  public void parse(String fileLocation) throws IOException, ParseException {
    try (final Reader r = new FileReader(fileLocation)) {
      final Object obj = new JSONParser().parse(r);
      final JSONObject jo = (JSONObject) obj;
      final JSONObject outerHits = (JSONObject) jo.get(HITS);
      final JSONArray hits = (JSONArray) outerHits.get(HITS);

      final SortedSet<HitSorter> sortedHits = new TreeSet<>();
      for (Object object : hits) {
        final JSONObject o = (JSONObject) object;
        sortedHits.add(new HitSorter(o));
      }

      final JSONArray newHits = new JSONArray();
      for (HitSorter hs : sortedHits) {
        newHits.add(hs.map);
      }

      outerHits.put(HITS, newHits);
      System.out.println(jo);
    } finally {
      // Reader closes automatically
    }
  }

  // ${workspace_loc}/jobs/src/test/resources/fixtures/arbitrary.json
  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws Exception {
    final CustomEsQuerySort sorter = new CustomEsQuerySort();
    sorter.parse(args[0]);
  }

}

