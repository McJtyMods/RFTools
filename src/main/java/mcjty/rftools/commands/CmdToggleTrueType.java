package mcjty.rftools.commands;

import mcjty.lib.McJtyLib;
import mcjty.lib.preferences.PreferencesProperties;
import mcjty.lib.tools.ChatTools;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CmdToggleTrueType extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public String getCommand() {
        return "truetype";
    }

    @Override
    public int getPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        ScreenConfiguration.useTruetype = !ScreenConfiguration.useTruetype;
    }

    @Override
    public boolean isClientSide() {
        return true;
    }
}
