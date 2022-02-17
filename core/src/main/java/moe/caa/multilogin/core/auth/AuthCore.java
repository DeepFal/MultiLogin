package moe.caa.multilogin.core.auth;

import lombok.Getter;
import moe.caa.multilogin.api.auth.Auth;
import moe.caa.multilogin.core.auth.verify.VerifyContext;
import moe.caa.multilogin.core.auth.verify.VerifyCore;
import moe.caa.multilogin.core.auth.yggdrasil.HasJoinedContext;
import moe.caa.multilogin.core.auth.yggdrasil.HasJoinedValidateCore;
import moe.caa.multilogin.core.auth.yggdrasil.ServiceUnavailableException;
import moe.caa.multilogin.core.config.YggdrasilService;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.language.LanguageHandler;
import moe.caa.multilogin.logger.Logger;

import java.sql.SQLException;
import java.util.Map;

public class AuthCore implements Auth {

    @Getter
    private final MultiCore core;

    @Getter
    private final HasJoinedValidateCore hasJoinedValidateCore;

    @Getter
    private final VerifyCore verifyCore;

    public AuthCore(MultiCore core) {
        this.core = core;
        this.hasJoinedValidateCore = new HasJoinedValidateCore(this);
        this.verifyCore = new VerifyCore(this);
    }

    @Override
    public AuthResultImpl auth(String username, String serverId, String ip) {
        Logger.LoggerProvider.getLogger().debug(String.format("Processing login requests. (username: %s, serverId: %s, ip: %s)", username, serverId, ip));

        if (core.getConfig().getYggdrasilServices().stream().noneMatch(YggdrasilService::isEnable)) {
            final String message = LanguageHandler.getInstance().getMessage("auth_yggdrasil_failed_no_server");
            return disallowed(username, serverId, ip, message);
        }

        // HasJoined 验证
        HasJoinedContext context;
        try {
            context = hasJoinedValidateCore.hasJoined(username, serverId, ip);
        } catch (SQLException e) {
            Logger.LoggerProvider.getLogger().error("An exception was encountered while processing yggdrasil authentication.", e);
            final String message = LanguageHandler.getInstance().getMessage("auth_yggdrasil_error");
            return disallowed(username, serverId, ip, message);
        }

        // 踢出
        if (context.getResponse().get() == null) {
            // 打印验证失败的详细信息
            Logger.LoggerProvider.getLogger().debug(String.format("Yggdrasil authentication of %s failed, with %d exceptions and %d authentication failures.",
                    username, context.getServiceUnavailable().size(), context.getAuthenticationFailed().size()
            ));

            // 打印验证失败的详细报错
            for (Map.Entry<YggdrasilService, Throwable> entry : context.getServiceUnavailable().entrySet()) {
                final String format = String.format("From username %s, service %d, serverId %s, ip %s.",
                        username, entry.getKey().getId(), serverId, ip
                );
                Logger.LoggerProvider.getLogger().debug(format, new ServiceUnavailableException(
                        format, entry.getValue()));
            }

            // 踹出去
            if (context.getServiceUnavailable().size() == 0) {
                final String message = LanguageHandler.getInstance().getMessage("auth_yggdrasil_failed_validation_failed");
                return disallowed(username, serverId, ip, message);
            } else {
                final String message = LanguageHandler.getInstance().getMessage("auth_yggdrasil_failed_server_down");
                return disallowed(username, serverId, ip, message);
            }
        }

        // 二次验证
        VerifyContext verify;
        try {
            verify = verifyCore.verify(context.getResponse().get().getValue1(), context.getResponse().get().getValue2());
        } catch (SQLException e) {
            Logger.LoggerProvider.getLogger().error("An exception was encountered while processing validation.", e);
            final String message = LanguageHandler.getInstance().getMessage("auth_verify_error");
            return disallowed(username, serverId, ip, context.getResponse().get().getValue2().getId(), message);
        }
        final String s = verify.getKickMessage().get();
        if (s != null) {
            return disallowed(username, serverId, ip, context.getResponse().get().getValue2().getId(), s);
        }
        Logger.LoggerProvider.getLogger().debug(String.format("Allowed to login. (username: %s, serverId: %s, ip: %s, service: %d)", username, serverId, ip, context.getResponse().get().getValue2().getId()));
        return AuthResultImpl.ofAllowed(context.getResponse().get().getValue1(), context.getResponse().get().getValue2());
    }

    private AuthResultImpl disallowed(String username, String serverId, String ip, String cause){
        Logger.LoggerProvider.getLogger().debug(String.format("Refused to login. (username: %s, serverId: %s, ip: %s, cause: %s)", username, serverId, ip, cause));
        return AuthResultImpl.ofDisallowed(cause);
    }

    private AuthResultImpl disallowed(String username, String serverId, String ip, int service, String cause){
        Logger.LoggerProvider.getLogger().debug(String.format("Refused to login. (username: %s, serverId: %s, ip: %s, service: %d, cause: %s)", username, serverId, ip, service, cause));
        return AuthResultImpl.ofDisallowed(cause);
    }
}