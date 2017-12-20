package gov.ca.cwds.neutron.inject.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

import gov.ca.cwds.neutron.rocket.LastFlightRocket;

/**
 * Marker annotation identifies batch jobs that implement the "last run" policy.
 * 
 * @author CWDS API Team
 * @see LastFlightRocket
 */
@BindingAnnotation
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LastRunFile {
}
