package moe.caa.multilogin.core.skinrestorer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import moe.caa.multilogin.api.auth.yggdrasil.response.Property;
import moe.caa.multilogin.core.config.SkinRestorerMethodType;
import moe.caa.multilogin.core.config.YggdrasilService;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.ValueUtil;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class SkinRestorerTask {
    private static final HttpResponse.BodyHandler<String> stringBodyHandler = HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
    private static final HttpResponse.BodyHandler<InputStream> inputStreamBodyHandler = HttpResponse.BodyHandlers.ofInputStream();
    private final String skinUrl;
    private final String skinModel;
    private final YggdrasilService service;

    public Property doRestorer() throws URISyntaxException, IOException, InterruptedException {
        if (service.getSkinRestorerMethod() == SkinRestorerMethodType.URL) {
            return doUrl();
        } else {
            return doUpload();
        }
    }

    // TODO: 2022/2/17 皮肤问题
    private Property doUpload() throws IOException, URISyntaxException, InterruptedException {
        // 下载皮肤文件
        try (final InputStream stream = sendRetry(inputStreamBodyHandler, HttpRequest.newBuilder()
                        .uri(new URI(skinUrl))
                        .timeout(Duration.ofMillis(MultiCore.getInstance().getConfig().getServicesTimeOut()))
                        .build(),
                service.getSkinRestorerRetry(), 0);
             BufferedInputStream bis = new BufferedInputStream(stream);
        ) {

            // 上传到那个啥mineskin.org
            final String format = String.format("variant=%s&name=%s&visibility=0&file=%s",
                    skinModel == null ? "classic" : skinModel,
                    UUID.randomUUID().toString().substring(0, 6),
                    ValueUtil.getBinaryStringByByteArray(bis.readAllBytes())
            );
            final String json = sendRetry(stringBodyHandler, HttpRequest.newBuilder()
                            .uri(new URI("https://api.mineskin.org/generate/upload"))
                            .timeout(Duration.ofMillis(MultiCore.getInstance().getConfig().getServicesTimeOut()))
                            .header("User-Agent", "MultiLogin/v2.0")
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .POST(HttpRequest.BodyPublishers.ofByteArray(format.getBytes()))
                            .build(), service.getSkinRestorerRetry()
                    , MultiCore.getInstance().getConfig().getSkinRestorerRetryDelay());
            System.out.println(json);
            final JsonObject jsonObject = JsonParser.parseString(json
            ).getAsJsonObject().getAsJsonObject("data").getAsJsonObject("texture");
            return Property.builder().name("")
                    .value(jsonObject.getAsJsonPrimitive("value").getAsString())
                    .signature(jsonObject.getAsJsonPrimitive("signature").getAsString()).build();
        }
    }

    private Property doUrl() throws URISyntaxException, IOException, InterruptedException {
        JsonObject jo = new JsonObject();
        jo.addProperty("name", UUID.randomUUID().toString().substring(0, 6));
        jo.addProperty("variant", skinModel == null ? "classic" : skinModel);
        jo.addProperty("visibility", 0);
        jo.addProperty("url", skinUrl);

        final String json = sendRetry(stringBodyHandler, HttpRequest.newBuilder()
                        .uri(new URI("https://api.mineskin.org/generate/url"))
                        .timeout(Duration.ofMillis(MultiCore.getInstance().getConfig().getServicesTimeOut()))
                        .header("User-Agent", "MultiLogin/v2.0")
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(MultiCore.getInstance().getGson().toJson(jo)))
                        .build(), service.getSkinRestorerRetry()
                , MultiCore.getInstance().getConfig().getSkinRestorerRetryDelay());
        final JsonObject jsonObject = JsonParser.parseString(json
        ).getAsJsonObject().getAsJsonObject("data").getAsJsonObject("texture");

        return Property.builder().name("")
                .value(jsonObject.getAsJsonPrimitive("value").getAsString())
                .signature(jsonObject.getAsJsonPrimitive("signature").getAsString()).build();
    }

    /**
     * 重试
     */
    private <T> T sendRetry(HttpResponse.BodyHandler<T> bodyHandler, HttpRequest request, int remain, int delay) throws IOException, InterruptedException {
        try {
            HttpResponse<T> ret = MultiCore.getInstance().getHttpClient().send(request, bodyHandler);
            return ret.body();
        } catch (Throwable throwable) {
            if (remain <= 0) throw throwable;
            Thread.sleep(delay);
            return sendRetry(bodyHandler, request, remain - 1, delay);
        }
    }
}
