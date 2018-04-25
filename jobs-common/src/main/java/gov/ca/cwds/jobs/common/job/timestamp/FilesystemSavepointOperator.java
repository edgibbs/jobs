package gov.ca.cwds.jobs.common.job.timestamp;

import com.google.inject.Inject;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import gov.ca.cwds.jobs.common.Constants;
import gov.ca.cwds.jobs.common.identifier.ChangedEntityIdentifier;
import gov.ca.cwds.jobs.common.inject.LastRunDir;
import gov.ca.cwds.rest.api.ApiException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Alexander Serbin on 2/5/2018.
 */
public class FilesystemSavepointOperator implements SavepointOperator {

  private static final Logger LOG = LoggerFactory.getLogger(FilesystemSavepointOperator.class);

  private static final String TIMESTAMP_FILENAME = "LastJobRun.time";
  private String outputDir;

  @Inject
  public FilesystemSavepointOperator(@LastRunDir String outputDir) {
    this.outputDir = outputDir;
  }

  @Override
  public boolean savepointExists() {
    return getRunningFile().toFile().exists();
  }

  @Override
  public ChangedEntityIdentifier readSavepoint() {
    try {
      String savepointString = readSavepoint(getRunningFile());
      if (savepointString != null) {
        String[] strings = savepointString.split(",");
      }

      if (StringUtils.isNumeric(savepointString)) {
        savepointId = Integer.parseInt(savepointString);
      } else {
        savepointTimestamp = LocalDateTime.parse(savepointString, Constants.DATE_TIME_FORMATTER);
      }
      ChangedEntityIdentifier savepoint = new ChangedEntityIdentifier(savepointId, null, );
      return savepoint;
    } catch (IOException e) {
      throw new ApiException("Can't read savepoint from the file", e);
    }
  }

  @Override
  public void writeSavepoint(ChangedEntityIdentifier savepoint) {
    if (savepoint == null) {
      LOG.info("Savepoint is empty for the batch and will not be recorded");
      return;
    }
    String savepointString = timestamp.format(Constants.DATE_TIME_FORMATTER);
    writeSavepoint(savepointString);
  }

  @Override
  public Integer readId() {
    try {
      return Integer.parseInt(readSavepoint(getRunningFile()));
    } catch (IOException e) {
      throw new ApiException("Can't reade savepoint Id from the file", e);
    }
  }

  @Override
  public void writeId(Integer id) {
    if (id == null) {
      LOG.info("Id is empty for the batch and will not be recorded");
      return;
    }
    writeSavepoint(id.toString());
  }

  @SuppressFBWarnings("PATH_TRAVERSAL_IN") //Path cannot be controlled by the user
  private Path getRunningFile() {
    Path path = Paths.get(outputDir, TIMESTAMP_FILENAME).normalize().toAbsolutePath();
    LOG.info("Path to the savepoint file: {}", path.toString());
    return path;
  }

  private String readSavepoint(Path runningFile) throws IOException {
    try (Stream<String> stream = Files.lines(runningFile)) {
      Optional<String> firstLine = stream.findFirst();
      if (!firstLine.isPresent()) {
        throw new ApiException("Corrupted date file: " + runningFile);
      }
      return firstLine.get();
    }
  }

  private void writeSavepoint(String savepoint) {
    try {
      if (savepointExists()) {
        FileUtils.forceDelete(getRunningFile().toFile());
      }
      if (getRunningFile().toFile().createNewFile()) {
        FileUtils.writeStringToFile(getRunningFile().toFile(), savepoint, "UTF-8");
      } else {
        throw new ApiException("Can't create the file " + getRunningFile().normalize().toString());
      }
    } catch (IOException e) {
      throw new ApiException("Can't write savepoint ", e);
    }
  }
}
