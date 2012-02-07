package org.jboss.arquillian.android.impl;

import java.io.IOException;
import java.util.logging.Logger;

import org.jboss.arquillian.android.configuration.AndroidSdk;
import org.jboss.arquillian.android.configuration.AndroidSdkConfiguration;
import org.jboss.arquillian.android.event.AndroidDebugBridgeInitialized;
import org.jboss.arquillian.android.event.AndroidDeviceReady;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;

public class EmulatorStartup {
    private static final Logger log = Logger.getLogger(EmulatorStartup.class.getName());

    @Inject
    @SuiteScoped
    private InstanceProducer<AndroidEmulator> androidEmulator;

    @Inject
    @SuiteScoped
    private InstanceProducer<IDevice> iDevice;

    @Inject
    private Event<AndroidDeviceReady> androidDeviceReady;

    public void createAndroidVirtualDeviceAvailable(@Observes AndroidDebugBridgeInitialized event,
            AndroidSdkConfiguration configuration, AndroidSdk sdk, ProcessExecutor executor) throws IOException {

        AndroidDebugBridge bridge = event.getBridge();

        if (!bridge.isConnected()) {
            throw new IllegalStateException("Android debug bridge must be connected in order to spawn emulator");
        }

        String name = configuration.getAvdName();
        String serialId = configuration.getSerialId();
        IDevice running = null;
        for (IDevice device : bridge.getDevices()) {
            if (equalsIgnoreNulls(name, device.getAvdName()) || equalsIgnoreNulls(serialId, device.getSerialNumber())) {
                running = device;
                break;
            }

        }

        if (running == null) {

            log.info("Starting avd named: " + name);
            
            // discover what device was added here
            DeviceDiscovery deviceDiscovery = new DeviceDiscovery();
            AndroidDebugBridge.addDeviceChangeListener(deviceDiscovery);

            Process emulator = executor.spawn(sdk.getEmulatorPath(), "-avd", name, configuration.getEmulatorOptions());
            androidEmulator.set(new AndroidEmulator(emulator));

            try {
                Thread.sleep(configuration.getEmulatorStartupTimeout());
                Validate.stateNotNull(deviceDiscovery.getDiscoveredDevice(), "No emulator device was connected during "
                        + configuration.getEmulatorStartupTimeout()
                        + "ms to Android Debug Bridge. Please increase the time limit in order to get emulator connected.");
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted while waiting for Emulator for " + name + " to start up");
            }

            running = deviceDiscovery.getDiscoveredDevice();
            AndroidDebugBridge.removeDeviceChangeListener(deviceDiscovery);

        } else {
            if (serialId != null) {
                log.fine("Using a real device with serialId: " + serialId);
            } else {
                log.info("Emulator for device " + name
                        + " is already started or represents a physical device, device serial is " + running.getSerialNumber()
                        + ". Emulator will not be reinitialized.");
            }
        }

        // publish device and fire event
        iDevice.set(running);
        androidDeviceReady.fire(new AndroidDeviceReady(running));

    }

    private boolean equalsIgnoreNulls(String current, String other) {
        if (current == null && other == null) {
            return false;
        } else if (current == null && other != null) {
            return false;
        } else if (current != null && other == null) {
            return false;
        }

        return current.equals(other);
    }

    private class DeviceDiscovery implements IDeviceChangeListener {

        private IDevice discoveredDevice;

        @Override
        public void deviceChanged(IDevice device, int changeMask) {
        }

        @Override
        public void deviceConnected(IDevice device) {
            this.discoveredDevice = device;
            log.fine("Discovered an emulator device connected to ADB bus");
        }

        @Override
        public void deviceDisconnected(IDevice device) {
        }

        public IDevice getDiscoveredDevice() {
            return discoveredDevice;
        }
    }
}
