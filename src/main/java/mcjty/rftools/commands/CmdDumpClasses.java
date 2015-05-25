package mcjty.rftools.commands;

import mcjty.rftools.blocks.storage.ModularStorageConfiguration;
import net.minecraft.command.ICommandSender;

public class CmdDumpClasses extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "[<code>]";
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
        boolean docode = fetchBool(sender, args, 1, false);
        ModularStorageConfiguration.dumpClasses(docode);
    }
}
