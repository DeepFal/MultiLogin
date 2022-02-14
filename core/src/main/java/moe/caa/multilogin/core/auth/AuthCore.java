package moe.caa.multilogin.core.auth;

import lombok.Getter;
import moe.caa.multilogin.core.auth.yggdrasil.HasJoinedValidateCore;
import moe.caa.multilogin.core.main.MultiCore;

public class AuthCore {

    @Getter
    private final MultiCore core;

    @Getter
    private final HasJoinedValidateCore hasJoinedValidateCore;

    public AuthCore(MultiCore core) {
        this.core = core;
        this.hasJoinedValidateCore = new HasJoinedValidateCore(this);
    }

    public void init() {
        hasJoinedValidateCore.init();
    }
}
