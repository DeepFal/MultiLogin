package moe.caa.multilogin.logger;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import moe.caa.multilogin.logger.bridges.BaseLoggerBridge;

import java.io.File;

/**
 * MultiLogin 日志记录程序（单例）
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
public class MultiLoginLogger implements Logger {
    @Getter
    private static final MultiLoginLogger instance = new MultiLoginLogger();

    private File loggerFolder;
    private File tempFolder;
    private BaseLoggerBridge loggerBridge;
    private FileLoggerWriteHandler fileLoggerWriteHandler;

    public boolean canInit() {
        if (loggerBridge == null || loggerFolder == null) return false;
        try {
            Class.forName("org.apache.logging.log4j.core.LoggerContext");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * 初始化操作
     */
    public synchronized void init() throws LoggerLoadFailedException {
        if (fileLoggerWriteHandler != null) return;
        try {
            this.fileLoggerWriteHandler = new FileLoggerWriteHandler();
            fileLoggerWriteHandler.init(loggerFolder, tempFolder);
            Logger.LoggerProvider.setLogger(this);
        } catch (Throwable e) {
            this.fileLoggerWriteHandler = null;
            throw new LoggerLoadFailedException("Initialize.", e);
        }
    }

    @Override
    public void log(Level level, String message, Throwable throwable) {
        loggerBridge.log(level, message, throwable);
        if (fileLoggerWriteHandler != null) fileLoggerWriteHandler.log(level, message, throwable);
    }

    public synchronized void terminate() {
        Logger.LoggerProvider.setLogger(loggerBridge);
        if (fileLoggerWriteHandler != null) fileLoggerWriteHandler.terminate();
        fileLoggerWriteHandler = null;
    }
}
