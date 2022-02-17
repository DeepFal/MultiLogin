package moe.caa.multilogin.api;

import moe.caa.multilogin.api.auth.AuthAPI;

public interface MultiLoginAPI {

    void onEnable() throws Throwable;

    void onDisabled();

    void reload() throws Throwable;

    AuthAPI getAuthCore();
}
