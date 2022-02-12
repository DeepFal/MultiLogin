package moe.caa.multilogin.logger.bridges;

import lombok.AllArgsConstructor;
import moe.caa.multilogin.logger.Level;
import org.apache.logging.log4j.Logger;

/**
 * org.apache.logging.log4j.Logger 日志程序桥接
 */
@AllArgsConstructor
public class Log4JLoggerBridge extends BaseLoggerBridge {
    private final Logger HANDLER;

    @Override
    public void log(Level level, String message, Throwable throwable) {
        if (level == Level.DEBUG) {
            HANDLER.log(org.apache.logging.log4j.Level.DEBUG, message, throwable);
        } else if (level == Level.INFO) {
            HANDLER.log(org.apache.logging.log4j.Level.INFO, message, throwable);
        } else if (level == Level.WARN) {
            HANDLER.log(org.apache.logging.log4j.Level.WARN, message, throwable);
        } else if (level == Level.ERROR) {
            HANDLER.log(org.apache.logging.log4j.Level.ERROR, message, throwable);
        }
    }
}
