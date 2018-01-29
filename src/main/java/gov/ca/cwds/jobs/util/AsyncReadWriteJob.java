package gov.ca.cwds.jobs.util;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import gov.ca.cwds.jobs.component.Rocket;
import gov.ca.cwds.neutron.exception.NeutronCheckedException;
import gov.ca.cwds.neutron.exception.NeutronRuntimeException;

/**
 * @author CWDS Elasticsearch Team
 * @param <I> output reader type
 * @param <O> output writer type
 */
@SuppressWarnings("unchecked")
public class AsyncReadWriteJob<I extends Serializable, O extends Serializable>
    extends ProducerConsumer<I> implements Rocket, JobComponent {

  private static final long serialVersionUID = 1L;

  private transient JobReader<I> reader;
  private transient JobProcessor<I, O> processor;
  private transient JobWriter<O> writer;

  private List<O> chunk = new LinkedList<>();

  private int chunkSize = 100;

  /**
   * @param reader reader
   * @param processor processor
   * @param writer writer
   */
  public AsyncReadWriteJob(JobReader<I> reader, JobProcessor<I, O> processor, JobWriter<O> writer) {
    this.reader = reader;
    this.processor = processor;
    this.writer = writer;
  }

  /**
   * Input type = Output Type, no mapping required
   * 
   * @param reader reader
   * @param writer writer
   */
  public AsyncReadWriteJob(JobReader<I> reader, JobWriter<O> writer) {
    this.reader = reader;
    this.processor = item -> (O) item;
    this.writer = writer;
  }

  /**
   * Set the chunk size.
   * 
   * @param chunkSize chunk size
   */
  public void setChunkSize(int chunkSize) {
    if (chunkSize > 0) {
      this.chunkSize = chunkSize;
    }
  }

  @Override
  public I produce() {
    try {
      return reader.read();
    } catch (Exception e) {
      throw new NeutronRuntimeException(e);
    }
  }

  @Override
  public void consume(I in) {
    try {
      O out = processor.process(in);
      chunk.add(out);
      if (chunk.size() == chunkSize) {
        flush();
      }
    } catch (Exception e) {
      chunk.clear();
      throw new NeutronRuntimeException("ERROR CONSUMING CHUNK!", e);
    }
  }

  private void flush() {
    writer.write(chunk); // NOSONAR
    chunk.clear();
  }

  @Override
  public void run() {
    try {
      init();
      super.run();
      if (!chunk.isEmpty()) {
        flush();
      }
    } catch (Exception e) {
      throw new NeutronRuntimeException(e);
    } finally {
      try {
        destroy();
      } catch (Exception e) {
        throw new NeutronRuntimeException(e); // NOSONAR
      }
    }
  }

  @Override
  public void init() throws NeutronCheckedException {
    reader.init();
    writer.init();
  }

  @Override
  public void destroy() throws NeutronCheckedException {
    reader.destroy();
    writer.destroy();
  }

}
