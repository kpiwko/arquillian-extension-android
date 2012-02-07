package org.jboss.arquillian.android.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import org.jboss.arquillian.android.AndroidConfigurationException;
import org.jboss.arquillian.android.configuration.AndroidSdk;
import org.jboss.arquillian.android.configuration.AndroidSdkConfiguration;
import org.jboss.arquillian.android.event.AndroidDeviceReady;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.Before;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.InstallException;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;

public class AndroidWebDriverSupport {
    private static final Logger log = Logger.getLogger(AndroidWebDriverSupport.class.getName());

    public void prepareWebDriverEnvironment(@Observes AndroidDeviceReady event, AndroidSdkConfiguration configuration,
            AndroidDebugBridge bridge) throws AndroidConfigurationException {

        if (!bridge.isConnected()) {
            throw new IllegalStateException("Android debug bridge must be connected in order to prepare webdriver support");
        }

        IDevice device = event.getDevice();

        // install android webdriver support on the device
        try {
            Validate.isReadable(configuration.getAndroidServerApk(), "Android server APK path must be valid, but it was: "
                    + configuration.getAndroidServerApk());
            device.installPackage(configuration.getAndroidServerApk(), configuration.isForce());
        } catch (InstallException e) {
            throw new AndroidConfigurationException("Unable to install Android Server APK from "
                    + configuration.getAndroidServerApk() + " on device " + device.getSerialNumber(), e);
        }
    }

    // we have to start it in before event because Drone does not have a proper event hierarchy
    public void startWebDriver(@Observes Before event, IDevice device, AndroidSdkConfiguration configuration, AndroidSdk sdk,
            ProcessExecutor executor) {

        try {
            WebDriverMonkey monkey = new WebDriverMonkey(configuration);
            // open webdriver
            device.executeShellCommand(
                    "am start -a android.intent.action.MAIN -n org.openqa.selenium.android.app/.MainActivity", monkey);

            // add port forwarding
            executor.execute(sdk.getAdbPath(), "-s", device.getSerialNumber(), "forward", "tcp:14444", "tcp:8080");
        } catch (TimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (AdbCommandRejectedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ShellCommandUnresponsiveException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private static class WebDriverMonkey implements IShellOutputReceiver {

        private AndroidSdkConfiguration configuration;

        private OutputStream ostream;

        private WebDriverMonkey(AndroidSdkConfiguration configuration) throws IOException {
            this.configuration = configuration;
            this.ostream = createLogFile();

        }

        @Override
        public void addOutput(byte[] data, int offset, int length) {

            if (configuration.isVerbose()) {
                String s = new String(data, offset, length);
                System.out.print(s);
            }

            try {
                ostream.write(data, offset, length);
            } catch (IOException e) {
                // ignore
            }

        }

        @Override
        public void flush() {
            try {
                ostream.flush();
            } catch (IOException e) {
                // ignore
            }

        }

        @Override
        public boolean isCancelled() {
            // TODO Auto-generated method stub
            return false;
        }

        private OutputStream createLogFile() {
            try {
                File output = new File("target/android-monkey.log");
                output.createNewFile();
                return new FileOutputStream(output);
            } catch (IOException e) {
                log.warning("Unable to create android monkey log file at " + "target/android-monkey.log ");
            }

            return new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    // do nothing
                }
            };
        }
    }

}
