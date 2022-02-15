package moe.caa.multilogin.core.auth.yggdrasil;

import moe.caa.multilogin.core.auth.AuthCore;
import moe.caa.multilogin.core.config.YggdrasilService;
import moe.caa.multilogin.flows.workflows.EntrustFlows;
import moe.caa.multilogin.flows.workflows.BaseFlows;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * HasJoined 验证核心
 */
public class HasJoinedValidateCore {
    private final AuthCore authCore;

    public HasJoinedValidateCore(AuthCore authCore) {
        this.authCore = authCore;
    }

    /**
     * 进行 HasJoined 验证
     */
    public HasJoinedContext hasJoined(String username, String serverId, String ip) throws SQLException {
        HasJoinedContext context = new HasJoinedContext(username, serverId, ip);
        // 验证队列排序
        final Set<Integer> yggdrasilIdByCurrentUsername =
                authCore.getCore().getSqlManager().getUserDataHandler().getYggdrasilIdByCurrentUsername(username);
        List<YggdrasilService> one = new ArrayList<>();
        List<YggdrasilService> two = new ArrayList<>();
        for (YggdrasilService service : authCore.getCore().getConfig().getYggdrasilServices()) {
            if (yggdrasilIdByCurrentUsername.contains(service.getId())) one.add(service);
            else two.add(service);
        }

        // 新建第一个车间，委托车间
        EntrustFlows<HasJoinedContext> entrustFlows = new EntrustFlows<>();
        for (YggdrasilService service : one) {
            entrustFlows.getSteps().add(
                    new HasJoinedFlows(service)
            );
        }
        // 第一轮有结果就直接返回
        if (entrustFlows.run(context) == BaseFlows.Signal.PASSED) return context;

        // 新建第二个车间，委托车间
        entrustFlows = new EntrustFlows<>();
        for (YggdrasilService service : two) {
            entrustFlows.getSteps().add(
                    new HasJoinedFlows(service)
            );
        }
        entrustFlows.run(context);
        return context;
    }
}
