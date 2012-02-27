package org.jboss.arquillian.android.api;

public class AndroidExecutionException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public AndroidExecutionException(Throwable cause) {
        super(cause);
    }

    public AndroidExecutionException() {
    }

    public AndroidExecutionException(String msg) {
        super(msg);
    }

    public AndroidExecutionException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
