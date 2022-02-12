package moe.caa.multilogin.logger;

import moe.caa.multilogin.logger.bridges.BaseLoggerBridge;

import java.io.File;

/**
 * MultiLogin 日志记录程序
 */
public class MultiLoginLogger implements Logger {
    private final File loggerFolder;
    private final BaseLoggerBridge loggerBridge;
    private FileLoggerWriteHandler fileLoggerWriteHandler;

    /**
     * 构建这个日志记录程序
     */
    public MultiLoginLogger(BaseLoggerBridge loggerBridge, File loggerFolder) {
        LoggerProvider.setLogger(this);
        this.loggerFolder = loggerFolder;
        this.loggerBridge = loggerBridge;
    }

    public static boolean canInit() {
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
            fileLoggerWriteHandler.init(loggerFolder);
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
        if (fileLoggerWriteHandler != null) fileLoggerWriteHandler.terminate();
        fileLoggerWriteHandler = null;
    }
}
