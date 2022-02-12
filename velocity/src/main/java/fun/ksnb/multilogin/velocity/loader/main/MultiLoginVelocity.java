package fun.ksnb.multilogin.velocity.loader.main;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import moe.caa.multilogin.loader.main.PluginLoader;
import moe.caa.multilogin.logger.bridges.Slf4JLoggerBridge;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;

public class MultiLoginVelocity {

    @Inject
    public MultiLoginVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) throws Throwable {
        final Slf4JLoggerBridge slf4JLoggerBridge = new Slf4JLoggerBridge(logger);
        moe.caa.multilogin.logger.Logger.LoggerProvider.setLogger(slf4JLoggerBridge);
        new PluginLoader(new File(dataDirectory.toFile(), "libraries"), new File(dataDirectory.toFile(), "temp")).load();
    }
}
