package moe.caa.multilogin.core.auth.verify;

import lombok.Data;
import moe.caa.multilogin.api.auth.yggdrasil.response.HasJoinedResponse;
import moe.caa.multilogin.core.config.YggdrasilService;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 二次验证上下文
 */
@Data
public class VerifyContext {
    private final VerifyCore verifyCore;
    private final HasJoinedResponse response;
    private final YggdrasilService service;
    private final boolean dataExists;
    private final AtomicReference<String> kickMessage = new AtomicReference<>();

    protected VerifyContext(VerifyCore verifyCore, HasJoinedResponse response, YggdrasilService service, boolean dataExists) {
        this.verifyCore = verifyCore;
        this.dataExists = dataExists;
        this.response = response;
        this.service = service;
    }
}
