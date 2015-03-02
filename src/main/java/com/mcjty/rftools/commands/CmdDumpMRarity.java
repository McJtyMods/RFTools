package com.mcjty.rftools.commands;

import com.mcjty.rftools.items.dimlets.DimletRandomizer;
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
        DimletRandomizer.dumpMaterialRarityDistribution(sender.getEntityWorld());
    }

    @Override
    public int getPermissionLevel() {
        return 0;
    }

    @Override
    public boolean isClientSide() {
        return false;
    }
}
