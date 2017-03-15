package mcjty.rftools.commands;

import mcjty.rftools.blocks.teleporter.TeleportDestinations;
import net.minecraft.command.ICommandSender;

public class CmdCleanupReceivers extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public String getCommand() {
        return "cleanup";
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
        TeleportDestinations destinations = TeleportDestinations.getDestinations(sender.getEntityWorld());
        destinations.cleanupInvalid(sender.getEntityWorld());
    }
}
