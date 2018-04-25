package gov.ca.cwds.jobs.common.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import gov.ca.cwds.jobs.common.BaseJobConfiguration;
import gov.ca.cwds.jobs.common.batch.JobBatchIterator;
import gov.ca.cwds.jobs.common.batch.JobBatchIteratorImpl;
import gov.ca.cwds.jobs.common.config.JobOptions;
import gov.ca.cwds.jobs.common.job.JobPreparator;
import gov.ca.cwds.jobs.common.job.timestamp.FilesystemSavepointOperator;
import gov.ca.cwds.jobs.common.job.timestamp.SavepointOperator;

/**
 * Created by Alexander Serbin on 3/4/2018.
 */
public abstract class AbstractBaseJobModule extends AbstractModule {

  private JobOptions jobOptions;

  private Class<? extends JobPreparator> jobPreparatorClass = DefaultJobPreparator.class;

  private Class<? extends JobBatchIterator> jobBatchIteratorClass = JobBatchIteratorImpl.class;

  private AbstractModule elasticSearchModule;

  public AbstractBaseJobModule(String[] args) {
    this.jobOptions = JobOptions.parseCommandLine(args);
  }

  public void setJobPreparatorClass(Class<? extends JobPreparator> jobPreparatorClass) {
    this.jobPreparatorClass = jobPreparatorClass;
  }

  public void setElasticSearchModule(AbstractModule elasticSearchModule) {
    this.elasticSearchModule = elasticSearchModule;
  }

  public void setJobBatchIteratorClass(
      Class<? extends JobBatchIterator> jobBatchIteratorClass) {
    this.jobBatchIteratorClass = jobBatchIteratorClass;
  }

  public Class<? extends JobBatchIterator> getJobBatchIteratorClass() {
    return jobBatchIteratorClass;
  }

  @Override
  protected void configure() {
    bind(JobOptions.class).toInstance(jobOptions);
    bindConstant().annotatedWith(LastRunDir.class).to(jobOptions.getLastRunLoc());
    bind(SavepointOperator.class).to(FilesystemSavepointOperator.class).asEagerSingleton();
    bind(JobPreparator.class).to(jobPreparatorClass);
    bindConstant().annotatedWith(JobBatchSize.class)
        .to(getJobsConfiguration(jobOptions).getBatchSize());
    bindConstant().annotatedWith(ElasticSearchBulkSize.class)
        .to(getJobsConfiguration(jobOptions).getElasticSearchBulkSize());
    bindConstant().annotatedWith(ReaderThreadsCount.class)
        .to(getJobsConfiguration(jobOptions).getReaderThreadsCount());
    if (elasticSearchModule != null) {
      install(elasticSearchModule);
    } else {
      install(new ElasticSearchModule(getJobsConfiguration(jobOptions)));
    }
  }

  protected abstract BaseJobConfiguration getJobsConfiguration(JobOptions jobsOptions);

  @Provides
  @Inject
  public BaseJobConfiguration getBaseJobsConfiguration(JobOptions jobsOptions) {
    return getJobsConfiguration(jobsOptions);
  }

  static class DefaultJobPreparator implements JobPreparator {

    @Override
    public void run() {
      //empty by default
    }
  }
}
