package moe.caa.multilogin.core.command.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import lombok.SneakyThrows;
import moe.caa.multilogin.api.plugin.ISender;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.Permissions;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.CheckUpdater;
import moe.caa.multilogin.core.util.FormatContent;
import moe.caa.multilogin.language.LanguageHandler;
import moe.caa.multilogin.logger.Logger;

public class MultiLoginCommand {

    public void register(CommandDispatcher<ISender> dispatcher) {
        dispatcher.register(
                CommandHandler.literal("multilogin")
                        .then(CommandHandler.literal("reload")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_RELOAD))
                                .executes(this::executeReload)
                        )
                        .then(CommandHandler.literal("update")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_UPDATE))
                                .executes(this::executeUpdate)
                        )
        );
    }

    private int executeUpdate(CommandContext<ISender> context) {
        context.getSource().sendMessage(LanguageHandler.getInstance().getMessage("command_message_update_start"));
        MultiCore.getInstance().getPlugin().getRunServer().getScheduler().runTaskAsync(() -> {
            try {
                CheckUpdater checkUpdater = MultiCore.getInstance().getCheckUpdater();
                checkUpdater.doCheck();
                if (checkUpdater.shouldUpdate()) {
                    context.getSource().sendMessage(
                            FormatContent.createContent(
                                    FormatContent.FormatEntry.builder().name("latest").content(checkUpdater.getLatestVersion()).build(),
                                    FormatContent.FormatEntry.builder().name("current").content(checkUpdater.getCurrentVersion()).build()
                            ).format(LanguageHandler.getInstance().getMessage("command_message_update_need"))
                    );
                } else {
                    context.getSource().sendMessage(LanguageHandler.getInstance().getMessage("command_message_update_unwanted"));
                }
            } catch (Exception e) {
                context.getSource().sendMessage(LanguageHandler.getInstance().getMessage("command_message_update_failed"));
                Logger.LoggerProvider.getLogger().error("Check update failure.", e);
            }
        });
        return 0;
    }

    @SneakyThrows
    private int executeReload(CommandContext<ISender> context) {
        MultiCore.getInstance().reload();
        context.getSource().sendMessage(LanguageHandler.getInstance().getMessage("command_message_reloaded"));
        return 0;
    }
}
