package com.mcjty.rftools.commands;

import com.mcjty.rftools.items.dimlets.KnownDimletConfiguration;
import net.minecraft.command.ICommandSender;

public class CmdDumpMRarity extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public String getCommand() {
        return "dumpmrarity";
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        KnownDimletConfiguration.dumpMaterialRarityDistribution();
    }
}
