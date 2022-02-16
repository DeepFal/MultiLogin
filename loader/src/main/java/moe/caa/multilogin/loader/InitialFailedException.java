package moe.caa.multilogin.loader;

public class InitialFailedException extends Exception {
    public InitialFailedException() {
    }

    public InitialFailedException(String message) {
        super(message);
    }

    public InitialFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
