package com.mcjty.rftools.commands;

public class CommandRftDb extends DefaultCommand {
    public CommandRftDb() {
        super();
        registerCommand(new CmdDebugToggle());
    }

    @Override
    public String getCommandName() {
        return "rftdb";
    }
}
