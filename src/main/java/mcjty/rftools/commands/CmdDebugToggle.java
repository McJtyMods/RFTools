package mcjty.rftools.commands;

import mcjty.varia.Logging;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

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
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "RFTools Debug Mode enabled!"));
        } else {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "RFTools Debug Mode disabled!"));
        }
    }
}
