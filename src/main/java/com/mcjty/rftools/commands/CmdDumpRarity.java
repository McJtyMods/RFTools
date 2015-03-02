package com.mcjty.rftools.commands;

import com.mcjty.rftools.items.dimlets.DimletRandomizer;
import net.minecraft.command.ICommandSender;

public class CmdDumpRarity extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "[<bonus>]";
    }

    @Override
    public String getCommand() {
        return "dumprarity";
    }

    @Override
    public int getPermissionLevel() {
        return 0;
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        float bonus = fetchFloat(sender, args, 1, 0.0f);

        DimletRandomizer.dumpRarityDistribution(bonus, sender.getEntityWorld());
    }
}
