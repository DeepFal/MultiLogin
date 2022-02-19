package moe.caa.multilogin.core.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import moe.caa.multilogin.core.main.MultiCore;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

public class CheckUpdater {
    @Getter
    public final String currentVersion;
    private final URI source;
    @Getter
    public String latestVersion = null;

    public CheckUpdater() throws URISyntaxException {
        source = new URI("https://api.github.com/repos/CaaMoe/MultiLogin/contents/gradle.properties?ref=master");
        currentVersion = MultiCore.getInstance().getPlugin().getPluginVersion();
    }

    public void doCheck() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(source)
                .GET()
                .timeout(Duration.ofMillis(MultiCore.getInstance().getConfig().getServicesTimeOut())).build();
        JsonObject json = JsonParser.parseReader(new InputStreamReader(sendRetry(request, 3))).getAsJsonObject();
        String sor = json.getAsJsonPrimitive("content").getAsString();
        sor = sor.substring(0, sor.length() - 1);
        latestVersion = new String(Base64.getDecoder().decode(sor), StandardCharsets.UTF_8).split("=")[1].trim();
    }

    public boolean shouldUpdate() {
        if (latestVersion == null) return false;
        return !currentVersion.equalsIgnoreCase(latestVersion);
    }

    /**
     * 重试
     */
    private InputStream sendRetry(HttpRequest request, int remain) throws IOException, InterruptedException {
        try {
            HttpResponse<InputStream> ret = MultiCore.getInstance().getHttpClient().send(request, HttpResponse.BodyHandlers.ofInputStream());
            return ret.body();
        } catch (Throwable throwable) {
            if (remain <= 0) throw throwable;
            return sendRetry(request, remain - 1);
        }
    }
}
