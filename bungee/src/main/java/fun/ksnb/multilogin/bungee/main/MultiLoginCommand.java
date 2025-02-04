package fun.ksnb.multilogin.bungee.main;

import fun.ksnb.multilogin.bungee.impl.BungeeSender;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class MultiLoginCommand extends Command implements TabExecutor {

    public MultiLoginCommand(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        MultiLoginBungeePluginBootstrap.getInstance().getCore().getCommandHandler().execute(new BungeeSender(sender), getName(), args);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return MultiLoginBungeePluginBootstrap.getInstance().getCore().getCommandHandler().tabComplete(new BungeeSender(sender), getName(), args);
    }
}
