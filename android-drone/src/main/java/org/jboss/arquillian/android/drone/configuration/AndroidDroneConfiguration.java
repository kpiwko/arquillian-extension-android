package org.jboss.arquillian.android.drone.configuration;

import java.io.File;

public class AndroidDroneConfiguration {

    private File androidServerApk = new File("android-server.apk");

    private File webdriverLogFile = new File("target/android-webdriver-monkey.log");

    private int webdriverPortHost = 14444;

    private int webdriverPortGuest = 8080;

    private boolean verbose = false;

    private boolean skip = false;

    /**
     * @return the verbose
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * @param verbose the verbose to set
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * @return the androidServerApk
     */
    public File getAndroidServerApk() {
        return androidServerApk;
    }

    /**
     * @param androidServerApk the androidServerApk to set
     */
    public void setAndroidServerApk(File androidServerApk) {
        this.androidServerApk = androidServerApk;
    }

    /**
     * @return the webdriverLogFile
     */
    public File getWebdriverLogFile() {
        return webdriverLogFile;
    }

    /**
     * @param webdriverLogFile the webdriverLogFile to set
     */
    public void setWebdriverLogFile(File webdriverLogFile) {
        this.webdriverLogFile = webdriverLogFile;
    }

    /**
     * @return the webdriverPortHost
     */
    public int getWebdriverPortHost() {
        return webdriverPortHost;
    }

    /**
     * @param webdriverPortHost the webdriverPortHost to set
     */
    public void setWebdriverPortHost(int webdriverPortHost) {
        this.webdriverPortHost = webdriverPortHost;
    }

    /**
     * @return the webdriverPortGuest
     */
    public int getWebdriverPortGuest() {
        return webdriverPortGuest;
    }

    /**
     * @param webdriverPortGuest the webdriverPortGuest to set
     */
    public void setWebdriverPortGuest(int webdriverPortGuest) {
        this.webdriverPortGuest = webdriverPortGuest;
    }

    /**
     * @return the skip
     */
    public boolean isSkip() {
        return skip;
    }

    /**
     * @param skip the skip to set
     */
    public void setSkip(boolean skip) {
        this.skip = skip;
    }

}
