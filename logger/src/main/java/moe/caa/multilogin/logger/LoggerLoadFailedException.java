package moe.caa.multilogin.logger;

/**
 * 日志程序初始化异常
 */
public class LoggerLoadFailedException extends Exception {
    public LoggerLoadFailedException(String message) {
        super(message);
    }

    public LoggerLoadFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}