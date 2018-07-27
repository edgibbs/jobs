package gov.ca.cwds.neutron.vox;

import java.lang.management.ManagementFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weakref.jmx.MBeanExporter;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.tools.jmx.Manager;

import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.neutron.atom.AtomCommandCenterConsole;
import gov.ca.cwds.neutron.atom.AtomLaunchDirector;
import gov.ca.cwds.neutron.atom.AtomLaunchPad;
import gov.ca.cwds.neutron.launch.LaunchCommandSettings;
import gov.ca.cwds.neutron.vox.rest.NeutronRestServer;

/**
 * <p>
 * Exposes the following to JMX:
 * </p>
 * <ul>
 * <li>Command Center methods</li>
 * <li>Guice bean attributes</li>
 * </ul>
 * 
 * @author CWDS API Team
 */
@Singleton
public class XRaySpex implements AtomCommandCenterConsole {

  private static final Logger LOGGER = LoggerFactory.getLogger(XRaySpex.class);

  private final Injector injector;
  private final LaunchCommandSettings settings;
  private final AtomLaunchDirector launchDirector;

  private Thread jettyServer;

  /**
   * REST administration. Started if enabled in {@link LaunchCommandSettings}.
   */
  private NeutronRestServer restServer = new NeutronRestServer();

  @Inject
  public XRaySpex(final LaunchCommandSettings settings, final AtomLaunchDirector launchDirector,
      final Injector injector) {
    this.settings = settings;
    this.launchDirector = launchDirector;
    this.injector = injector;
  }

  @Override
  public void initCommandControl() {
    if (LaunchCommand.getSettings().isExposeJmx()) {
      exposeJMX();
    }

    if (LaunchCommand.getSettings().isExposeRest()) {
      exposeREST();
    }
  }

  /**
   * Jetty for REST administration.
   */
  protected void exposeREST() {
    jettyServer = new Thread(restServer::run);
    jettyServer.start();
  }

  protected void exposeJMX() {
    LOGGER.warn("\n>>>>>>> ENABLE JMX! <<<<<<<\n");
    final MBeanExporter exporter = new MBeanExporter(ManagementFactory.getPlatformMBeanServer());
    for (AtomLaunchPad pad : launchDirector.getLaunchPads().values()) {
      exporter.export("Neutron:rocket=" + pad.getFlightSchedule().getRocketName(), pad);
    }

    exporter.export("Neutron:runner=Launch_Command", this);
    LOGGER.info("MBeans: {}", exporter.getExportedObjects());

    Manager.manage("Neutron_Guice", injector);
  }

  public LaunchCommandSettings getSettings() {
    return settings;
  }

  public Thread getJettyServer() {
    return jettyServer;
  }

}
