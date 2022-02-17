package moe.caa.multilogin.core.auth;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import moe.caa.multilogin.api.auth.AuthResult;
import moe.caa.multilogin.api.auth.yggdrasil.response.HasJoinedResponse;
import moe.caa.multilogin.core.config.YggdrasilService;

/**
 * 返回登录结果
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthResultImpl implements AuthResult {
    private final HasJoinedResponse response;
    private final YggdrasilService service;
    private final String kickMessage;

    /**
     * 构建成功登录的结果
     */
    protected static AuthResultImpl ofAllowed(HasJoinedResponse response, YggdrasilService service) {
        return new AuthResultImpl(response, service, null);
    }

    /**
     * 构建失败登录的结果
     */
    protected static AuthResultImpl ofDisallowed(String kickMessage) {
        return new AuthResultImpl(null, null, kickMessage);
    }

    public boolean hasPassed() {
        return response != null;
    }
}
