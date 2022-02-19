package fun.ksnb.multilogin.velocity.main;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import fun.ksnb.multilogin.velocity.impl.VelocityPlayer;
import fun.ksnb.multilogin.velocity.impl.VelocitySender;

import java.util.List;

public class MultiLoginVelocityCommandHandler implements SimpleCommand {
    private final MultiLoginVelocity plugin;

    public MultiLoginVelocityCommandHandler(MultiLoginVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        String[] ns = new String[args.length + 1];
        System.arraycopy(args, 0, ns, 1, args.length);
        ns[0] = invocation.alias();
        if (invocation.source() instanceof Player) {
            plugin.getMultiLoginAPI().getCommandHandler().execute(
                    new VelocityPlayer((Player) invocation.source()), ns
            );
        } else {
            plugin.getMultiLoginAPI().getCommandHandler().execute(
                    new VelocitySender(invocation.source()), ns
            );
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        String[] ns = new String[args.length + 1];
        System.arraycopy(args, 0, ns, 1, args.length);
        ns[0] = invocation.alias();
        if (invocation.source() instanceof Player) {
            return plugin.getMultiLoginAPI().getCommandHandler().tabComplete(
                    new VelocityPlayer((Player) invocation.source()), ns
            );
        } else {
            return plugin.getMultiLoginAPI().getCommandHandler().tabComplete(
                    new VelocitySender(invocation.source()), ns
            );
        }
    }
}
