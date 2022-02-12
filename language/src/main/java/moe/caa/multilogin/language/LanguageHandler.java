package moe.caa.multilogin.language;

import lombok.NoArgsConstructor;
import moe.caa.multilogin.logger.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * 代表信息文本处理程序
 */
@NoArgsConstructor
public class LanguageHandler {
    private Properties inside;
    private Properties outside;
    private boolean useOutside = false;

    /**
     * 初始化这个处理程序
     */
    public void init(String name) throws IOException {
        inside.load(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/" + name)), StandardCharsets.UTF_8));
    }

    public synchronized void loadOutside(File outsideFile) throws OutsideMessageLoadException {
        if (!outsideFile.exists()) return;
        outside = new Properties();
        try {
            outside.load(new InputStreamReader(new FileInputStream(outsideFile), StandardCharsets.UTF_8));
            useOutside = true;
        } catch (IOException e) {
            useOutside = false;
            throw new OutsideMessageLoadException(outsideFile.getAbsolutePath(), e);
        }
        for (Map.Entry<Object, Object> entry : inside.entrySet()) {
            if (!outside.containsKey(entry.getKey())) break;
            Logger.LoggerProvider.getLogger().warn("Message content is missing at node " + entry.getKey());
        }
        Logger.LoggerProvider.getLogger().info("Loaded file: " + outsideFile.getName());
    }

    /**
     * 获取一条语言文本
     */
    public String getMessage(String path, String... placeholders) {
        String ret = useOutside ? outside.getProperty(path, inside.getProperty(path)) : inside.getProperty(path);
        for (int i = 0; i < placeholders.length; i++) {
            ret = ret.replace("{" + i + "}", placeholders[i]);
        }
        return ret;
    }
}
