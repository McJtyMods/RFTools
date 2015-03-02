package com.mcjty.rftools.commands;

import com.mcjty.rftools.blocks.teleporter.TeleportDestinationClientInfo;
import com.mcjty.rftools.blocks.teleporter.TeleportDestinations;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import java.util.Collection;

public class CmdListReceivers extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public String getCommand() {
        return "list";
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
        TeleportDestinations destinations = TeleportDestinations.getDestinations(sender.getEntityWorld());

        Collection<TeleportDestinationClientInfo> validDestinations = destinations.getValidDestinations(null);
        for (TeleportDestinationClientInfo clientInfo : validDestinations) {
            int id = clientInfo.getDimension();
            sender.addChatMessage(new ChatComponentText("    Receiver: dimension=" + id + ", location=" + clientInfo.getCoordinate()));
        }
    }
}
