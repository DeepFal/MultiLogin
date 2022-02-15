package moe.caa.multilogin.api.auth;

import moe.caa.multilogin.api.auth.yggdrasil.response.HasJoinedResponse;

public interface AuthResult {

    String getKickMessage();

    HasJoinedResponse getResponse();

    boolean hasPassed();
}
