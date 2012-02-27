package org.jboss.arquillian.android.api;

import java.util.List;

public interface AndroidBridge {

    List<AndroidDevice> getDevices();

    void connect() throws AndroidExecutionException;

    boolean isConnected();

    void disconnect() throws AndroidExecutionException;
}
