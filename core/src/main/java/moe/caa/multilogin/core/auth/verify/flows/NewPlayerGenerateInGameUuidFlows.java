package moe.caa.multilogin.core.auth.verify.flows;

import moe.caa.multilogin.core.auth.verify.VerifyContext;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.flows.workflows.BaseFlows;
import moe.caa.multilogin.language.LanguageHandler;
import moe.caa.multilogin.logger.Logger;

import java.sql.SQLException;
import java.util.UUID;

/**
 * 新玩家生成它在游戏内的UUID
 */
public class NewPlayerGenerateInGameUuidFlows extends BaseFlows<VerifyContext> {

    @Override
    public Signal run(VerifyContext verifyContext) {
        if (verifyContext.getOriginalData() != null) return Signal.PASSED;
        try {
            // 编排游戏内UUID
            UUID uuid = verifyContext.getService().getTransformUuid().getResultUuid(verifyContext.getResponse().getId(), verifyContext.getResponse().getName());
            // 如果按照规则生成的UUID被使用，就随机一个出来

            if (MultiCore.getInstance().getSqlManager().getUserDataHandler().hasUseByInGameUuid(uuid)) {
                UUID nextInGameUuid = MultiCore.getInstance().getSqlManager().getUserDataHandler().getNextInGameUuid();
                // 打印日志
                Logger.LoggerProvider.getLogger().warn(
                        String.format("The UUID generated according to the rule is %s, which has been used. Set to %s. (onlineUuid: %s, username: %s, service: %d)",
                                uuid, nextInGameUuid, verifyContext.getResponse().getId(), verifyContext.getResponse().getName(), verifyContext.getService().getId()
                        )
                );
                uuid = nextInGameUuid;
            }
            verifyContext.getNewData().setValue1(uuid);
            return Signal.PASSED;
        } catch (SQLException e) {
            verifyContext.getKickMessage().set(LanguageHandler.getInstance().getMessage("auth_verify_error"));
            Logger.LoggerProvider.getLogger().error("An exception was encountered while processing the generation of in game uuid.", e);
            return Signal.TERMINATED;
        }
    }
}
