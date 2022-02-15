package moe.caa.multilogin.core.auth.yggdrasil;

import moe.caa.multilogin.core.auth.yggdrasil.response.HasJoinedResponse;
import moe.caa.multilogin.core.config.YggdrasilService;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.flows.workflows.IFlows;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HasJoinedFlows extends IFlows<HasJoinedContext> {
    private static final HttpResponse.BodyHandler<InputStream> inputStreamBodyHandler = HttpResponse.BodyHandlers.ofInputStream();

    private final YggdrasilService yggdrasilService;

    protected HasJoinedFlows(YggdrasilService yggdrasilService) {
        this.yggdrasilService = yggdrasilService;
    }

    @Override
    public Signal run(HasJoinedContext context) {
        try {
            HasJoinedResponse response = yggdrasilService.isPostMode() ? sendPost(context, yggdrasilService.buildPostContent(context)) : sendGet(context);
            if (response != null) return Signal.PASSED;
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
        return MultiCore.getInstance().getGson().fromJson(new InputStreamReader(
                sendRetry(HttpRequest.newBuilder()
                                .uri(new URI(yggdrasilService.buildUrl(context)))
                                .timeout(Duration.ofMillis(MultiCore.getInstance().getConfig().getServicesTimeOut()))
                                .POST(HttpRequest.BodyPublishers.ofString(postContent))
                                .build(),
                        yggdrasilService.getAuthRetry())
        ), HasJoinedResponse.class);
    }

    /**
     * HasJoined GET 请求
     */
    private HasJoinedResponse sendGet(HasJoinedContext context) throws URISyntaxException, IOException, InterruptedException {
        return MultiCore.getInstance().getGson().fromJson(new InputStreamReader(
                sendRetry(HttpRequest.newBuilder()
                                .uri(new URI(yggdrasilService.buildUrl(context)))
                                .timeout(Duration.ofMillis(MultiCore.getInstance().getConfig().getServicesTimeOut()))
                                .GET()
                                .build(),
                        yggdrasilService.getAuthRetry())
        ), HasJoinedResponse.class);
    }

    /**
     * 重试
     */
    private InputStream sendRetry(HttpRequest request, int remain) throws IOException, InterruptedException {
        try {
            HttpResponse<InputStream> ret = MultiCore.getInstance().getHttpClient().send(request, inputStreamBodyHandler);
            return ret.body();
        } catch (Throwable throwable) {
            if (remain <= 0) throw throwable;
            return sendRetry(request, remain - 1);
        }
    }
}
