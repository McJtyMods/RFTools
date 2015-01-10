package com.mcjty.rftools.commands;

import com.mcjty.rftools.RFTools;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class CmdDebugToggle extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public String getCommand() {
        return "toggle";
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if (!isAllowed(sender)) {
            return;
        }

        RFTools.debugMode = !RFTools.debugMode;
        if (RFTools.debugMode) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "RFTools Debug Mode enabled!"));
        } else {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "RFTools Debug Mode disabled!"));
        }
    }
}
