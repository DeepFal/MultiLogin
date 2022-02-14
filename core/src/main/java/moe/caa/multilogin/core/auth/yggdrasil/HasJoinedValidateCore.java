package moe.caa.multilogin.core.auth.yggdrasil;

import moe.caa.multilogin.core.auth.AuthCore;
import moe.caa.multilogin.core.auth.yggdrasil.response.HasJoinedResponse;
import moe.caa.multilogin.core.config.YggdrasilService;
import moe.caa.multilogin.flows.FlowContext;
import moe.caa.multilogin.flows.workflows.EntrustFlows;

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

    public void init() {

    }

    /**
     * 进行 HasJoined 验证
     */
    public HasJoinedResponse hasJoined(String username, String serverId, String ip) throws SQLException, ServiceUnavailableException {
        // TODO: 2022/2/14 YggdrasilService 被吞了？稍后再丢出去

        HasJoinedContext context = new HasJoinedContext(username, serverId, ip);

        // 服务器宕机
        boolean serverDown = false;

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
                    new HasJoinedFlows(service, String.format("%s -> [%s, %s, %s]", service.getName(), username, service, ip))
            );
        }
        HasJoinedContext run = entrustFlows.run(context);

        // 第一轮有结果直接返回
        if (run.getSignal() == FlowContext.Signal.PASS) return run.getResponse();
        // 有报错，宕机
        if (run.getSignal() == FlowContext.Signal.ERROR) serverDown = true;

        // 新建第二个车间，委托车间
        entrustFlows = new EntrustFlows<>();
        for (YggdrasilService service : two) {
            entrustFlows.getSteps().add(
                    new HasJoinedFlows(service, String.format("%s -> [%s, %s, %s]", service.getName(), username, service, ip))
            );
        }
        run = entrustFlows.run(context);

        // 第二轮有结果直接返回
        if (run.getSignal() == FlowContext.Signal.PASS) return run.getResponse();
        // 有报错，宕机
        if (run.getSignal() == FlowContext.Signal.ERROR) serverDown = true;

        if (!serverDown) return null;
        // 宕机返回报错
        throw new ServiceUnavailableException();
    }
}
