package gov.ca.cwds.jobs.json;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class GsonReader<T> implements MessageBodyReader<T> {

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] antns, MediaType mt) {
    return true;
  }

  @Override
  public T readFrom(Class<T> type, Type genericType, Annotation[] antns, MediaType mt,
      MultivaluedMap<String, String> mm, InputStream in)
      throws IOException, WebApplicationException {
    return new Gson().fromJson(IOUtils.toString(in), type);
  }

}
