package mcjty.rftools.commands;

import mcjty.lib.varia.BlockPosTools;
import mcjty.rftools.blocks.teleporter.TeleportDestinationClientInfo;
import mcjty.rftools.blocks.teleporter.TeleportDestinations;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import java.util.Collection;

public class CmdListReceivers extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public String getCommand() {
        return "list";
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
        TeleportDestinations destinations = TeleportDestinations.getDestinations(sender.getEntityWorld());

        Collection<TeleportDestinationClientInfo> validDestinations = destinations.getValidDestinations(sender.getEntityWorld(), null);
        for (TeleportDestinationClientInfo clientInfo : validDestinations) {
            int id = clientInfo.getDimension();
            ITextComponent component = new TextComponentString("    Receiver: dimension=" + id + ", location=" + BlockPosTools.toString(clientInfo.getCoordinate()));
            if (sender instanceof EntityPlayer) {
                ((EntityPlayer) sender).sendStatusMessage(component, false);
            } else {
                sender.sendMessage(component);
            }
        }
    }
}
