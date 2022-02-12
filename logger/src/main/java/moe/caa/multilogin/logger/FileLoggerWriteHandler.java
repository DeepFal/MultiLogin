package moe.caa.multilogin.logger;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import moe.caa.multilogin.logger.bridges.Log4JLoggerBridge;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 日志文件写入程序，使用 log4j<br>
 * 此实例仅能通过 MultiLogger 来访问<br>
 * 请确保当前类或父加载器包含 log4j 库<br>
 *
 * @see MultiLoginLogger
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileLoggerWriteHandler implements Logger {
    private Log4JLoggerBridge toFileLogger;
    private LoggerContext context;

    /**
     * 初始化这个日志记录程序
     */
    protected void init(File folder) throws IOException {
        File tempFile = File.createTempFile("log4j2-temp", "multilogin");
        tempFile.deleteOnExit();
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("multilogin_log4j2.xml"))));
        String rePlacePath = folder.getAbsolutePath();
        try (BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(tempFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                fw.write(line.replace("%path%", rePlacePath));
                fw.write('\n');
            }
            fw.flush();
        }
        context = new LoggerContext("MultiLogin_To_Logfile");
        context.setConfigLocation(tempFile.toURI());
        context.reconfigure();
        toFileLogger = new Log4JLoggerBridge(context.getLogger("MultiLogin_To_Logfile"));
    }

    protected void terminate() {
        if (context != null) {
            context.terminate();
        }
    }

    @Override
    public void log(Level level, String message, Throwable throwable) {
        toFileLogger.log(level, message, throwable);
    }
}