package org.jboss.arquillian.android.drone;

import org.jboss.arquillian.android.drone.impl.AndroidDroneConfigurator;
import org.jboss.arquillian.android.drone.impl.AndroidWebDriverSupport;
import org.jboss.arquillian.core.spi.LoadableExtension;

public class AndroidDroneExtension implements LoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        builder.observer(AndroidDroneConfigurator.class);
        builder.observer(AndroidWebDriverSupport.class);
    }

}
