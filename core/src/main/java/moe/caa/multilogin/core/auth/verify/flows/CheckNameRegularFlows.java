package moe.caa.multilogin.core.auth.verify.flows;

import moe.caa.multilogin.core.auth.verify.VerifyContext;
import moe.caa.multilogin.core.util.FormatContent;
import moe.caa.multilogin.core.util.ValueUtil;
import moe.caa.multilogin.flows.workflows.BaseFlows;
import moe.caa.multilogin.language.LanguageHandler;

import java.util.regex.Pattern;

/**
 * 名字正则检查
 */
public class CheckNameRegularFlows extends BaseFlows<VerifyContext> {

    @Override
    public Signal run(VerifyContext verifyContext) {
        String reg = verifyContext.getService().getNameAllowedRegular();
        if (ValueUtil.isEmpty(reg)) return Signal.PASSED;
        if (Pattern.matches(reg, verifyContext.getResponse().getName())) return Signal.PASSED;
        verifyContext.getKickMessage().set(FormatContent.createContent(
                FormatContent.FormatEntry.builder().name("current_name").content(verifyContext.getResponse().getName()).build(),
                FormatContent.FormatEntry.builder().name("regular").content(reg).build()
        ).format(LanguageHandler.getInstance().getMessage("auth_verify_failed_username_mismatch")));
        return Signal.TERMINATED;
    }
}
