package org.jboss.arquillian.android.impl;

import java.io.File;
import java.util.logging.Logger;

import org.jboss.arquillian.android.api.AndroidBridge;
import org.jboss.arquillian.android.api.AndroidExecutionException;
import org.jboss.arquillian.android.configuration.AndroidExtensionConfiguration;
import org.jboss.arquillian.android.configuration.AndroidSdk;
import org.jboss.arquillian.android.spi.event.AndroidBridgeInitialized;
import org.jboss.arquillian.android.spi.event.AndroidBridgeTerminated;
import org.jboss.arquillian.android.spi.event.AndroidDeviceShutdown;
import org.jboss.arquillian.android.spi.event.AndroidExtensionConfigured;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;

/**
 * @author <a href="kpiwko@redhat.com">Karel Piwko</a>
 */
public class AndroidBridgeConnector {

    private static final Logger log = Logger.getLogger(AndroidBridgeConnector.class.getName());

    @Inject
    @SuiteScoped
    private InstanceProducer<AndroidBridge> androidBridge;

    @Inject
    private Event<AndroidBridgeInitialized> adbInitialized;

    @Inject
    private Event<AndroidBridgeTerminated> adbTerminated;

    public void initAndroidDebugBridge(@Observes AndroidExtensionConfigured event, AndroidSdk sdk,
            AndroidExtensionConfiguration configuration) throws AndroidExecutionException {

        long start = System.currentTimeMillis();
        log.info("Initializing Android Debug Bridge");
        AndroidBridge bridge = new AndroidBridgeImpl(new File(sdk.getAdbPath()), configuration.isForce());
        bridge.connect();
        long delta = System.currentTimeMillis() - start;
        log.info("Android debug Bridge was initialized in " + delta + "ms");
        androidBridge.set(bridge);

        adbInitialized.fire(new AndroidBridgeInitialized(bridge));
    }

    public void destroyAndroidDebugBridge(@Observes AndroidDeviceShutdown event) throws AndroidExecutionException {
        androidBridge.get().disconnect();
        adbTerminated.fire(new AndroidBridgeTerminated());
    }
}
