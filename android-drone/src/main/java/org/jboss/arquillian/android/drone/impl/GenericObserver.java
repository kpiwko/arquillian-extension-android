package org.jboss.arquillian.android.drone.impl;

import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;


public class GenericObserver {

	public void observeEvent(@Observes EventContext<Object> context) {
		System.out.println("CONTEXT: " + context.getEvent().getClass());
		context.proceed();
	}
	
	public void observeObject(@Observes Object event) {
		System.out.println("OBJECT: " + event.getClass() + "\n" + event.toString());		
	}
}
