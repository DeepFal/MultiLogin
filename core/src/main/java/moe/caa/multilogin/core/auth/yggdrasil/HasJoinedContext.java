package moe.caa.multilogin.core.auth.yggdrasil;

import lombok.Data;
import lombok.EqualsAndHashCode;
import moe.caa.multilogin.core.auth.yggdrasil.response.HasJoinedResponse;
import moe.caa.multilogin.flows.FlowContext;

@EqualsAndHashCode(callSuper = true)
@Data
public class HasJoinedContext extends FlowContext {
    private final String username;
    private final String serverId;
    private final String ip;
    private HasJoinedResponse response;

    public HasJoinedContext(String username, String serverId, String ip) {
        this.username = username;
        this.serverId = serverId;
        this.ip = ip;
    }

    @Override
    public HasJoinedContext clone() {
        return new HasJoinedContext(username, serverId, ip);
    }
}
