package mcjty.rftools.commands;

import mcjty.lib.font.FontLoader;
import mcjty.lib.font.TrueTypeFont;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.rftools.proxy.ClientProxy;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CmdFont extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "<name>,<size>";
    }

    @Override
    public String getCommand() {
        return "font";
    }

    @Override
    public int getPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if (args.length < 3) {
            ITextComponent component = new TextComponentString(TextFormatting.RED + "Several parameters are missing!");
            if (sender instanceof EntityPlayer) {
                ((EntityPlayer) sender).sendStatusMessage(component, false);
            } else {
                sender.sendMessage(component);
            }
            return;
        } else if (args.length > 3) {
            ITextComponent component = new TextComponentString(TextFormatting.RED + "Too many parameters!");
            if (sender instanceof EntityPlayer) {
                ((EntityPlayer) sender).sendStatusMessage(component, false);
            } else {
                sender.sendMessage(component);
            }
            return;
        }

        ScreenConfiguration.font = fetchString(sender, args, 1, "rftools:fonts/ubuntu.ttf");
        ScreenConfiguration.fontSize = fetchFloat(sender, args, 2, 40);
        TrueTypeFont font = FontLoader.createFont(new ResourceLocation(ScreenConfiguration.font), ScreenConfiguration.fontSize, false);
        if (font == null) {
            ITextComponent component = new TextComponentString(TextFormatting.RED + "Could not load font!");
            if (sender instanceof EntityPlayer) {
                ((EntityPlayer) sender).sendStatusMessage(component, false);
            } else {
                sender.sendMessage(component);
            }
            return;
        }
        ClientProxy.font = font;
    }

    @Override
    public boolean isClientSide() {
        return true;
    }
}
