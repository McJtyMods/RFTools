package mcjty.rftools.commands;

import mcjty.lib.McJtyLib;
import mcjty.lib.gui.GuiStyle;
import mcjty.lib.preferences.PreferencesProperties;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
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
            ITextComponent component = new StringTextComponent(TextFormatting.RED + "Too many parameters!");
            if (sender instanceof PlayerEntity) {
                ((PlayerEntity) sender).sendStatusMessage(component, false);
            } else {
                sender.sendMessage(component);
            }
            return;
        }

        if (!(sender instanceof PlayerEntity)) {
            ITextComponent component = new StringTextComponent(TextFormatting.RED + "This command only works as a player!");
            if (sender instanceof PlayerEntity) {
                ((PlayerEntity) sender).sendStatusMessage(component, false);
            } else {
                sender.sendMessage(component);
            }
            return;
        }

        PlayerEntity player = (PlayerEntity) sender;
        PreferencesProperties properties = McJtyLib.getPreferencesProperties(player);

        if (args.length < 2) {
            GuiStyle style = properties.getStyle();
            ITextComponent component = new StringTextComponent(TextFormatting.YELLOW + "Current GUI style: " + style.getStyle());
            if (sender instanceof PlayerEntity) {
                ((PlayerEntity) sender).sendStatusMessage(component, false);
            } else {
                sender.sendMessage(component);
            }
            return;
        }

        String s = fetchString(sender, args, 1, "");
        boolean b = properties.setStyle(s);
        if (!b) {
            String buf = "";
            for (GuiStyle style : GuiStyle.values()) {
                buf = buf + " " + style.getStyle();
            }

            ITextComponent component = new StringTextComponent(TextFormatting.RED + "Unknown style! Options:" + buf);
            if (sender instanceof PlayerEntity) {
                ((PlayerEntity) sender).sendStatusMessage(component, false);
            } else {
                sender.sendMessage(component);
            }
        }
    }
}
