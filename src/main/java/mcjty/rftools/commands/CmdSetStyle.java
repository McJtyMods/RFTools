package mcjty.rftools.commands;

import mcjty.lib.McJtyLib;
import mcjty.lib.gui.GuiStyle;
import mcjty.lib.preferences.PreferencesProperties;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

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
            sender.addChatMessage(new TextComponentString(TextFormatting.RED + "Too many parameters!"));
            return;
        }

        if (!(sender instanceof EntityPlayer)) {
            sender.addChatMessage(new TextComponentString(TextFormatting.RED + "This command only works as a player!"));
            return;
        }

        EntityPlayer player = (EntityPlayer) sender;
        PreferencesProperties properties = McJtyLib.getPreferencesProperties(player);

        if (args.length < 2) {
            GuiStyle style = properties.getStyle();
            ((EntityPlayer) sender).addChatComponentMessage(new TextComponentString(TextFormatting.YELLOW + "Current GUI style: " + style.getStyle()));
            return;
        }

        String s = fetchString(sender, args, 1, "");
        boolean b = properties.setStyle(s);
        if (!b) {
            String buf = "";
            for (GuiStyle style : GuiStyle.values()) {
                buf = buf + " " + style.getStyle();
            }

            sender.addChatMessage(new TextComponentString(TextFormatting.RED + "Unknown style! Options:" + buf));
        }
    }
}
