package moe.caa.multilogin.core.main;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.util.HttpUtil;
import moe.caa.multilogin.core.util.ValueUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * 检查更新程序
 */
public class CheckUpdater {
    private final URL SOURCE;
    private final MultiCore core;
    public String latestVersion = null;
    public boolean haveUpdate = false;

    public CheckUpdater(MultiCore core) throws MalformedURLException {
        this.core = core;
        SOURCE = new URL("https://api.github.com/repos/CaaMoe/MultiLogin/contents/gradle.properties?ref=master");
    }


    /**
     * 检查更新
     */
    private void check0() throws Exception {
        // JsonObject json = JsonParser.parseString(HttpUtil.httpGet(SOURCE, 10000, 3)).getAsJsonObject();
        JsonObject json = JsonParser.parseString(HttpUtil.httpGet(SOURCE, 10000, 3)).getAsJsonObject();
        String sor = json.get("content").getAsString();
        sor = sor.substring(0, sor.length() - 1);
        String s = new String(ValueUtil.getDECODER().decode(sor), StandardCharsets.UTF_8);
        latestVersion = s.split("=")[1].trim().toLowerCase(Locale.ROOT);
    }

    public void check() {
        try {
            check0();
        } catch (Exception e) {
            MultiLogger.getLogger().log(LoggerLevel.DEBUG, "", e);
        }
        String pluginVersion = core.getPlugin().getPluginVersion().trim().toLowerCase(Locale.ROOT);
        haveUpdate = !pluginVersion.equalsIgnoreCase(latestVersion);
        if (haveUpdate) {
            MultiLogger.getLogger().log(LoggerLevel.INFO, "插件的最新版本为 " + latestVersion + "， 请注意及时更新");
        }
    }
}