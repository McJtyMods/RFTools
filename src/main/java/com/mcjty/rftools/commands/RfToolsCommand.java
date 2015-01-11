package com.mcjty.rftools.commands;

import net.minecraft.command.ICommandSender;

public interface RfToolsCommand {
    public String getHelp();

    /**
     * 0 is allowed for everyone
     * 4 is most restrictive
     */
    public int getPermissionLevel();

    public String getCommand();

    public void execute(ICommandSender sender, String[] args);
}
