package mcjty.rftools.commands;

import net.minecraft.command.ICommandSender;

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
        // @todo fix the 1.13 way?
        //        ScreenConfiguration.useTruetype.set(!ScreenConfiguration.useTruetype.get());
    }

    @Override
    public boolean isClientSide() {
        return true;
    }
}
