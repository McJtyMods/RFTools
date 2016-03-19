package mcjty.rftools.commands;

import mcjty.lib.preferences.PlayerPreferencesProperties;
import mcjty.lib.preferences.PreferencesProperties;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CmdSetBuffBar extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "[<x> <y>]";
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
    public void execute(ICommandSender sender, String[] args) {
        if (args.length > 3) {
            sender.addChatMessage(new TextComponentString(TextFormatting.RED + "Too many parameters!"));
            return;
        }

        if (!(sender instanceof EntityPlayer)) {
            sender.addChatMessage(new TextComponentString(TextFormatting.RED + "This command only works as a player!"));
            return;
        }

        EntityPlayer player = (EntityPlayer) sender;
        PreferencesProperties properties = PlayerPreferencesProperties.getProperties(player);

        if (args.length < 3) {
            int buffX = properties.getBuffX();
            int buffY = properties.getBuffY();
            ((EntityPlayer) sender).addChatComponentMessage(new TextComponentString(TextFormatting.YELLOW + "Current buffbar location: " + buffX + "," + buffY));
            return;
        }

        int x = fetchInt(sender, args, 1, 0);
        int y = fetchInt(sender, args, 2, 0);
        properties.setBuffXY(x, y);
    }
}
