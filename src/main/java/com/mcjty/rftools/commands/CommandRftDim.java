package com.mcjty.rftools.commands;

public class CommandRftDim extends DefaultCommand {

    public CommandRftDim() {
        super();
        registerCommand(new CmdListDimensions());
        registerCommand(new CmdDelDimension());
        registerCommand(new CmdTeleport());
        registerCommand(new CmdDumpRarity());
        registerCommand(new CmdDumpMRarity());
    }

    @Override
    public String getCommandName() {
        return "rftdim";
    }
}
