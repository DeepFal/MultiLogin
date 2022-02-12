package moe.caa.multilogin.logger.bridges;

import lombok.AllArgsConstructor;
import moe.caa.multilogin.logger.Level;
import org.slf4j.Logger;

/**
 * org.slf4j.Logger 日志程序桥接
 */
@AllArgsConstructor
public class Slf4JLoggerBridge extends BaseLoggerBridge {
    private final Logger HANDLER;

    @Override
    public void log(Level level, String message, Throwable throwable) {
        if (level == Level.DEBUG) {
            HANDLER.debug(message, throwable);
        } else if (level == Level.INFO) {
            HANDLER.info(message, throwable);
        } else if (level == Level.WARN) {
            HANDLER.warn(message, throwable);
        } else if (level == Level.ERROR) {
            HANDLER.error(message, throwable);
        }
    }
}
