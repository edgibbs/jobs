package gov.ca.cwds.neutron.util.transform;

import java.io.File;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Stream utility functions, such as splitting a stream by every Nth element.
 * 
 * @author CWDS API Team
 */
public final class NeutronStreamUtils {

  private NeutronStreamUtils() {
    // static methods only
  }

  public static String getFilePath(String path) {
    return path.substring(0, path.lastIndexOf(File.separatorChar));
  }

  /**
   * Split a stream every Nth element.
   * 
   * @param n split size
   * @return element at that location
   * @param <T> stream element type
   */
  public static <T> Function<T, Stream<T>> everyNth(int n) {
    return new Function<T, Stream<T>>() {
      int i = 0;

      @Override
      public Stream<T> apply(T t) {
        return (i++ % n == 0) ? Stream.of(t) : Stream.empty();
      }
    };
  }

}
