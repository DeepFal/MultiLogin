package moe.caa.multilogin.core.auth.verify.flows;

import moe.caa.multilogin.core.auth.verify.VerifyContext;
import moe.caa.multilogin.core.database.SQLManager;
import moe.caa.multilogin.flows.workflows.BaseFlows;
import moe.caa.multilogin.language.LanguageHandler;
import moe.caa.multilogin.logger.Logger;

import java.sql.SQLException;

/**
 * 白名单检查
 */
public class CheckWhitelistFlows extends BaseFlows<VerifyContext> {

    @Override
    public Signal run(VerifyContext verifyContext) {
        try {
            final SQLManager sqlManager = verifyContext.getVerifyCore().getAuthCore().getCore().getSqlManager();
            if (sqlManager.getUserDataHandler()
                    .hasWhitelist(verifyContext.getResponse().getId(), verifyContext.getService().getId()))
                return Signal.PASSED;
            if (sqlManager.getCacheWhitelistDataHandler().hasWhitelistAndRemove(
                    verifyContext.getResponse().getName(),
                    verifyContext.getResponse().getId(),
                    verifyContext.getService().getId()
            )) {
                return Signal.PASSED;
            }
            verifyContext.getKickMessage().set(LanguageHandler.getInstance().getMessage("auth_verify_failed_no_whitelist"));
            return Signal.TERMINATED;
        } catch (SQLException e) {
            verifyContext.getKickMessage().set(LanguageHandler.getInstance().getMessage("auth_verify_error"));
            Logger.LoggerProvider.getLogger().error("An exception was encountered while processing the whitelist.", e);
            return Signal.TERMINATED;
        }
    }
}
