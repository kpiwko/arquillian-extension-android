package org.jboss.arquillian.android.spi.event;

import org.jboss.arquillian.android.api.AndroidDevice;

public class AndroidDeviceReady extends AndroidDeviceEvent {

    private AndroidDevice device;

    public AndroidDeviceReady(AndroidDevice device) {
        super(device.getAvdName());
        this.device = device;
    }

    public AndroidDevice getDevice() {
        return device;
    }
}
