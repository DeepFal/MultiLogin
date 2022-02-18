package moe.caa.multilogin.core.command.arguments;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import moe.caa.multilogin.core.config.YggdrasilService;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.FormatContent;
import moe.caa.multilogin.language.LanguageHandler;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class YggdrasilServiceArgumentType implements ArgumentType<YggdrasilService> {
    private static final Collection<String> EXAMPLES = Arrays.asList(
            "official",
            "demoYggdrasilPath"
    );

    private static final DynamicCommandExceptionType dynamicCommandExceptionType = new DynamicCommandExceptionType(path -> new LiteralMessage(FormatContent.createContent(FormatContent.FormatEntry.builder().name("path").content(path).build())
            .format(LanguageHandler.getInstance().getMessage("command_exception_reader_invalid_yggdrasil"))));

    public static YggdrasilServiceArgumentType yggdrasil() {

        return new YggdrasilServiceArgumentType();
    }

    public static YggdrasilService getYggdrasil(final CommandContext<?> context, final String name) {
        return context.getArgument(name, YggdrasilService.class);
    }

    @Override
    public YggdrasilService parse(StringReader reader) throws CommandSyntaxException {
        int argBeginning = reader.getCursor();
        if (!reader.canRead()) {
            reader.skip();
        }
        while (reader.canRead() && reader.peek() != ' ') {
            reader.skip();
        }
        String path = reader.getString().substring(argBeginning, reader.getCursor());

        for (YggdrasilService service : MultiCore.getInstance().getConfig().getYggdrasilServices()) {
            if (service.getPath().equals(path)) return service;
        }
        throw dynamicCommandExceptionType.create(path);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        MultiCore.getInstance().getConfig().getYggdrasilServices().stream()
                .filter(service -> service.getPath().toLowerCase(Locale.ROOT).startsWith(builder.getRemainingLowerCase()))
                .forEach(ys -> builder.suggest(ys.getPath()));
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
