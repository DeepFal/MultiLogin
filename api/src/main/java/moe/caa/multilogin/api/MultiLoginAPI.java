package moe.caa.multilogin.api;

import moe.caa.multilogin.api.auth.Auth;

public interface MultiLoginAPI {

    void onEnable() throws Throwable;

    void onDisabled();

    void reload() throws Throwable;

    Auth getAuthCore();
}
