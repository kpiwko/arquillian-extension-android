package org.jboss.arquillian.android.spi.event;

public class AndroidDeviceEvent {
    protected final String name;

    public AndroidDeviceEvent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
