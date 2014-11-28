package com.mcjty.rftools.commands;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

public class CmdListDimensions implements RfToolsCommand {
    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public String getCommand() {
        return "list";
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        WorldServer[] worlds = DimensionManager.getWorlds();
        for (WorldServer world : worlds) {
            int id = world.provider.dimensionId;
            String dimName = world.provider.getDimensionName();
            sender.addChatMessage(new ChatComponentText("    id:" + id + ", " + dimName));
        }
    }
}
