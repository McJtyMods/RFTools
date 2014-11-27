package com.mcjty.rftools.commands;

import net.minecraft.command.ICommandSender;

public interface RfToolsCommand {
    public String getHelp();

    public String getCommand();

    public void execute(ICommandSender sender, String[] args);
}
