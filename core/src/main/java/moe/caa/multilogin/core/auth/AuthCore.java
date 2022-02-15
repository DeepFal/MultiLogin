package moe.caa.multilogin.core.auth;

import lombok.Getter;
import moe.caa.multilogin.core.auth.yggdrasil.HasJoinedContext;
import moe.caa.multilogin.core.auth.yggdrasil.HasJoinedValidateCore;
import moe.caa.multilogin.core.auth.yggdrasil.ServiceUnavailableException;
import moe.caa.multilogin.core.main.MultiCore;

import java.sql.SQLException;

public class AuthCore {

    @Getter
    private final MultiCore core;

    @Getter
    private final HasJoinedValidateCore hasJoinedValidateCore;

    public AuthCore(MultiCore core) {
        this.core = core;
        this.hasJoinedValidateCore = new HasJoinedValidateCore(this);
    }

    public AuthResult auth(String username, String serverId, String ip) throws SQLException, ServiceUnavailableException {
        HasJoinedContext context = hasJoinedValidateCore.hasJoined(username, serverId, ip);
        return AuthResult.ofDisallowed("Unsupported");
    }
}
