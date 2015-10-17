package mcjty.rftools.commands;

import mcjty.lib.gui.GuiStyle;
import mcjty.rftools.playerprops.PlayerExtendedProperties;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class CmdSetStyle extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "[<style>]";
    }

    @Override
    public String getCommand() {
        return "setstyle";
    }

    @Override
    public int getPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if (args.length > 2) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Too many parameters!"));
            return;
        }

        if (!(sender instanceof EntityPlayer)) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "This command only works as a player!"));
            return;
        }

        EntityPlayer player = (EntityPlayer) sender;
        PlayerExtendedProperties properties = PlayerExtendedProperties.getProperties(player);

        if (args.length < 2) {
            GuiStyle style = properties.getPreferencesProperties().getStyle();
            ((EntityPlayer) sender).addChatComponentMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "Current GUI style: " + style.getStyle()));
            return;
        }

        String s = fetchString(sender, args, 1, "");
        boolean b = properties.getPreferencesProperties().setStyle(s);
        if (!b) {
            String buf = "";
            for (GuiStyle style : GuiStyle.values()) {
                buf = buf + " " + style.getStyle();
            }

            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Unknown style! Options:" + buf));
        }
    }
}
