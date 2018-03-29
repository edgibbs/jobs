package gov.ca.cwds.data.persistence.cms;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.NamedNativeQuery;

import gov.ca.cwds.neutron.rocket.cases.CaseSQLResource;

/**
 * Entity bean for view VW_LST_CASE_HIST for focus child person cases.
 * 
 * @author CWDS API Team
 */
@Entity
@Table(name = "VW_LST_CASE_HIST")
//@formatter:off
@NamedNativeQuery(
    name = "gov.ca.cwds.data.persistence.cms.EsChildPersonCase.findAllUpdatedAfterChild",
    query = CaseSQLResource.SELECT_LAST_RUN_CHILD
        + " ORDER BY FOCUS_CHILD_ID, CASE_ID, PARENT_ID "
        + "FOR READ ONLY WITH UR ",
    resultClass = EsChildPersonCase.class, readOnly = true)
@NamedNativeQuery(
    name = "gov.ca.cwds.data.persistence.cms.EsChildPersonCase.findAllUpdatedAfterParent",
    query = CaseSQLResource.SELECT_LAST_RUN_PARENT 
        + " ORDER BY FOCUS_CHILD_ID, CASE_ID, PARENT_ID "
        + "FOR READ ONLY WITH UR ",
    resultClass = EsChildPersonCase.class, readOnly = true)
@NamedNativeQuery(
    name = "gov.ca.cwds.data.persistence.cms.EsChildPersonCase.findAllUpdatedAfterWithLimitedAccess",
    query = "SELECT x.* FROM (" + CaseSQLResource.SELECT_LAST_RUN_CHILD + ") x "
        + " WHERE (1 = 1 OR CURRENT TIMESTAMP != :after) AND x.LIMITED_ACCESS_CODE = 'N' "
        + "ORDER BY FOCUS_CHILD_ID, CASE_ID, PARENT_ID "
        + "FOR READ ONLY WITH UR ",
    resultClass = EsChildPersonCase.class, readOnly = true)
//@formatter:on
public class EsChildPersonCase extends EsPersonCase {

  private static final long serialVersionUID = 8157993904607079133L;

  /**
   * Default constructor.
   */
  public EsChildPersonCase() {
    super();
  }

  @Override
  public String getNormalizationGroupKey() {
    return this.getFocusChildId();
  }

}
