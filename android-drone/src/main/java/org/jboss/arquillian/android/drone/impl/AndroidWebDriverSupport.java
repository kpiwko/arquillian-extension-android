package org.jboss.arquillian.android.drone.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Logger;

import org.jboss.arquillian.android.api.AndroidBridge;
import org.jboss.arquillian.android.api.AndroidDevice;
import org.jboss.arquillian.android.api.AndroidDeviceOutputReciever;
import org.jboss.arquillian.android.api.AndroidExecutionException;
import org.jboss.arquillian.android.drone.configuration.AndroidDroneConfiguration;
import org.jboss.arquillian.android.spi.event.AndroidDeviceReady;
import org.jboss.arquillian.core.api.annotation.Observes;

public class AndroidWebDriverSupport {
    private static final Logger log = Logger.getLogger(AndroidWebDriverSupport.class.getName());

    private static final String START_WEBDRIVER_HUB_CMD = "am start -a android.intent.action.MAIN -n org.openqa.selenium.android.app/.MainActivity";
    private static final String TOP_CMD = "top -n 1";
    private static final String WEBDRIVER_HUB_NAME = "org.openqa.selenium.android.app";

    public void prepareWebDriverEnvironment(@Observes AndroidDeviceReady event, AndroidDroneConfiguration configuration,
            AndroidBridge bridge) throws AndroidExecutionException, IOException {

        if (!bridge.isConnected()) {
            throw new IllegalStateException("Android debug bridge must be connected in order to prepare WebDriver support");
        }

        AndroidDevice device = event.getDevice();

        log.info("Installing Android Server APK for WebDriver support");
        device.installPackage(configuration.getAndroidServerApk(), true);

        // start selenium server
        WebDriverMonkey monkey = new WebDriverMonkey(configuration.getWebdriverLogFile(), configuration.isVerbose());
        device.executeShellCommand(START_WEBDRIVER_HUB_CMD, monkey);

        // check the process of selenium server is present
        waitUntilSeleniumStarted(device, monkey);

        // forward ports
        log.info("Creating port forwaring for WebDriver support");
        device.createForward(configuration.getWebdriverPortHost(), configuration.getWebdriverPortGuest());
    }

    private void waitUntilSeleniumStarted(AndroidDevice device, WebDriverMonkey monkey) throws IOException,
            AndroidExecutionException {

        log.info("Waiting until the Selenium is ready.");
        for (int i = 0; i < 5; i++) {
            device.executeShellCommand(TOP_CMD, monkey);
            if (monkey.isWebDriverHubStarted()) {
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        throw new AndroidExecutionException("Unable to start Android Server for WebDriver support.");
    }

    private static class WebDriverMonkey implements AndroidDeviceOutputReciever {

        private final boolean verbose;
        private final Writer output;

        private boolean started = false;

        public WebDriverMonkey(File output, boolean verbose) throws IOException {
            this.output = new FileWriter(output);
            this.verbose = verbose;
        }

        @Override
        public void processNewLines(String[] lines) {
            for (String line : lines) {
                try {
                    output.append(line).append("\n").flush();
                } catch (IOException e) {
                    // ignore output
                }
                if (line.contains(WEBDRIVER_HUB_NAME)) {
                    this.started = true;
                }
            }

        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isVerbose() {
            return verbose;
        }

        public boolean isWebDriverHubStarted() {
            return started;
        }

    }

}
