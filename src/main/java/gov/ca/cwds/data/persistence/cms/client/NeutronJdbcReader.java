package gov.ca.cwds.data.persistence.cms.client;

import java.sql.ResultSet;
import java.sql.SQLException;

import gov.ca.cwds.data.persistence.PersistentObject;

@FunctionalInterface
public interface NeutronJdbcReader<T extends PersistentObject> {

  T read(final ResultSet rs) throws SQLException;

}
