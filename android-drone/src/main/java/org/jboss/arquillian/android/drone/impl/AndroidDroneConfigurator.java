package org.jboss.arquillian.android.drone.impl;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.jboss.arquillian.android.configuration.ConfigurationMapper;
import org.jboss.arquillian.android.drone.configuration.AndroidDroneConfiguration;
import org.jboss.arquillian.android.drone.event.AndroidDroneConfigured;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;

public class AndroidDroneConfigurator {
    private static final Logger log = Logger.getLogger(AndroidDroneConfigurator.class.getName());

    public static final String ANDROID_DRONE_EXTENSION_NAME = "android-drone";

    @Inject
    @SuiteScoped
    private InstanceProducer<AndroidDroneConfiguration> androidDroneConfiguration;

    @Inject
    private Event<AndroidDroneConfigured> afterConfiguration;

    public void configureAndroidDrone(@Observes BeforeSuite event, ArquillianDescriptor descriptor) {

        AndroidDroneConfiguration configuration = new AndroidDroneConfiguration();
        boolean configured = false;

        for (ExtensionDef extensionDef : descriptor.getExtensions()) {
            if (ANDROID_DRONE_EXTENSION_NAME.equals(extensionDef.getExtensionName())) {
                ConfigurationMapper.fromArquillianDescriptor(descriptor, configuration, extensionDef.getExtensionProperties());
                configured = true;
                log.fine("Configured Android Drone extension from Arquillian configuration file");
            }
        }

        if (configured && configuration.isSkip() != true) {
            Validate.isReadable(configuration.getAndroidServerApk(), "You must provide a valid path to Android Server APK: "
                    + configuration.getAndroidServerApk());

            File webdriverLog = configuration.getWebdriverLogFile();

            Validate.notNull(webdriverLog, "You must provide a valid path to Android Webdriver Monkey log file: "
                    + configuration.getWebdriverLogFile());

            // create the log file if not present
            try {
                webdriverLog.createNewFile();
            } catch (IOException e) {
                throw new IllegalStateException("Unable to create Android Webdriver Monkey log file at "
                        + webdriverLog.getAbsolutePath(), e);
            }

            androidDroneConfiguration.set(configuration);
            afterConfiguration.fire(new AndroidDroneConfigured());
        }
    }
}
