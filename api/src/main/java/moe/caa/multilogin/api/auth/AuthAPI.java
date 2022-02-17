package moe.caa.multilogin.api.auth;

public interface AuthAPI {
    AuthResult auth(String username, String serverId, String ip);
}
