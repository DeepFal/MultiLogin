package moe.caa.multilogin.core.command.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import lombok.SneakyThrows;
import moe.caa.multilogin.api.plugin.IPlayer;
import moe.caa.multilogin.api.plugin.ISender;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.Permissions;
import moe.caa.multilogin.core.command.arguments.StringArgumentType;
import moe.caa.multilogin.core.command.arguments.UUIDArgumentType;
import moe.caa.multilogin.core.command.arguments.YggdrasilServiceArgumentType;
import moe.caa.multilogin.core.config.YggdrasilService;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.FormatContent;
import moe.caa.multilogin.language.LanguageHandler;

import java.sql.SQLException;
import java.util.UUID;

public class WhitelistCommand {

    public void register(CommandDispatcher<ISender> dispatcher) {
        dispatcher.register(
                CommandHandler.literal("whitelist")
                        .then(CommandHandler.literal("add")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_WHITELIST_ADD))

                                // 只有名字
                                .then(CommandHandler.literal("name")
                                        .then(CommandHandler.argument("name", StringArgumentType.string())
                                                .executes(this::executeAddByName)
                                        )
                                )
                                .then(CommandHandler.literal("n")
                                        .then(CommandHandler.argument("name", StringArgumentType.string())
                                                .executes(this::executeAddByName)
                                        )
                                )

                                // 只有UUID
                                .then(CommandHandler.literal("uuid")
                                        .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                .executes(this::executeAddByUuid)
                                        )
                                )
                                .then(CommandHandler.literal("u")
                                        .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                .executes(this::executeAddByUuid)
                                        )
                                )

                                // 名字和UUID
                                .then(CommandHandler.literal("nameAndUuid")
                                        .then(CommandHandler.argument("name", StringArgumentType.string())
                                                .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                        .executes(this::executeAddByNameAndUuid)
                                                )
                                        )
                                )
                                .then(CommandHandler.literal("nu")
                                        .then(CommandHandler.argument("name", StringArgumentType.string())
                                                .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                        .executes(this::executeAddByNameAndUuid)
                                                )
                                        )
                                )
                                .then(CommandHandler.literal("uuidAndName")
                                        .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                .then(CommandHandler.argument("name", StringArgumentType.string())
                                                        .executes(this::executeAddByNameAndUuid)
                                                )
                                        )
                                )
                                .then(CommandHandler.literal("un")
                                        .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                .then(CommandHandler.argument("name", StringArgumentType.string())
                                                        .executes(this::executeAddByNameAndUuid)
                                                )
                                        )
                                )

                                // 名字和Yggdrasil
                                .then(CommandHandler.literal("nameAndYggdrasil")
                                        .then(CommandHandler.argument("name", StringArgumentType.string())
                                                .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                        .executes(this::executeAddByNameAndYggdrasil)
                                                )
                                        )
                                )
                                .then(CommandHandler.literal("ny")
                                        .then(CommandHandler.argument("name", StringArgumentType.string())
                                                .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                        .executes(this::executeAddByNameAndYggdrasil)
                                                )
                                        )
                                )
                                .then(CommandHandler.literal("yggdrasilAndName")
                                        .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                .then(CommandHandler.argument("name", StringArgumentType.string())
                                                        .executes(this::executeAddByNameAndYggdrasil)
                                                )
                                        )
                                )
                                .then(CommandHandler.literal("yn")
                                        .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                .then(CommandHandler.argument("name", StringArgumentType.string())
                                                        .executes(this::executeAddByNameAndYggdrasil)
                                                )
                                        )
                                )

                                // UUID和Yggdrasil
                                .then(CommandHandler.literal("uuidAndYggdrasil")
                                        .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                        .executes(this::executeAddByUuidAndYggdrasil)
                                                )
                                        )
                                )
                                .then(CommandHandler.literal("uy")
                                        .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                        .executes(this::executeAddByUuidAndYggdrasil)
                                                )
                                        )
                                )
                                .then(CommandHandler.literal("yggdrasilAndUuid")
                                        .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                        .executes(this::executeAddByUuidAndYggdrasil)
                                                )
                                        )
                                )
                                .then(CommandHandler.literal("yu")
                                        .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                        .executes(this::executeAddByUuidAndYggdrasil)
                                                )
                                        )
                                )

                                // 全都有
                                .then(CommandHandler.literal("uuidNameAndYggdrasil")
                                        .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                .then(CommandHandler.argument("name", StringArgumentType.string())
                                                        .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                                .executes(this::executeAddByUuidNameAndYggdrasil)
                                                        )
                                                )

                                        )
                                )
                                .then(CommandHandler.literal("uny")
                                        .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                .then(CommandHandler.argument("name", StringArgumentType.string())
                                                        .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                                .executes(this::executeAddByUuidNameAndYggdrasil)
                                                        )
                                                )

                                        )
                                )
                                .then(CommandHandler.literal("nameUuidAndYggdrasil")
                                        .then(CommandHandler.argument("name", StringArgumentType.string())
                                                .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                        .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                                .executes(this::executeAddByUuidNameAndYggdrasil)
                                                        )
                                                )

                                        )
                                )
                                .then(CommandHandler.literal("nuy")
                                        .then(CommandHandler.argument("name", StringArgumentType.string())
                                                .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                        .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                                .executes(this::executeAddByUuidNameAndYggdrasil)
                                                        )
                                                )

                                        )
                                )

                                .then(CommandHandler.literal("nameYggdrasilAndUuid")
                                        .then(CommandHandler.argument("name", StringArgumentType.string())
                                                .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                        .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                                .executes(this::executeAddByUuidNameAndYggdrasil)
                                                        )
                                                )

                                        )
                                )
                                .then(CommandHandler.literal("nyu")
                                        .then(CommandHandler.argument("name", StringArgumentType.string())
                                                .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                        .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                                .executes(this::executeAddByUuidNameAndYggdrasil)
                                                        )
                                                )

                                        )
                                )

                                .then(CommandHandler.literal("uuidYggdrasilAndName")
                                        .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                        .then(CommandHandler.argument("name", StringArgumentType.string())
                                                                .executes(this::executeAddByUuidNameAndYggdrasil)
                                                        )
                                                )

                                        )
                                )
                                .then(CommandHandler.literal("uyn")
                                        .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                        .then(CommandHandler.argument("name", StringArgumentType.string())
                                                                .executes(this::executeAddByUuidNameAndYggdrasil)
                                                        )
                                                )

                                        )
                                )

                                .then(CommandHandler.literal("yggdrasilNameAndUuid")
                                        .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                .then(CommandHandler.argument("name", StringArgumentType.string())
                                                        .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                                .executes(this::executeAddByUuidNameAndYggdrasil)
                                                        )
                                                )

                                        )
                                )
                                .then(CommandHandler.literal("ynu")
                                        .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                .then(CommandHandler.argument("name", StringArgumentType.string())
                                                        .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                                .executes(this::executeAddByUuidNameAndYggdrasil)
                                                        )
                                                )

                                        )
                                )

                                .then(CommandHandler.literal("yggdrasilUuidAndName")
                                        .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                        .then(CommandHandler.argument("name", StringArgumentType.string())
                                                                .executes(this::executeAddByUuidNameAndYggdrasil)
                                                        )
                                                )

                                        )
                                )
                                .then(CommandHandler.literal("yun")
                                        .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                        .then(CommandHandler.argument("name", StringArgumentType.string())
                                                                .executes(this::executeAddByUuidNameAndYggdrasil)
                                                        )
                                                )

                                        )
                                )
                        )
                        .then(CommandHandler.literal("remove")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_WHITELIST_REMOVE))

                                // 只有名字
                                .then(CommandHandler.literal("name")
                                        .then(CommandHandler.argument("name", StringArgumentType.string())
                                                .executes(this::executeRemoveByName)
                                        )
                                )
                                .then(CommandHandler.literal("n")
                                        .then(CommandHandler.argument("name", StringArgumentType.string())
                                                .executes(this::executeRemoveByName)
                                        )
                                )

                                // 只有UUID
                                .then(CommandHandler.literal("uuid")
                                        .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                .executes(this::executeRemoveByUuid)
                                        )
                                )
                                .then(CommandHandler.literal("u")
                                        .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                .executes(this::executeRemoveByUuid)
                                        )
                                )

                                // 只有Yggdrasil
                                .then(CommandHandler.literal("yggdrasil")
                                        .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                .executes(this::executeRemoveByYggdrasil)
                                        )
                                )
                                .then(CommandHandler.literal("y")
                                        .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                .executes(this::executeRemoveByYggdrasil)
                                        )
                                )

                                // 名字和UUID
                                .then(CommandHandler.literal("nameAndUuid")
                                        .then(CommandHandler.argument("name", StringArgumentType.string())
                                                .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                        .executes(this::executeRemoveByNameAndUuid)
                                                )
                                        )
                                )
                                .then(CommandHandler.literal("nu")
                                        .then(CommandHandler.argument("name", StringArgumentType.string())
                                                .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                        .executes(this::executeRemoveByNameAndUuid)
                                                )
                                        )
                                )
                                .then(CommandHandler.literal("uuidAndName")
                                        .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                .then(CommandHandler.argument("name", StringArgumentType.string())
                                                        .executes(this::executeRemoveByNameAndUuid)
                                                )
                                        )
                                )
                                .then(CommandHandler.literal("un")
                                        .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                .then(CommandHandler.argument("name", StringArgumentType.string())
                                                        .executes(this::executeRemoveByNameAndUuid)
                                                )
                                        )
                                )

                                // 名字和Yggdrasil
                                .then(CommandHandler.literal("nameAndYggdrasil")
                                        .then(CommandHandler.argument("name", StringArgumentType.string())
                                                .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                        .executes(this::executeRemoveByNameAndYggdrasil)
                                                )
                                        )
                                )
                                .then(CommandHandler.literal("ny")
                                        .then(CommandHandler.argument("name", StringArgumentType.string())
                                                .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                        .executes(this::executeRemoveByNameAndYggdrasil)
                                                )
                                        )
                                )
                                .then(CommandHandler.literal("yggdrasilAndName")
                                        .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                .then(CommandHandler.argument("name", StringArgumentType.string())
                                                        .executes(this::executeRemoveByNameAndYggdrasil)
                                                )
                                        )
                                )
                                .then(CommandHandler.literal("yn")
                                        .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                .then(CommandHandler.argument("name", StringArgumentType.string())
                                                        .executes(this::executeRemoveByNameAndYggdrasil)
                                                )
                                        )
                                )

                                // UUID和Yggdrasil
                                .then(CommandHandler.literal("uuidAndYggdrasil")
                                        .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                        .executes(this::executeRemoveByUuidAndYggdrasil)
                                                )
                                        )
                                )
                                .then(CommandHandler.literal("uy")
                                        .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                        .executes(this::executeRemoveByUuidAndYggdrasil)
                                                )
                                        )
                                )
                                .then(CommandHandler.literal("yggdrasilAndUuid")
                                        .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                        .executes(this::executeRemoveByUuidAndYggdrasil)
                                                )
                                        )
                                )
                                .then(CommandHandler.literal("yu")
                                        .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                        .executes(this::executeRemoveByUuidAndYggdrasil)
                                                )
                                        )
                                )

                                // 全都有
                                .then(CommandHandler.literal("uuidNameAndYggdrasil")
                                        .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                .then(CommandHandler.argument("name", StringArgumentType.string())
                                                        .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                                .executes(this::executeRemoveByUuidNameAndYggdrasil)
                                                        )
                                                )

                                        )
                                )
                                .then(CommandHandler.literal("uny")
                                        .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                .then(CommandHandler.argument("name", StringArgumentType.string())
                                                        .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                                .executes(this::executeRemoveByUuidNameAndYggdrasil)
                                                        )
                                                )

                                        )
                                )
                                .then(CommandHandler.literal("nameUuidAndYggdrasil")
                                        .then(CommandHandler.argument("name", StringArgumentType.string())
                                                .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                        .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                                .executes(this::executeRemoveByUuidNameAndYggdrasil)
                                                        )
                                                )

                                        )
                                )
                                .then(CommandHandler.literal("nuy")
                                        .then(CommandHandler.argument("name", StringArgumentType.string())
                                                .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                        .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                                .executes(this::executeRemoveByUuidNameAndYggdrasil)
                                                        )
                                                )

                                        )
                                )

                                .then(CommandHandler.literal("nameYggdrasilAndUuid")
                                        .then(CommandHandler.argument("name", StringArgumentType.string())
                                                .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                        .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                                .executes(this::executeRemoveByUuidNameAndYggdrasil)
                                                        )
                                                )

                                        )
                                )
                                .then(CommandHandler.literal("nyu")
                                        .then(CommandHandler.argument("name", StringArgumentType.string())
                                                .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                        .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                                .executes(this::executeRemoveByUuidNameAndYggdrasil)
                                                        )
                                                )

                                        )
                                )

                                .then(CommandHandler.literal("uuidYggdrasilAndName")
                                        .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                        .then(CommandHandler.argument("name", StringArgumentType.string())
                                                                .executes(this::executeRemoveByUuidNameAndYggdrasil)
                                                        )
                                                )

                                        )
                                )
                                .then(CommandHandler.literal("uyn")
                                        .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                        .then(CommandHandler.argument("name", StringArgumentType.string())
                                                                .executes(this::executeRemoveByUuidNameAndYggdrasil)
                                                        )
                                                )

                                        )
                                )

                                .then(CommandHandler.literal("yggdrasilNameAndUuid")
                                        .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                .then(CommandHandler.argument("name", StringArgumentType.string())
                                                        .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                                .executes(this::executeRemoveByUuidNameAndYggdrasil)
                                                        )
                                                )

                                        )
                                )
                                .then(CommandHandler.literal("ynu")
                                        .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                .then(CommandHandler.argument("name", StringArgumentType.string())
                                                        .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                                .executes(this::executeRemoveByUuidNameAndYggdrasil)
                                                        )
                                                )

                                        )
                                )

                                .then(CommandHandler.literal("yggdrasilUuidAndName")
                                        .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                        .then(CommandHandler.argument("name", StringArgumentType.string())
                                                                .executes(this::executeRemoveByUuidNameAndYggdrasil)
                                                        )
                                                )

                                        )
                                )
                                .then(CommandHandler.literal("yun")
                                        .then(CommandHandler.argument("yggdrasil", YggdrasilServiceArgumentType.yggdrasil())
                                                .then(CommandHandler.argument("uuid", UUIDArgumentType.uuid())
                                                        .then(CommandHandler.argument("name", StringArgumentType.string())
                                                                .executes(this::executeRemoveByUuidNameAndYggdrasil)
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(CommandHandler.literal("clearCache")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_WHITELIST_CLEAR_CACHE))
                                .executes(this::executeClearCache)
                        )
        );
    }

    @SneakyThrows
    private int executeRemoveByYggdrasil(CommandContext<ISender> context) {
        final YggdrasilService yggdrasil = YggdrasilServiceArgumentType.getYggdrasil(context, "yggdrasil");
        int count = 0;
        count += MultiCore.getInstance().getSqlManager().getCacheWhitelistDataHandler().removeByYggdrasilId(yggdrasil.getId());
        count += MultiCore.getInstance().getSqlManager().getUserDataHandler().removeWhitelist(yggdrasil.getId());

        handleRemove();

        if (count == 0) {
            context.getSource().sendMessage(FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("yggdrasil").content(yggdrasil.getPath()).build()
            ).format(LanguageHandler.getInstance().getMessage("command_message_whitelist_remove_by_yggdrasil_repeat")));
        } else {
            context.getSource().sendMessage(FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("yggdrasil").content(yggdrasil.getPath()).build(),
                    FormatContent.FormatEntry.builder().name("count").content(count).build()
            ).format(LanguageHandler.getInstance().getMessage("command_message_whitelist_remove_by_yggdrasil")));
        }
        return 0;
    }

    @SneakyThrows
    private int executeRemoveByUuidNameAndYggdrasil(CommandContext<ISender> context) {
        final UUID uuid = UUIDArgumentType.getUuid(context, "uuid");
        final String name = StringArgumentType.getString(context, "name");
        final YggdrasilService yggdrasil = YggdrasilServiceArgumentType.getYggdrasil(context, "yggdrasil");
        int count = 0;
        count += MultiCore.getInstance().getSqlManager().getCacheWhitelistDataHandler().removeByOnlineUuidAndYggdrasilId(name, uuid, yggdrasil.getId());
        count += MultiCore.getInstance().getSqlManager().getUserDataHandler().removeWhitelist(name, uuid, yggdrasil.getId());

        handleRemove();

        if (count == 0) {
            context.getSource().sendMessage(FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("name_or_uuid").content(uuid).build()
            ).format(LanguageHandler.getInstance().getMessage("command_message_whitelist_remove_repeat")));
        } else {
            context.getSource().sendMessage(FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("name_or_uuid").content(uuid).build(),
                    FormatContent.FormatEntry.builder().name("count").content(count).build()
            ).format(LanguageHandler.getInstance().getMessage("command_message_whitelist_removed")));
        }
        return 0;
    }

    @SneakyThrows
    private int executeRemoveByUuidAndYggdrasil(CommandContext<ISender> context) {
        final UUID uuid = UUIDArgumentType.getUuid(context, "uuid");
        final YggdrasilService yggdrasil = YggdrasilServiceArgumentType.getYggdrasil(context, "yggdrasil");
        int count = 0;
        count += MultiCore.getInstance().getSqlManager().getCacheWhitelistDataHandler().removeByOnlineUuidAndYggdrasilId(uuid, yggdrasil.getId());
        count += MultiCore.getInstance().getSqlManager().getUserDataHandler().removeWhitelist(yggdrasil.getId(), uuid);

        handleRemove();

        if (count == 0) {
            context.getSource().sendMessage(FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("name_or_uuid").content(uuid).build()
            ).format(LanguageHandler.getInstance().getMessage("command_message_whitelist_remove_repeat")));
        } else {
            context.getSource().sendMessage(FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("name_or_uuid").content(uuid).build(),
                    FormatContent.FormatEntry.builder().name("count").content(count).build()
            ).format(LanguageHandler.getInstance().getMessage("command_message_whitelist_removed")));
        }
        return 0;
    }

    @SneakyThrows
    private int executeRemoveByNameAndYggdrasil(CommandContext<ISender> context) {
        final String name = StringArgumentType.getString(context, "name");
        final YggdrasilService yggdrasil = YggdrasilServiceArgumentType.getYggdrasil(context, "yggdrasil");
        int count = 0;
        count += MultiCore.getInstance().getSqlManager().getCacheWhitelistDataHandler().removeByCurrentUsernameAndYggdrasilId(name, yggdrasil.getId());
        count += MultiCore.getInstance().getSqlManager().getUserDataHandler().removeWhitelist(name, yggdrasil.getId());

        handleRemove();

        if (count == 0) {
            context.getSource().sendMessage(FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("name_or_uuid").content(name).build()
            ).format(LanguageHandler.getInstance().getMessage("command_message_whitelist_remove_repeat")));
        } else {
            context.getSource().sendMessage(FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("name_or_uuid").content(name).build(),
                    FormatContent.FormatEntry.builder().name("count").content(count).build()
            ).format(LanguageHandler.getInstance().getMessage("command_message_whitelist_removed")));
        }
        return 0;
    }

    @SneakyThrows
    private int executeRemoveByNameAndUuid(CommandContext<ISender> context) {
        final UUID uuid = UUIDArgumentType.getUuid(context, "uuid");
        final String name = StringArgumentType.getString(context, "name");
        int count = 0;
        count += MultiCore.getInstance().getSqlManager().getCacheWhitelistDataHandler().removeByCurrentUsernameAndOnlineUuid(name, uuid);
        count += MultiCore.getInstance().getSqlManager().getUserDataHandler().removeWhitelist(name, uuid);

        handleRemove();

        if (count == 0) {
            context.getSource().sendMessage(FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("name_or_uuid").content(name).build()
            ).format(LanguageHandler.getInstance().getMessage("command_message_whitelist_remove_repeat")));
        } else {
            context.getSource().sendMessage(FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("name_or_uuid").content(name).build(),
                    FormatContent.FormatEntry.builder().name("count").content(count).build()
            ).format(LanguageHandler.getInstance().getMessage("command_message_whitelist_removed")));
        }
        return 0;
    }

    @SneakyThrows
    private int executeRemoveByUuid(CommandContext<ISender> context) {
        final UUID uuid = UUIDArgumentType.getUuid(context, "uuid");
        int count = 0;
        count += MultiCore.getInstance().getSqlManager().getCacheWhitelistDataHandler().removeByOnlineUuid(uuid);
        count += MultiCore.getInstance().getSqlManager().getUserDataHandler().removeWhitelist(uuid);

        handleRemove();

        if (count == 0) {
            context.getSource().sendMessage(FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("name_or_uuid").content(uuid).build()
            ).format(LanguageHandler.getInstance().getMessage("command_message_whitelist_remove_repeat")));
        } else {
            context.getSource().sendMessage(FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("name_or_uuid").content(uuid).build(),
                    FormatContent.FormatEntry.builder().name("count").content(count).build()
            ).format(LanguageHandler.getInstance().getMessage("command_message_whitelist_removed")));
        }
        return 0;
    }

    @SneakyThrows
    private int executeRemoveByName(CommandContext<ISender> context) {
        final String name = StringArgumentType.getString(context, "name");
        int count = 0;
        count += MultiCore.getInstance().getSqlManager().getCacheWhitelistDataHandler().removeByCurrentUsername(name);
        count += MultiCore.getInstance().getSqlManager().getUserDataHandler().removeWhitelist(name);

        handleRemove();

        if (count == 0) {
            context.getSource().sendMessage(FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("name_or_uuid").content(name).build()
            ).format(LanguageHandler.getInstance().getMessage("command_message_whitelist_remove_repeat")));
        } else {
            context.getSource().sendMessage(FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("name_or_uuid").content(name).build(),
                    FormatContent.FormatEntry.builder().name("count").content(count).build()
            ).format(LanguageHandler.getInstance().getMessage("command_message_whitelist_removed")));
        }
        return 0;
    }

    @SneakyThrows
    private int executeAddByUuidNameAndYggdrasil(CommandContext<ISender> context) {
        final UUID uuid = UUIDArgumentType.getUuid(context, "uuid");
        final String name = StringArgumentType.getString(context, "name");
        final YggdrasilService yggdrasil = YggdrasilServiceArgumentType.getYggdrasil(context, "yggdrasil");
        boolean added;
        // 原来他有没有白名单
        if (!MultiCore.getInstance().getSqlManager().getUserDataHandler().hasWhitelist(name, uuid, yggdrasil.getId())) {
            // 这个白名单加了没有
            added = MultiCore.getInstance().getSqlManager().getCacheWhitelistDataHandler().insertNewByUsernameOnlineUuidAndYggdrasilId(name, uuid, yggdrasil.getId());
        } else {
            added = false;
        }
        if (added) {
            context.getSource().sendMessage(FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("name_or_uuid").content(name).build()
            ).format(LanguageHandler.getInstance().getMessage("command_message_whitelist_added")));
        } else {
            context.getSource().sendMessage(FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("name_or_uuid").content(name).build()
            ).format(LanguageHandler.getInstance().getMessage("command_message_whitelist_add_repeat")));
        }
        return 0;
    }

    @SneakyThrows
    private int executeAddByUuidAndYggdrasil(CommandContext<ISender> context) {
        final UUID uuid = UUIDArgumentType.getUuid(context, "uuid");
        final YggdrasilService yggdrasil = YggdrasilServiceArgumentType.getYggdrasil(context, "yggdrasil");
        boolean added;
        // 原来他有没有白名单
        if (!MultiCore.getInstance().getSqlManager().getUserDataHandler().hasWhitelist(uuid, yggdrasil.getId())) {
            // 这个白名单加了没有
            added = MultiCore.getInstance().getSqlManager().getCacheWhitelistDataHandler().insertNewByOnlineUuidAndYggdrasilId(uuid, yggdrasil.getId());
        } else {
            added = false;
        }
        if (added) {
            context.getSource().sendMessage(FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("name_or_uuid").content(uuid).build()
            ).format(LanguageHandler.getInstance().getMessage("command_message_whitelist_added")));
        } else {
            context.getSource().sendMessage(FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("name_or_uuid").content(uuid).build()
            ).format(LanguageHandler.getInstance().getMessage("command_message_whitelist_add_repeat")));
        }
        return 0;
    }

    @SneakyThrows
    private int executeAddByNameAndYggdrasil(CommandContext<ISender> context) {
        final String name = StringArgumentType.getString(context, "name");
        final YggdrasilService yggdrasil = YggdrasilServiceArgumentType.getYggdrasil(context, "yggdrasil");
        boolean added;
        // 原来他有没有白名单
        if (!MultiCore.getInstance().getSqlManager().getUserDataHandler().hasWhitelist(name, yggdrasil.getId())) {
            // 这个白名单加了没有
            added = MultiCore.getInstance().getSqlManager().getCacheWhitelistDataHandler().insertNewByUsernameAndYggdrasilId(name, yggdrasil.getId());
        } else {
            added = false;
        }
        if (added) {
            context.getSource().sendMessage(FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("name_or_uuid").content(name).build()
            ).format(LanguageHandler.getInstance().getMessage("command_message_whitelist_added")));
        } else {
            context.getSource().sendMessage(FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("name_or_uuid").content(name).build()
            ).format(LanguageHandler.getInstance().getMessage("command_message_whitelist_add_repeat")));
        }
        return 0;
    }

    @SneakyThrows
    private int executeAddByNameAndUuid(CommandContext<ISender> context) {
        final String name = StringArgumentType.getString(context, "name");
        final UUID uuid = UUIDArgumentType.getUuid(context, "uuid");
        boolean added;
        // 原来他有没有白名单
        if (!MultiCore.getInstance().getSqlManager().getUserDataHandler().hasWhitelist(name, uuid)) {
            // 这个白名单加了没有
            added = MultiCore.getInstance().getSqlManager().getCacheWhitelistDataHandler().insertNewByUsernameAndOnlineUuid(name, uuid);
        } else {
            added = false;
        }
        if (added) {
            context.getSource().sendMessage(FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("name_or_uuid").content(name).build()
            ).format(LanguageHandler.getInstance().getMessage("command_message_whitelist_added")));
        } else {
            context.getSource().sendMessage(FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("name_or_uuid").content(name).build()
            ).format(LanguageHandler.getInstance().getMessage("command_message_whitelist_add_repeat")));
        }
        return 0;
    }

    @SneakyThrows
    private int executeAddByUuid(CommandContext<ISender> context) {
        final UUID uuid = UUIDArgumentType.getUuid(context, "uuid");
        boolean added;
        // 原来他有没有白名单
        if (!MultiCore.getInstance().getSqlManager().getUserDataHandler().hasWhitelist(uuid)) {
            // 这个白名单加了没有
            added = MultiCore.getInstance().getSqlManager().getCacheWhitelistDataHandler().insertNewByOnlineUuid(uuid);
        } else {
            added = false;
        }
        if (added) {
            context.getSource().sendMessage(FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("name_or_uuid").content(uuid).build()
            ).format(LanguageHandler.getInstance().getMessage("command_message_whitelist_added")));
        } else {
            context.getSource().sendMessage(FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("name_or_uuid").content(uuid).build()
            ).format(LanguageHandler.getInstance().getMessage("command_message_whitelist_add_repeat")));
        }
        return 0;
    }

    @SneakyThrows
    private int executeAddByName(CommandContext<ISender> context) {
        final String name = StringArgumentType.getString(context, "name");
        boolean added;
        // 原来他有没有白名单
        if (!MultiCore.getInstance().getSqlManager().getUserDataHandler().hasWhitelist(name)) {
            // 这个白名单加了没有
            added = MultiCore.getInstance().getSqlManager().getCacheWhitelistDataHandler().insertNewByUsername(name);
        } else {
            added = false;
        }
        if (added) {
            context.getSource().sendMessage(FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("name_or_uuid").content(name).build()
            ).format(LanguageHandler.getInstance().getMessage("command_message_whitelist_added")));
        } else {
            context.getSource().sendMessage(FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("name_or_uuid").content(name).build()
            ).format(LanguageHandler.getInstance().getMessage("command_message_whitelist_add_repeat")));
        }
        return 0;
    }

    @SneakyThrows
    private int executeClearCache(CommandContext<ISender> context) {
        int i = MultiCore.getInstance().getSqlManager().getCacheWhitelistDataHandler().removeAllCacheWhitelist();
        if (i == 0) {
            context.getSource().sendMessage(LanguageHandler.getInstance().getMessage("command_message_whitelist_cache_clear_empty"));
        } else {
            context.getSource().sendMessage(FormatContent.createContent(
                    FormatContent.FormatEntry.builder().name("count").content(i).build()
            ).format(LanguageHandler.getInstance().getMessage("command_message_whitelist_cache_cleared")));
        }
        return 0;
    }

    private void handleRemove() throws SQLException {
        for (IPlayer player : MultiCore.getInstance().getPlugin().getRunServer().getPlayerManager().getOnlinePlayers()) {
            if (MultiCore.getInstance().getSqlManager().getUserDataHandler().hasWhitelistInGame(player.getUniqueId()))
                continue;
            player.kickPlayer(LanguageHandler.getInstance().getMessage("in_game_whitelist_removed"));
        }
    }
}
