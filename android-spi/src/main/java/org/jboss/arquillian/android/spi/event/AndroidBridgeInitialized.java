package org.jboss.arquillian.android.spi.event;

import org.jboss.arquillian.android.api.AndroidBridge;

public class AndroidBridgeInitialized {
    private AndroidBridge bridge;

    public AndroidBridgeInitialized(AndroidBridge bridge) {
        this.bridge = bridge;
    }

    public AndroidBridge getBridge() {
        return bridge;
    }

    public void setBridge(AndroidBridge bridge) {
        this.bridge = bridge;
    }
}
