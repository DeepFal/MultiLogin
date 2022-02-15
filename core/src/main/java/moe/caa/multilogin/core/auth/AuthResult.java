package moe.caa.multilogin.core.auth;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import moe.caa.multilogin.core.auth.yggdrasil.response.HasJoinedResponse;
import moe.caa.multilogin.core.config.YggdrasilService;

/**
 * 返回登录结果
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthResult {
    private final HasJoinedResponse response;
    private final YggdrasilService service;
    private final String kickMessage;

    public boolean hasPassed(){
        return response != null;
    }

    /**
     * 构建成功登录的结果
     */
    public static AuthResult ofAllowed(HasJoinedResponse response, YggdrasilService service){
        return new AuthResult(response, service, null);
    }

    /**
     * 构建失败登录的结果
     */
    public static AuthResult ofDisallowed(String kickMessage){
        return new AuthResult(null, null, kickMessage);
    }
}
