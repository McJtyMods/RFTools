package com.mcjty.rftools.commands;

public class CommandRftCfg extends DefaultCommand {
    public CommandRftCfg() {
        super();
        registerCommand(new CmdSetBuffBar());
        registerCommand(new CmdDimletCfg());
    }

    @Override
    public String getCommandName() {
        return "rftcfg";
    }
}
