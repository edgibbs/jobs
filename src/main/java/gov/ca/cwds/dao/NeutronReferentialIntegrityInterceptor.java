package gov.ca.cwds.dao;

import java.io.Serializable;
import java.util.Iterator;

import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.ca.cwds.data.ApiReferentialIntegrityInterceptor;

/**
 * Hibernate interceptor traps referential integrity errors.
 * 
 * @author CWDS API Team
 */
public class NeutronReferentialIntegrityInterceptor extends ApiReferentialIntegrityInterceptor {

  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER =
      LoggerFactory.getLogger(NeutronReferentialIntegrityInterceptor.class);

  @Override
  public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames,
      Type[] types) {
    LOGGER.debug("on delete");
  }

  // Called on entity update.
  @Override
  public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState,
      Object[] previousState, String[] propertyNames, Type[] types) {
    LOGGER.debug("on flush dirty");

    // if (entity instanceof Client) {
    // LOGGER.debug("Client Update Operation");
    // return true;
    // }

    return false;
  }

  // Called on entity load.
  @Override
  public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames,
      Type[] types) {
    LOGGER.debug("Load Operation");
    return true;
  }

  @Override
  public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames,
      Type[] types) {
    LOGGER.debug("on save");

    // if (entity instanceof Client) {
    // LOGGER.debug("Client Create Operation");
    // return true;
    // }

    return false;
  }

  // Called before commit to database.
  @Override
  public void preFlush(Iterator iterator) {
    LOGGER.debug("Before commiting");
  }

  // Called after committed to database.
  @Override
  public void postFlush(Iterator iterator) {
    LOGGER.debug("After commiting");
  }
}
