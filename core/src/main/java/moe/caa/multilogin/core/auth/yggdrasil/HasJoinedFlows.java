package moe.caa.multilogin.core.auth.yggdrasil;

import moe.caa.multilogin.api.auth.yggdrasil.response.HasJoinedResponse;
import moe.caa.multilogin.core.config.YggdrasilService;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.Pair;
import moe.caa.multilogin.flows.workflows.BaseFlows;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class HasJoinedFlows extends BaseFlows<HasJoinedContext> {
    private static final HttpResponse.BodyHandler<String> stringBodyHandler = HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

    private final YggdrasilService yggdrasilService;

    protected HasJoinedFlows(YggdrasilService yggdrasilService) {
        this.yggdrasilService = yggdrasilService;
    }

    @Override
    public Signal run(HasJoinedContext context) {
        try {

            HasJoinedResponse response = yggdrasilService.isPostMode() ? sendPost(context, yggdrasilService.buildPostContent(context)) : sendGet(context);
            if (response != null) {
                context.getResponse().set(new Pair<>(response, yggdrasilService));
                return Signal.PASSED;
            }
            context.getAuthenticationFailed().add(yggdrasilService);
            return Signal.TERMINATED;
        } catch (Throwable throwable) {
            context.getServiceUnavailable().put(yggdrasilService, throwable);
            return Signal.TERMINATED;
        }
    }

    /**
     * HasJoined POST 请求
     */
    private HasJoinedResponse sendPost(HasJoinedContext context, String postContent) throws URISyntaxException, IOException, InterruptedException {
        final URI uri = new URI(yggdrasilService.buildUrl(context));
        final int servicesTimeOut = MultiCore.getInstance().getConfig().getServicesTimeOut();
        final String s = sendRetry(HttpRequest.newBuilder()
                        .uri(uri)
                        .timeout(Duration.ofMillis(servicesTimeOut))
                        .POST(HttpRequest.BodyPublishers.ofString(postContent))
                        .build(),
                yggdrasilService.getAuthRetry());
        return MultiCore.getInstance().getGson().fromJson(s, HasJoinedResponse.class);
    }

    /**
     * HasJoined GET 请求
     */
    private HasJoinedResponse sendGet(HasJoinedContext context) throws URISyntaxException, IOException, InterruptedException {
        final URI uri = new URI(yggdrasilService.buildUrl(context));
        final int servicesTimeOut = MultiCore.getInstance().getConfig().getServicesTimeOut();
        final String s = sendRetry(HttpRequest.newBuilder()
                        .uri(uri)
                        .timeout(Duration.ofMillis(servicesTimeOut))
                        .GET()
                        .build(),
                yggdrasilService.getAuthRetry());
        return MultiCore.getInstance().getGson().fromJson(s, HasJoinedResponse.class);
    }

    /**
     * 重试
     */
    private String sendRetry(HttpRequest request, int remain) throws IOException, InterruptedException {
        try {
            HttpResponse<String> ret = MultiCore.getInstance().getHttpClient().send(request, stringBodyHandler);
            return ret.body();
        } catch (Throwable throwable) {
            if (remain <= 0) throw throwable;
            return sendRetry(request, remain - 1);
        }
    }
}
