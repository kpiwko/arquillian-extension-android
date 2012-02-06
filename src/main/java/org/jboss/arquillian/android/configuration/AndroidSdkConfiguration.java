package org.jboss.arquillian.android.configuration;

public class AndroidSdkConfiguration {

    private boolean skip;

    private boolean verbose;

    private boolean force;

    private String avdName;

    private String emulatorOptions;

    private String home = System.getenv("ANDROID_HOME");

    private String apiLevel = "10";

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public String getAvdName() {
        return avdName;
    }

    public void setAvdName(String avdName) {
        this.avdName = avdName;
    }

    public String getEmulatorOptions() {
        return emulatorOptions;
    }

    public void setEmulatorOptions(String emulatorOptions) {
        this.emulatorOptions = emulatorOptions;
    }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public String getApiLevel() {
        return apiLevel;
    }

    public void setApiLevel(String apiLevel) {
        this.apiLevel = apiLevel;
    }
}
