package com.mcjty.rftools.commands;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class CmdTeleport implements RfToolsCommand {
    @Override
    public String getHelp() {
        return "<dimension> <x> <y> <z>";
    }

    @Override
    public String getCommand() {
        return "tp";
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if (args.length < 5) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Several parameters are missing!"));
            return;
        } else if (args.length > 5) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Too many parameters!"));
            return;
        }

        int dim = fetchInt(sender, args, 1);
        int x = fetchInt(sender, args, 2);
        int y = fetchInt(sender, args, 3);
        int z = fetchInt(sender, args, 4);

        if (sender instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) sender;

            int currentId = player.worldObj.provider.dimensionId;
            if (currentId != dim) {
                MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension((EntityPlayerMP) player, dim);
            }
            player.setPositionAndUpdate(x, y, z);
        }

    }

    private int fetchInt(ICommandSender sender, String[] args, int index) {
        int dim;
        try {
            dim = Integer.parseInt(args[index]);
        } catch (NumberFormatException e) {
            dim = 0;
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Dimension parameter is not a valid number!"));
        }
        return dim;
    }
}
