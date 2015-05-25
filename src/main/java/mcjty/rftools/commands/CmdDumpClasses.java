package mcjty.rftools.commands;

import mcjty.rftools.blocks.storage.ModularStorageConfiguration;
import net.minecraft.command.ICommandSender;

public class CmdDumpClasses extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public String getCommand() {
        return "dumpclasses";
    }

    @Override
    public int getPermissionLevel() {
        return 0;
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        ModularStorageConfiguration.dumpClasses();
    }
}
