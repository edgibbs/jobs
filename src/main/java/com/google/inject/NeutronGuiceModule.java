package com.google.inject;

/**
 * Guice abstract module to facilitate unit tests.
 * 
 * @author CWDS API Team
 */
public abstract class NeutronGuiceModule extends AbstractModule {

  private Binder testBinder;

  /**
   * {@inheritDoc}
   */
  @Override
  protected Binder binder() {
    return testBinder != null ? testBinder : binder;
  }

  public Binder getTestBinder() {
    return testBinder;
  }

  public void setTestBinder(Binder testBinder) {
    this.testBinder = testBinder;
  }

}
