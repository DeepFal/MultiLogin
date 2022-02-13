package fun.ksnb.multilogin.velocity.impl;

import com.velocitypowered.api.proxy.ProxyServer;
import moe.caa.multilogin.api.plugin.BaseScheduler;
import moe.caa.multilogin.api.plugin.IPlayerManager;
import moe.caa.multilogin.api.plugin.IServer;

public class VelocityServer implements IServer {
    private final ProxyServer server;
    private final BaseScheduler scheduler;
    private final IPlayerManager playerManager;

    public VelocityServer(ProxyServer server) {
        this.server = server;
        this.scheduler = new VelocityScheduler();
        this.playerManager = new VelocityPlayerManager(server);
    }

    @Override
    public BaseScheduler getScheduler() {
        return scheduler;
    }

    @Override
    public IPlayerManager getPlayerManager() {
        return playerManager;
    }

    @Override
    public boolean isOnlineMode() {
        return server.getConfiguration().isOnlineMode();
    }

    @Override
    public boolean isWhitelist() {
        return false;
    }

    @Override
    public boolean isForwarded() {
        // TODO: 2022/2/13 ???
        return true;
    }

    @Override
    public String getName() {
        return server.getVersion().getName();
    }

    @Override
    public String getVersion() {
        return server.getVersion().getVersion();
    }

    @Override
    public void shutdown() {
        server.shutdown();
    }
}
