package moe.caa.multilogin.core.auth.yggdrasil;

import java.io.IOException;

/**
 * 服务器宕机异常
 */
public class ServiceUnavailableException extends IOException {
    public ServiceUnavailableException() {
    }

    public ServiceUnavailableException(String message) {
        super(message);
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
