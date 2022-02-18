package moe.caa.multilogin.core.skinrestorer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import moe.caa.multilogin.api.auth.yggdrasil.response.Property;
import moe.caa.multilogin.core.config.SkinRestorerMethodType;
import moe.caa.multilogin.core.config.YggdrasilService;
import moe.caa.multilogin.core.main.MultiCore;

import javax.imageio.ImageIO;
import java.io.*;
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

    private Property doUpload() throws IOException, URISyntaxException, InterruptedException {
        final File output = File.createTempFile("textures-", "-multilogin-skinrestorer.png", MultiCore.getInstance().getPlugin().getTempFolder());
        output.deleteOnExit();
        // 下载皮肤文件
        try (final InputStream stream = sendRetry(inputStreamBodyHandler, HttpRequest.newBuilder()
                        .uri(new URI(skinUrl))
                        .timeout(Duration.ofMillis(MultiCore.getInstance().getConfig().getServicesTimeOut()))
                        .build(),
                service.getSkinRestorerRetry(), 0);

             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintWriter pw = new PrintWriter(baos, true);
        ) {
            final String replace = UUID.randomUUID().toString().replace("-", "");
            ImageIO.write(ImageIO.read(stream), "png", output);
            BufferedInputStream imgReader = new BufferedInputStream(new FileInputStream(output));
            final byte[] imgData = imgReader.readAllBytes();
            imgReader.close();

            // 上传到那个啥mineskin.org
            String boundary = "------ML@" + replace;
            String CRLF = "\r\n";

            // 写文件
            pw.append(boundary).append(CRLF);
            pw.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(replace).append(".png\"").append(CRLF);
            pw.append("Content-Type: image/png").append(CRLF).append(CRLF);
            pw.flush();
            baos.write(imgData);
            baos.flush();
            pw.append(CRLF);

            // 写name
            pw.append(boundary).append(CRLF);
            pw.append("Content-Disposition: form-data; name=\"name\"").append(CRLF).append(CRLF);
            pw.append(replace, 0, 6).append(CRLF);

            // 写variant
            pw.append(boundary).append(CRLF);
            pw.append("Content-Disposition: form-data; name=\"variant\"").append(CRLF).append(CRLF);
            pw.append(skinModel == null ? "classic" : skinModel).append(CRLF);

            // 写visibility
            pw.append(boundary).append(CRLF);
            pw.append("Content-Disposition: form-data; name=\"visibility\"").append(CRLF).append(CRLF);
            pw.append("0").append(CRLF);

            pw.append(boundary).append("--").append(CRLF);
            pw.flush();

            final String json = sendRetry(stringBodyHandler, HttpRequest.newBuilder()
                            .uri(new URI("https://api.mineskin.org/generate/upload"))
                            .timeout(Duration.ofMillis(MultiCore.getInstance().getConfig().getServicesTimeOut()))
                            .header("User-Agent", "MultiLogin/v2.0")
                            .header("Content-Type", "multipart/form-data; boundary=" + boundary.substring(2))
                            .POST(HttpRequest.BodyPublishers.ofByteArray(baos.toByteArray()))
                            .build(), service.getSkinRestorerRetry()
                    , MultiCore.getInstance().getConfig().getSkinRestorerRetryDelay());
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
