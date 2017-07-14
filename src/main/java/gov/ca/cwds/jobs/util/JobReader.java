package gov.ca.cwds.jobs.util;

/**
 * @author CWDS TPT-2
 * 
 * @param <T> type to read into
 */
public interface JobReader<T> extends JobComponent {

  /**
   *
   * @return extracted object, MUST return null when done
   * @throws Exception if unable to read from source
   */
  T read() throws Exception;

}
