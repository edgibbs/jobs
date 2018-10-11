## CWDS Jobs

The CWDS Jobs project provides java based stand alone applications which are meant to be scheduled periodically.

## Installation

## Prerequisites
Prerequisites are job dependent but typically you can expect the following to be needed :

1.  DB2 10.x
2.  Postgres 9.x
3.  Elasticsearch 5.3.2 or newer (newer would require testing)

## Development Environment

### Prerequisites

1. Source code, available at [GitHub](https://github.com/ca-cwds/jobs)
1. Java SE 8 development kit
1. DB2 Database
1. Postgres Database
1. Elasticsearch

### Building

% ./gradlew build


### Facility Indexer Job

Main Class: gov.ca.cwds.jobs.FacilityIndexerJob
run job using following command: 
```bash
$java -cp jobs.jar gov.ca.cwds.jobs.FacilityIndexerJob path/to/config/file.yaml
```
#### Code overview
In order to create new job you have to implement 2 interfaces: _JobReader_, _JobWriter_ and optional _JobProcessor_
_JobReader_ has a single method _I read()_ which is responsible for reading items from input source. It must return null when everything is read.
_JobWriter_ has a single method _write(List\<I\> items)_ which is responsible for writing chunk of data to output target.
_JobProcessor_ has a single method _O process(I item)_, you can implement it if some mapping logic is required
Then you should provide those components to a job. Google guice example config:
```java
@Provides
@Named("job")
@Inject
public Job job(@Named("reader") JobReader reader,
               @Named("writer") JobWriter writer) {
        return new AsyncReadWriteJob(reader, writer);
}

//or with processor

@Provides
@Named("job")
@Inject
public Job job(@Named("reader") JobReader reader,
               @Named("processor") JobProcessor processor,
               @Named("writer") JobWriter writer) {
        return new AsyncReadWriteJob(reader, processor, writer);
}
```
Read and write operation runs in separate threads.


# Questions

If you have any questions regarding the contents of this repository, please email the Office of Systems Integration at FOSS@osi.ca.gov.
