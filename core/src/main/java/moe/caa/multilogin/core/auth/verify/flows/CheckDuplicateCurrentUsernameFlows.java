package moe.caa.multilogin.core.auth.verify.flows;

import moe.caa.multilogin.core.auth.verify.VerifyContext;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.FormatContent;
import moe.caa.multilogin.flows.workflows.BaseFlows;
import moe.caa.multilogin.language.LanguageHandler;
import moe.caa.multilogin.logger.Logger;

import java.sql.SQLException;

/**
 * 重名检查和更新用户名
 */
public class CheckDuplicateCurrentUsernameFlows extends BaseFlows<VerifyContext> {
    @Override
    public Signal run(VerifyContext verifyContext) {
        // 重名检查有点复杂，就是还要考虑到 inGameUuid 相同的情况。
        try {
            // 数据库记录没有对应的值
            if (verifyContext.getOriginalData() == null) {
                // 这时按照新玩家来处理，只需要判断当前用户名在数据库中有没有记录就可以了
                // 如果名字被使用，踹出去
                if (MultiCore.getInstance().getSqlManager().getUserDataHandler()
                        .hasUseByCurrentUsername(verifyContext.getResponse().getName())) {
                    verifyContext.getKickMessage().set(FormatContent.createContent(
                            FormatContent.FormatEntry.builder().name("current_name").content(verifyContext.getResponse().getName()).build()
                    ).format(LanguageHandler.getInstance().getMessage("auth_verify_failed_username_repeated")));
                    return Signal.TERMINATED;
                } else {
                    // 修改数据库中的值
                    verifyContext.getNewData().setValue2(verifyContext.getResponse().getName());
                }
                return Signal.PASSED;
            }

            // 玩家仍然用老的名字登录，通过
            if (verifyContext.getOriginalData().getValue2().equals(verifyContext.getResponse().getName())) {
                return Signal.PASSED;
            }

            // 检查玩家的新的名字到底能不能用
            // 判断当前用户名在数据库中有没有记录
            if (MultiCore.getInstance().getSqlManager().getUserDataHandler()
                    .hasUseByCurrentUsername(verifyContext.getResponse().getName())) {

                // 如果有记录，判断它能不能强制使用这个名字（判断当前拥有它的名字的 inGameUuid 是一样的吗）
                if (MultiCore.getInstance().getSqlManager().getUserDataHandler().hasUseByCurrentUsernameAndInGameUuid(
                        verifyContext.getResponse().getName(), verifyContext.getOriginalData().getValue1()
                )) {
                    // 修改数据库中的值
                    verifyContext.getNewData().setValue2(verifyContext.getResponse().getName());
                } else {

                    // 不是它用的，踹出去
                    verifyContext.getKickMessage().set(FormatContent.createContent(
                            FormatContent.FormatEntry.builder().name("current_name").content(verifyContext.getResponse().getName()).build()
                    ).format(LanguageHandler.getInstance().getMessage("auth_verify_failed_username_repeated")));
                    return Signal.TERMINATED;
                }
            } else {
                // 修改数据库中的值
                verifyContext.getNewData().setValue2(verifyContext.getResponse().getName());
            }

            return Signal.PASSED;
        } catch (SQLException e) {
            verifyContext.getKickMessage().set(LanguageHandler.getInstance().getMessage("auth_verify_error"));
            Logger.LoggerProvider.getLogger().error("An exception was encountered while handling duplicate name checks.", e);
            return Signal.TERMINATED;
        }
    }
}
