package gov.ca.cwds.neutron.enums;

/**
 * Neutron constants for the Quartz scheduler.
 * 
 * @author CWDS API Team
 */
public class NeutronSchedulerConstants {

  public static final String ROCKET_CLASS = "job_class";

  public static final String GRP_LST_CHG = "last_chg";

  public static final String GRP_FULL_LOAD = "full_load";

  public static final int LAST_CHG_WINDOW_HOURS = 2;

  public static final String SCHEDULER_THREAD_COUNT = "4";

  public static final String SCHEDULER_INSTANCE_NAME = "neutron";

  private NeutronSchedulerConstants() {
    // static constants only
  }

}
