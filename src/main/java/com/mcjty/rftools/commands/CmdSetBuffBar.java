package com.mcjty.rftools.commands;

import com.mcjty.rftools.Preferences;
import com.mcjty.rftools.blocks.teleporter.TeleportDestinations;
import com.mcjty.rftools.dimension.DimensionStorage;
import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class CmdSetBuffBar extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "<x> <y>";
    }

    @Override
    public String getCommand() {
        return "buffs";
    }

    @Override
    public int getPermissionLevel() {
        return 0;
    }

    @Override
    public boolean isClientSide() {
        return true;
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "The coordinate parameters are missing!"));
            return;
        } else if (args.length > 3) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Too many parameters!"));
            return;
        }

        int x = fetchInt(sender, args, 1, 0);
        int y = fetchInt(sender, args, 2, 0);
        Preferences.setBuffBarX(x);
        Preferences.setBuffBarY(y);
    }
}
