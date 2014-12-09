package com.mcjty.rftools.commands;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public abstract class AbstractRfToolsCommand implements RfToolsCommand {
    protected int fetchInt(ICommandSender sender, String[] args, int index, int defaultValue) {
        int dim;
        try {
            dim = Integer.parseInt(args[index]);
        } catch (NumberFormatException e) {
            dim = 0;
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Dimension parameter is not a valid number!"));
        } catch (ArrayIndexOutOfBoundsException e) {
            return defaultValue;
        }
        return dim;
    }
}
