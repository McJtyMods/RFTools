package com.mcjty.rftools.commands;

import com.mcjty.rftools.blocks.teleporter.TeleportDestinations;
import net.minecraft.command.ICommandSender;
import net.minecraft.world.World;

public class CmdCleanupReceivers extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public String getCommand() {
        return "cleanup";
    }

    @Override
    public int getPermissionLevel() {
        return 1;
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        World world = sender.getEntityWorld();
        TeleportDestinations destinations = TeleportDestinations.getDestinations(world);
        destinations.cleanupInvalid(world);
        destinations.save(world);
    }
}
