package moe.caa.multilogin.api.command;

import moe.caa.multilogin.api.plugin.ISender;

import java.util.List;

public interface CommandAPI {
    void execute(ISender sender, String[] args);

    List<String> tabComplete(ISender sender, String[] args);
}
