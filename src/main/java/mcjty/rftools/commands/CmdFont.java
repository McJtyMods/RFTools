package mcjty.rftools.commands;

import mcjty.lib.tools.ChatTools;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.rftools.font.FontLoader;
import mcjty.rftools.font.TrueTypeFont;
import mcjty.rftools.proxy.ClientProxy;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ResourceLocation;
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
            ChatTools.addChatMessage(sender, new TextComponentString(TextFormatting.RED + "Several parameters are missing!"));
            return;
        } else if (args.length > 3) {
            ChatTools.addChatMessage(sender, new TextComponentString(TextFormatting.RED + "Too many parameters!"));
            return;
        }

        ScreenConfiguration.font = fetchString(sender, args, 1, "rftools:fonts/ubuntu.ttf");
        ScreenConfiguration.fontSize = fetchFloat(sender, args, 2, 40);
        TrueTypeFont font = FontLoader.createFont(new ResourceLocation(ScreenConfiguration.font), ScreenConfiguration.fontSize, false);
        if (font == null) {
            ChatTools.addChatMessage(sender, new TextComponentString(TextFormatting.RED + "Could not load font!"));
            return;
        }
        ClientProxy.font = font;
    }

    @Override
    public boolean isClientSide() {
        return true;
    }
}
