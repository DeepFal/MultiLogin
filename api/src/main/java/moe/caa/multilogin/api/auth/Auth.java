package moe.caa.multilogin.api.auth;

public interface Auth {
    AuthResult auth(String username, String serverId, String ip);
}
