package org.jboss.arquillian.android.impl;

import org.jboss.arquillian.android.event.AndroidVirtualDeviceAvailable;
import org.jboss.arquillian.android.event.AndroidVirtualDeviceCreated;
import org.jboss.arquillian.core.api.annotation.Observes;

public class EmulatorStarter {

    public void createAndroidVirtualDeviceAvailable(@Observes AndroidVirtualDeviceAvailable event) {
        startAvd(event.getName());
    }

    public void createAndroidVirtualDeviceCreated(@Observes AndroidVirtualDeviceCreated event) {
        startAvd(event.getName());
    }

    private void startAvd(String name) {

    }
}
