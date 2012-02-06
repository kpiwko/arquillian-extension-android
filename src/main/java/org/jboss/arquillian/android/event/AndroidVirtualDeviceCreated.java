package org.jboss.arquillian.android.event;

public class AndroidVirtualDeviceCreated {

    private final String name;

    public AndroidVirtualDeviceCreated(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
