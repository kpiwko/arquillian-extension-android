package org.jboss.arquillian.android.event;

public class AndroidVirtualDeviceAvailable {

    private final String name;

    public AndroidVirtualDeviceAvailable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
