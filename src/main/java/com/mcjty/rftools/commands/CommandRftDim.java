package com.mcjty.rftools.commands;

public class CommandRftDim extends DefaultCommand {

    public CommandRftDim() {
        super();
        registerCommand(new CmdListDimensions());
        registerCommand(new CmdDelDimension());
        registerCommand(new CmdTeleport());
        registerCommand(new CmdDumpRarity());
        registerCommand(new CmdDumpMRarity());
        registerCommand(new CmdListEffects());
        registerCommand(new CmdAddEffect());
        registerCommand(new CmdDelEffect());
        registerCommand(new CmdSetPower());
        registerCommand(new CmdInfo());
        registerCommand(new CmdReclaim());
        registerCommand(new CmdSafeDelete());
        registerCommand(new CmdRecover());
        registerCommand(new CmdSaveDims());
        registerCommand(new CmdSaveDim());
        registerCommand(new CmdLoadDim());
    }

    @Override
    public String getCommandName() {
        return "rftdim";
    }
}
