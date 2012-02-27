package org.jboss.arquillian.android.api;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public interface AndroidDevice {

    /**
     * Returns the serial number of the device.
     */
    public String getSerialNumber();

    /**
     * Returns the name of the AVD the emulator is running.
     * <p/>
     * This is only valid if {@link #isEmulator()} returns true.
     * <p/>
     * If the emulator is not running any AVD (for instance it's running from an Android source tree build), this method will
     * return "<code>&lt;build&gt;</code>".
     *
     * @return the name of the AVD or <code>null</code> if there isn't any.
     */
    public String getAvdName();

    /**
     * Returns the device properties. It contains the whole output of 'getprop'
     */
    public Map<String, String> getProperties();

    /**
     * Returns the cached property value.
     *
     * @param name the name of the value to return.
     * @return the value or <code>null</code> if the property does not exist or has not yet been cached.
     */
    public String getProperty(String name) throws IOException, AndroidExecutionException;

    /**
     * Returns if the device is ready.
     *
     */
    public boolean isOnline();

    /**
     * Returns <code>true</code> if the device is an emulator.
     */
    public boolean isEmulator();

    /**
     * Returns if the device is offline
     *
     */
    public boolean isOffline();

    /**
     * Executes a shell command on the device, and sends the result to a <var>receiver</var>
     * <p/>
     * This is similar to calling <code>executeShellCommand(command, receiver, DdmPreferences.getTimeOut())</code>.
     *
     * @param command the shell command to execute
     * @param receiver the {@link AndroidDeviceOutputReciever} that will receives the output of the shell command
     */
    public void executeShellCommand(String command, AndroidDeviceOutputReciever reciever) throws AndroidExecutionException,
            IOException;

    /**
     * Creates a port forwarding between a local and a remote port.
     *
     * @param localPort the local port to forward
     * @param remotePort the remote port.
     */
    public void createForward(int localPort, int remotePort) throws AndroidExecutionException, IOException;

    /**
     * Removes a port forwarding between a local and a remote port.
     *
     * @param localPort the local port to forward
     * @param remotePort the remote port.
     */
    public void removeForward(int localPort, int remotePort) throws AndroidExecutionException, IOException;

    /**
     * Installs an Android application on device. This is a helper method that combines the syncPackageToDevice,
     * installRemotePackage, and removePackage steps
     *
     * @param packageFilePath the absolute file system path to file on local host to install
     * @param reinstall set to <code>true</code> if re-install of app should be performed
     * @param extraArgs optional extra arguments to pass. See 'adb shell pm install --help' for available options.
     * @return a {@link String} with an error code, or <code>null</code> if success.
     */
    public void installPackage(File packageFilePath, boolean reinstall, String... extraArgs) throws AndroidExecutionException;

    /**
     * Uninstalls an package from the device.
     *
     * @param packageName the Android application package name to uninstall
     * @return a {@link String} with an error code, or <code>null</code> if success.
     */
    public void uninstallPackage(String packageName) throws AndroidExecutionException;

}
