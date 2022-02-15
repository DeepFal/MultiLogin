package moe.caa.multilogin.api;

public interface MultiLoginAPI {

    void onEnabled() throws Throwable;

    void onDisabled();

    void reload() throws Throwable;
}
