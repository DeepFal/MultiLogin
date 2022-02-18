package moe.caa.multilogin.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import lombok.Getter;
import moe.caa.multilogin.api.command.CommandAPI;
import moe.caa.multilogin.api.plugin.ISender;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.language.LanguageHandler;
import moe.caa.multilogin.logger.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 命令处理程序
 */
public class CommandHandler implements CommandAPI {

    @Getter
    private final MultiCore core;

    private final CommandDispatcher<ISender> dispatcher = new CommandDispatcher<>();

    public CommandHandler(MultiCore core) {
        this.core = core;
        CommandSyntaxException.BUILT_IN_EXCEPTIONS = new BuiltInExceptions();
    }

    @Override
    public void execute(ISender sender, String[] args) {
        Logger.LoggerProvider.getLogger().debug(String.format("Executing command: %s. (%s)", String.join(" ", args), sender.getName()));
        try {
            dispatcher.execute(String.join(" ", args), sender);
        } catch (CommandSyntaxException e) {
            sender.sendMessage(e.getRawMessage().getString());
            Logger.LoggerProvider.getLogger().debug("argument: " + String.join(" ", args), e);
        } catch (Exception e) {
            sender.sendMessage(LanguageHandler.getInstance().getMessage("command_error"));
            Logger.LoggerProvider.getLogger().error("An exception occurred while executing the command.", e);
            Logger.LoggerProvider.getLogger().error("sender: " + sender.getName());
            Logger.LoggerProvider.getLogger().error("argument: " + String.join(" ", args));
        }
    }

    @Override
    public List<String> tabComplete(ISender sender, String[] args) {
        if (!sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_TAB_COMPLETE)) {
            return Collections.emptyList();
        }
        CompletableFuture<Suggestions> suggestions = dispatcher.getCompletionSuggestions(dispatcher.parse(String.join(" ", args), sender));
        List<String> ret = new ArrayList<>();
        try {
            Suggestions suggestions1 = suggestions.get();
            for (Suggestion suggestion : suggestions1.getList()) {
                ret.add(suggestion.getText());
            }
        } catch (Exception e) {
            Logger.LoggerProvider.getLogger().error("An exception occurred while completing the command.", e);
            Logger.LoggerProvider.getLogger().error("sender: " + sender.getName());
            Logger.LoggerProvider.getLogger().error("arguments: " + String.join(" ", args));
        }
        return ret;
    }
}
