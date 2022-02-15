package moe.caa.multilogin.core.auth.verify;

import lombok.Getter;
import moe.caa.multilogin.api.auth.yggdrasil.response.HasJoinedResponse;
import moe.caa.multilogin.core.auth.AuthCore;
import moe.caa.multilogin.core.auth.verify.flows.CheckNameRegularFlows;
import moe.caa.multilogin.core.auth.verify.flows.CheckWhitelistFlows;
import moe.caa.multilogin.core.config.YggdrasilService;
import moe.caa.multilogin.flows.workflows.BaseFlows;
import moe.caa.multilogin.flows.workflows.ParallelFlows;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * 二次验证核心
 */
public class VerifyCore {
    @Getter
    private final AuthCore authCore;

    private final List<BaseFlows<VerifyContext>> verifyList = Arrays.asList(
            // 名字正则检查
            new CheckNameRegularFlows(),
            // 白名单检查
            new CheckWhitelistFlows()
    );

    public VerifyCore(AuthCore authCore) {
        this.authCore = authCore;
    }

    /**
     * 进行 二次 验证
     */
    public VerifyContext verify(HasJoinedResponse response, YggdrasilService service) throws SQLException {
        VerifyContext context = new VerifyContext(this, response, service,
                authCore.getCore().getSqlManager().getUserDataHandler().hasExists(response.getId(), service.getId()));
        // 并行车间
        ParallelFlows<VerifyContext> parallelFlows = new ParallelFlows<>();
        parallelFlows.getSteps().addAll(verifyList);
        parallelFlows.run(context);
        return context;
    }
}
