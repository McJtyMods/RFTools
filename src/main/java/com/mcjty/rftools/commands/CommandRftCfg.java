package com.mcjty.rftools.commands;

public class CommandRftCfg extends DefaultCommand {
    public CommandRftCfg() {
        super();
        registerCommand(new CmdSetBuffBar());
    }

    @Override
    public String getCommandName() {
        return "rftcfg";
    }
}
