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
            return AuthResultImpl.ofDisallowed(LanguageHandler.getInstance().getMessage("auth_yggdrasil_failed_no_server"));
        }

        // HasJoined 验证
        HasJoinedContext context;
        try {
            context = hasJoinedValidateCore.hasJoined(username, serverId, ip);
        } catch (SQLException e) {
            Logger.LoggerProvider.getLogger().error("An exception was encountered while processing yggdrasil authentication.", e);
            return AuthResultImpl.ofDisallowed(LanguageHandler.getInstance().getMessage("auth_yggdrasil_error"));
        }

        // 打印验证信息
        Logger.LoggerProvider.getLogger().debug(String.format("Yggdrasil authentication of %s is complete, with %d exceptions and %d authentication failures, %s.",
                username, context.getServiceUnavailable().size(), context.getAuthenticationFailed().size(),
                context.getResponse().get() == null ? "finally failed to authenticate" : " finally authenticated"
        ));

        // 打印验证时报错
        for (Map.Entry<YggdrasilService, Throwable> entry : context.getServiceUnavailable().entrySet()) {
            final String format = String.format("From username %s, service %d (%s), serverId %s, ip %s.",
                    username, entry.getKey().getId(), entry.getKey().getName(), serverId, ip
            );
            Logger.LoggerProvider.getLogger().debug(format, new ServiceUnavailableException(
                    format, entry.getValue()));
        }

        // 踢出
        if (context.getResponse().get() == null) {
            if (context.getServiceUnavailable().size() == 0) {
                return AuthResultImpl.ofDisallowed(LanguageHandler.getInstance().getMessage("auth_yggdrasil_failed_validation_failed"));
            } else {
                return AuthResultImpl.ofDisallowed(LanguageHandler.getInstance().getMessage("auth_yggdrasil_failed_server_down"));
            }
        }

        // 二次验证
        VerifyContext verify;
        try {
            verify = verifyCore.verify(context.getResponse().get().getValue1(), context.getResponse().get().getValue2());
        } catch (SQLException e) {
            Logger.LoggerProvider.getLogger().error("An exception was encountered while processing validation.", e);
            return AuthResultImpl.ofDisallowed(LanguageHandler.getInstance().getMessage("auth_verify_error"));
        }
        final String s = verify.getKickMessage().get();
        if (s != null) {
            AuthResultImpl.ofDisallowed(s);
        }
        return AuthResultImpl.ofAllowed(context.getResponse().get().getValue1(), context.getResponse().get().getValue2());
    }
}