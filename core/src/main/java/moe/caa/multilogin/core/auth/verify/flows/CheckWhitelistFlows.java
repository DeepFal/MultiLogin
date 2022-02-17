package moe.caa.multilogin.core.auth.verify.flows;

import moe.caa.multilogin.core.auth.verify.VerifyContext;
import moe.caa.multilogin.flows.workflows.BaseFlows;

/**
 * 白名单检查
 */
public class CheckWhitelistFlows extends BaseFlows<VerifyContext> {

    @Override
    public Signal run(VerifyContext verifyContext) {
//        try {
//            final SQLManager sqlManager = MultiCore.getInstance().getSqlManager();
//            if (sqlManager.getUserDataHandler()
//                    .hasWhitelist(verifyContext.getResponse().getId(), verifyContext.getService().getId()))
//                return Signal.PASSED;
//            if (sqlManager.getCacheWhitelistDataHandler().hasWhitelistAndRemove(
//                    verifyContext.getResponse().getName(),
//                    verifyContext.getResponse().getId(),
//                    verifyContext.getService().getId()
//            )) {
//                return Signal.PASSED;
//            }
//            verifyContext.getKickMessage().set(LanguageHandler.getInstance().getMessage("auth_verify_failed_no_whitelist"));
//            return Signal.TERMINATED;
//        } catch (SQLException e) {
//            verifyContext.getKickMessage().set(LanguageHandler.getInstance().getMessage("auth_verify_error"));
//            Logger.LoggerProvider.getLogger().error("An exception was encountered while processing the whitelist.", e);
//            return Signal.TERMINATED;
//        }
        return Signal.PASSED;
    }
}
