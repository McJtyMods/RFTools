package mcjty.rftools.commands;

import mcjty.lib.tools.ChatTools;
import mcjty.lib.varia.Logging;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

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
    public int getPermissionLevel() {
        return 1;
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        Logging.debugMode = !Logging.debugMode;
        if (Logging.debugMode) {
            ChatTools.addChatMessage(sender, new TextComponentString(TextFormatting.YELLOW + "RFTools Debug Mode enabled!"));
        } else {
            ChatTools.addChatMessage(sender, new TextComponentString(TextFormatting.YELLOW + "RFTools Debug Mode disabled!"));
        }
    }
}
