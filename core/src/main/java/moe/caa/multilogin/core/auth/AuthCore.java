package moe.caa.multilogin.core.auth;

import lombok.Getter;
import lombok.SneakyThrows;
import moe.caa.multilogin.api.auth.Auth;
import moe.caa.multilogin.core.auth.yggdrasil.HasJoinedContext;
import moe.caa.multilogin.core.auth.yggdrasil.HasJoinedValidateCore;
import moe.caa.multilogin.core.auth.yggdrasil.ServiceUnavailableException;
import moe.caa.multilogin.core.config.YggdrasilService;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.logger.Logger;

import java.util.Map;

public class AuthCore implements Auth {

    @Getter
    private final MultiCore core;

    @Getter
    private final HasJoinedValidateCore hasJoinedValidateCore;

    public AuthCore(MultiCore core) {
        this.core = core;
        this.hasJoinedValidateCore = new HasJoinedValidateCore(this);
    }

    @SneakyThrows
    @Override
    public AuthResultImpl auth(String username, String serverId, String ip) {
        HasJoinedContext context = hasJoinedValidateCore.hasJoined(username, serverId, ip);
        Logger.LoggerProvider.getLogger().debug(String.format("Yggdrasil authentication of %s is complete, with %d exceptions and %d authentication failures, %s.",
                username, context.getServiceUnavailable().size(), context.getAuthenticationFailed().size(),
                context.getResponse().get() == null ? "finally failed to authenticate" : " finally authenticated"
        ));

        for (Map.Entry<YggdrasilService, Throwable> entry : context.getServiceUnavailable().entrySet()) {
            final String format = String.format("From username %s, service %d (%s), serverId %s, ip %s.",
                    username, entry.getKey().getId(), entry.getKey().getName(), serverId, ip
            );
            Logger.LoggerProvider.getLogger().debug(format, new ServiceUnavailableException(
                    format, entry.getValue()));
        }

        return AuthResultImpl.ofDisallowed("Unsupported");
    }
}
