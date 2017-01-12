package mcjty.rftools.commands;

import mcjty.lib.tools.ChatTools;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CmdFont extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "<name>,<size>,<antialias>";
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
        if (args.length < 4) {
            ChatTools.addChatMessage(sender, new TextComponentString(TextFormatting.RED + "Several parameters are missing!"));
            return;
        } else if (args.length > 4) {
            ChatTools.addChatMessage(sender, new TextComponentString(TextFormatting.RED + "Too many parameters!"));
            return;
        }

        ScreenConfiguration.font = fetchString(sender, args, 1, "rftools:fonts/ubuntu.ttf");
        ScreenConfiguration.fontSize = fetchFloat(sender, args, 2, 40);
        ScreenConfiguration.fontAntialias = fetchBool(sender, args, 3, false);
    }

    @Override
    public boolean isClientSide() {
        return true;
    }
}
