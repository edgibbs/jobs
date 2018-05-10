package gov.ca.cwds.neutron.atom;

import java.util.List;

import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.neutron.jetpack.CheeseRay;
import gov.ca.cwds.neutron.util.transform.EntityNormalizer;

/**
 * Normalization features and settings for Elasticsearch indexing jobs.
 * 
 * @author CWDS API Team
 * @param <T> normalized type
 * @param <M> denormalized type
 */
public interface AtomTransform<T extends PersistentObject, M extends ApiGroupNormalizer<?>>
    extends AtomShared {

  /**
   * Transform (normalize) in the rocket instead of relying on the transformation thread.
   * 
   * @return true if the transformer thread should run
   */
  default boolean useTransformThread() {
    return true;
  }

  /**
   * Return the rocket's entity class for its denormalized source view or materialized query table,
   * if any, or null if not using a denormalized source.
   * 
   * @return entity class of view or materialized query table
   */
  default Class<? extends ApiGroupNormalizer<? extends PersistentObject>> getDenormalizedClass() {
    return null;
  }

  /**
   * True if the Rocket class reduces denormalized results to normalized ones.
   * 
   * @return true if class overrides {@link #normalize(List)}
   */
  default boolean isViewNormalizer() {
    return getDenormalizedClass() != null;
  }

  /**
   * Default normalize method just returns the input. Child classes may customize this method to
   * normalize denormalized result sets (view records) to normalized entities (parent/child)
   * records.
   * 
   * @param recs entity records
   * @return unmodified entity records
   * @see EntityNormalizer
   */
  @SuppressWarnings("unchecked")
  default List<T> normalize(List<M> recs) {
    return (List<T>) recs;
  }

  /**
   * Increment normalized record count.
   */
  default void incrementNormalizeCount() {
    CheeseRay.logEvery(getFlightLog().incrementNormalized(), "Normalize", "single");
  }

  /**
   * Normalize view records for a single grouping (such as all the same client) into a normalized
   * entity bean, consisting of a parent object and its child objects.
   * 
   * @param recs denormalized view beans
   * @return normalized entity bean instance
   */
  default T normalizeSingle(List<M> recs) {
    incrementNormalizeCount();
    final List<T> list = normalize(recs);
    return list != null && !list.isEmpty() ? list.get(0) : null;
  }

}
