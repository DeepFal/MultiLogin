package moe.caa.multilogin.core.auth.yggdrasil;

import lombok.Data;
import moe.caa.multilogin.core.auth.yggdrasil.response.HasJoinedResponse;
import moe.caa.multilogin.core.config.YggdrasilService;
import moe.caa.multilogin.core.util.Pair;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Data
public class HasJoinedContext {
    private final String username;
    private final String serverId;
    private final String ip;

    // 存放成功的标志
    private final AtomicReference<Pair<HasJoinedResponse, YggdrasilService>> response = new AtomicReference<>();

    // 存放异常的
    private final Map<YggdrasilService, Throwable> serviceUnavailable = new ConcurrentHashMap<>();

    // 存放没有通过验证的
    private final Set<YggdrasilService> authenticationFailed = ConcurrentHashMap.newKeySet();

    protected HasJoinedContext(String username, String serverId, String ip) {
        this.username = username;
        this.serverId = serverId;
        this.ip = ip;
    }
}
