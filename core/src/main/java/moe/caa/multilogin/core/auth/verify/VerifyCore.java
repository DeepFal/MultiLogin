package moe.caa.multilogin.core.auth.verify;

import lombok.Getter;
import moe.caa.multilogin.api.auth.yggdrasil.response.HasJoinedResponse;
import moe.caa.multilogin.core.auth.AuthCore;
import moe.caa.multilogin.core.auth.verify.flows.CheckDuplicateCurrentUsernameFlows;
import moe.caa.multilogin.core.auth.verify.flows.CheckNameRegularFlows;
import moe.caa.multilogin.core.auth.verify.flows.CheckWhitelistFlows;
import moe.caa.multilogin.core.auth.verify.flows.NewPlayerGenerateInGameUuidFlows;
import moe.caa.multilogin.core.config.YggdrasilService;
import moe.caa.multilogin.flows.workflows.BaseFlows;
import moe.caa.multilogin.flows.workflows.ParallelFlows;
import moe.caa.multilogin.language.LanguageHandler;
import moe.caa.multilogin.logger.Logger;

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
            // 重名检查和更新用户名
            new CheckDuplicateCurrentUsernameFlows(),
            // 白名单检查（有问题！）
            new CheckWhitelistFlows(),
            // 新玩家生成它在游戏内的UUID
            new NewPlayerGenerateInGameUuidFlows()
    );

    public VerifyCore(AuthCore authCore) {
        this.authCore = authCore;
    }

    /**
     * 进行 二次 验证
     */
    public VerifyContext verify(HasJoinedResponse response, YggdrasilService service) throws SQLException {
        VerifyContext context = new VerifyContext(response, service, authCore.getCore().getSqlManager().getUserDataHandler()
                .getInGameUuidAndCurrentUsernameAndWhitelistByOnlineUuidAndYggdrasilId(response.getId(), service.getId()));
        // 并行车间
        ParallelFlows<VerifyContext> parallelFlows = new ParallelFlows<>();
        parallelFlows.getSteps().addAll(verifyList);

        // 结果提前结束，踢出去
        if (parallelFlows.run(context) != BaseFlows.Signal.PASSED) return context;

        if (context.getOriginalData() == null) {
            context.getInGameUuid().set(context.getNewData().getValue1());

            // 写入数据
            authCore.getCore().getSqlManager().getUserDataHandler().insertNew(
                    response.getId(), service.getId(), context.getNewData().getValue1(), response.getName(), context.getNewData().getValue3()
            );

            Logger.LoggerProvider.getLogger().info(String.format("New player data. (online uuid: %s, service: %d, in game uuid: %s)",
                    response.getId(), service.getId(), context.getNewData().getValue1()
            ));
        } else {
            context.getInGameUuid().set(context.getOriginalData().getValue1());

            // 更新数据
            if (context.getOriginalData().getValue2().equals(context.getNewData().getValue2())) {
                if (context.getOriginalData().getValue3() != context.getOriginalData().getValue3()) {
                    // 更新白名单
                    authCore.getCore().getSqlManager().getUserDataHandler().updateWhitelist(response.getId(), service.getId(), context.getNewData().getValue3());
                }
            } else {
                if (context.getOriginalData().getValue3() != context.getOriginalData().getValue3()) {
                    // 更新白名单和名字
                    authCore.getCore().getSqlManager().getUserDataHandler().updateUsernameAndWhitelist(response.getId(),
                            service.getId(), context.getNewData().getValue2(), context.getNewData().getValue3());
                } else {
                    // 只更新名字
                    authCore.getCore().getSqlManager().getUserDataHandler().updateUsername(response.getId(), service.getId(), context.getNewData().getValue2());
                }
            }
        }

        if (!service.isRefuseRepeatedLogin()) {
            // 把游戏内同UUID的玩家踹出去
            authCore.getCore().getPlugin().getRunServer().getPlayerManager()
                    .kickPlayerIfOnline(context.getInGameUuid().get(), LanguageHandler.getInstance().getMessage("in_game_busy_login"));
        } else {
            if (authCore.getCore().getPlugin().getRunServer().getPlayerManager().getPlayer(context.getInGameUuid().get()) != null) {
                context.getKickMessage().set(LanguageHandler.getInstance().getMessage("auth_verify_failed_repeat_login"));
            }
        }
        return context;
    }
}
