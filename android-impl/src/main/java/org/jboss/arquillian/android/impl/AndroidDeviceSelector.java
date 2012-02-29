/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.android.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.arquillian.android.api.AndroidBridge;
import org.jboss.arquillian.android.api.AndroidDevice;
import org.jboss.arquillian.android.configuration.AndroidConfigurationException;
import org.jboss.arquillian.android.configuration.AndroidExtensionConfiguration;
import org.jboss.arquillian.android.configuration.AndroidSdk;
import org.jboss.arquillian.android.spi.event.AndroidBridgeInitialized;
import org.jboss.arquillian.android.spi.event.AndroidDeviceReady;
import org.jboss.arquillian.android.spi.event.AndroidVirtualDeviceAvailable;
import org.jboss.arquillian.android.spi.event.AndroidVirtualDeviceCreated;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;

/**
 * Select either a real device or virtual device for execution. If a real device is specified via its serial number, it will
 * check if it is connected, otherwise it will use an virtual device.
 *
 * Observes:
 * <ul>
 * <li>{@link AndroidBridgeInitialized}</li>
 * </ul>
 *
 * Creates:
 * <ul>
 * <li>{@link AndroidDevice}</li>
 * </ul>
 *
 * Fires:
 * <ul>
 * <li>{@link AndroidVirtualDeviceCreated}</li>
 * <li>{@link AndroidVirtualDeviceAvailable}</li>
 * <li>{@link AndroidDeviceReady}</li>
 * </ul>
 *
 * @author <a href="kpiwko@redhat.com">Karel Piwko</a>
 *
 */
public class AndroidDeviceSelector {
    private static Logger log = Logger.getLogger(AndroidDeviceSelector.class.getName());

    @Inject
    @SuiteScoped
    private InstanceProducer<AndroidDevice> androidDevice;

    @Inject
    private Event<AndroidVirtualDeviceCreated> avdCreated;

    @Inject
    private Event<AndroidVirtualDeviceAvailable> avdAvailable;

    @Inject
    private Event<AndroidDeviceReady> androidDeviceReady;

    @SuppressWarnings("serial")
    public void getOrCreateAndroidDevice(@Observes AndroidBridgeInitialized event, ProcessExecutor executor,
            AndroidExtensionConfiguration configuration, AndroidSdk sdk) throws AndroidConfigurationException, IOException {

        String avdName = configuration.getAvdName();
        String serialId = configuration.getSerialId();
        AndroidBridge bridge = event.getBridge();

        // get priority for device specified by serialId if such device is connected
        AndroidDevice device = checkIfRealDeviceIsConnected(bridge, serialId,
                configuration.getRealDeviceDiscoveryTimeoutInSeconds());
        if (device != null) {
            androidDevice.set(device);
            androidDeviceReady.fire(new AndroidDeviceReady(device));
            return;
        }

        Set<String> devices = getAvdDeviceNames(executor, sdk);

        // check out avd availability
        if (!devices.contains(avdName) || configuration.isForce()) {
            if (log.isLoggable(Level.FINE)) {
                log.fine("Creating an Android virtual device named " + avdName);
            }

            Validate.notNullOrEmpty(configuration.getSdSize(), "Memory SD card size must be defined");

            executor.execute(new HashMap<String, String>() {
                {
                    put("Do you wish to create a custom hardware profile [no]", "no\n");
                }
            }, sdk.getAndroidPath(), "create", "avd", "-n", avdName, "-t", "android-" + configuration.getApiLevel(), "-f",
                    "-p", avdName, "-c", configuration.getSdSize());

            log.info("Android virtual device " + avdName + " was created");
            avdCreated.fire(new AndroidVirtualDeviceCreated(avdName));
        } else {
            // device exists, will not be created
            log.info("Android virtual device " + avdName + " already exists, will be reused in tests");
            avdAvailable.fire(new AndroidVirtualDeviceAvailable(avdName));

        }

    }

    private AndroidDevice checkIfRealDeviceIsConnected(AndroidBridge bridge, String serialId, long timeout) {
        // no serialId was specified
        if (serialId == null || serialId.trim().isEmpty()) {
            return null;
        }

        // we can get device immediately
        for (AndroidDevice device : bridge.getDevices()) {
            if (serialId.equals(device.getSerialNumber())) {
                log.info("Selected a device by its \"serialId\" " + serialId);
                return device;
            }
        }

        NamedDeviceDiscovery deviceDiscovery = new NamedDeviceDiscovery(serialId);
        AndroidDebugBridge.addDeviceChangeListener(deviceDiscovery);

        log.info("Waiting " + timeout + " seconds until the device is connected");

        // wait until the device is connected to ADB
        long timeLeft = timeout * 1000;
        while (timeLeft > 0) {
            long started = System.currentTimeMillis();
            if (deviceDiscovery.isOnline()) {
                log.info("Selected a device by its \"serialId\" " + serialId);
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            timeLeft -= System.currentTimeMillis() - started;
        }
        // device is connected to ADB
        AndroidDevice connectedDevice = deviceDiscovery.getDiscoveredDevice();
        AndroidDebugBridge.removeDeviceChangeListener(deviceDiscovery);

        if (connectedDevice != null) {
            return connectedDevice;
        }

        log.warning("SerialId " + serialId + " was specified, however no such device was connected during " + timeout
                + " seconds. Trying to connect to an emulator instead.");

        return null;

    }

    private Set<String> getAvdDeviceNames(ProcessExecutor executor, AndroidSdk sdk) throws IOException {

        final Pattern deviceName = Pattern.compile("[\\s]*Name: ([^\\s]+)[\\s]*");

        Set<String> names = new HashSet<String>();

        List<String> output = executor.execute(sdk.getAndroidPath(), "list", "avd");

        for (String line : output) {
            Matcher m;
            if (line.trim().startsWith("Name: ") && (m = deviceName.matcher(line)).matches()) {
                String name = m.group(1);
                // skip a device which has no name
                if (name == null || name.trim().length() == 0) {
                    continue;
                }
                names.add(name);
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Available Android Device: " + name);
                }
            }
        }

        return names;
    }

    private class NamedDeviceDiscovery implements IDeviceChangeListener {

        private IDevice discoveredDevice;

        private boolean online;

        private String serialId;

        public NamedDeviceDiscovery(String serialId) {
            this.serialId = serialId;
        }

        @Override
        public void deviceChanged(IDevice device, int changeMask) {
            if (discoveredDevice.equals(device) && (changeMask & IDevice.CHANGE_STATE) == 1) {
                if (device.isOnline()) {
                    this.online = true;
                }
            }
        }

        @Override
        public void deviceConnected(IDevice device) {
            if (serialId.equals(device.getSerialNumber())) {
                this.discoveredDevice = device;
                log.fine("Discovered an device connected to ADB bus specified by serialId " + serialId);
            }
        }

        @Override
        public void deviceDisconnected(IDevice device) {
        }

        public AndroidDevice getDiscoveredDevice() {
            return new AndroidDeviceImpl(discoveredDevice);
        }

        public boolean isOnline() {
            return online;
        }
    }
}
