package org.jboss.arquillian.android.api;

public interface AndroidDeviceOutputReciever {

    void processNewLines(String[] lines);

    boolean isCancelled();

    boolean isVerbose();

}
