package com.mcjty.rftools.commands;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public abstract class AbstractRfToolsCommand implements RfToolsCommand {

    protected boolean isAllowed(ICommandSender sender) {
        if (MinecraftServer.getServer().getEntityWorld().getWorldInfo().areCommandsAllowed()) {
            return true;
        } else {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Command is not allowed!"));
            return false;
        }
    }

    protected int fetchInt(ICommandSender sender, String[] args, int index, int defaultValue) {
        int value;
        try {
            value = Integer.parseInt(args[index]);
        } catch (NumberFormatException e) {
            value = 0;
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Parameter is not a valid integer!"));
        } catch (ArrayIndexOutOfBoundsException e) {
            return defaultValue;
        }
        return value;
    }

    protected float fetchFloat(ICommandSender sender, String[] args, int index, float defaultValue) {
        float value;
        try {
            value = Float.parseFloat(args[index]);
        } catch (NumberFormatException e) {
            value = 0.0f;
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Parameter is not a valid real number!"));
        } catch (ArrayIndexOutOfBoundsException e) {
            return defaultValue;
        }
        return value;
    }
}
