package mcjty.rftools.commands;

import mcjty.rftools.blocks.teleporter.TeleportationTools;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.TextComponentString;
import net.minecraft.util.TextFormatting;

public class CmdTeleport extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "<dimension> <x> <y> <z>";
    }

    @Override
    public String getCommand() {
        return "tp";
    }

    @Override
    public int getPermissionLevel() {
        return 2;
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if (args.length < 5) {
            sender.addChatMessage(new TextComponentString(TextFormatting.RED + "Several parameters are missing!"));
            return;
        } else if (args.length > 5) {
            sender.addChatMessage(new TextComponentString(TextFormatting.RED + "Too many parameters!"));
            return;
        }

        int dim = fetchInt(sender, args, 1, 0);
        int x = fetchInt(sender, args, 2, 0);
        int y = fetchInt(sender, args, 3, 100);
        int z = fetchInt(sender, args, 4, 0);

        if (sender instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) sender;

            int currentId = player.worldObj.provider.getDimensionId();
            if (currentId != dim) {
                TeleportationTools.teleportToDimension(player, dim, x, y, z);
            } else {
                player.setPositionAndUpdate(x, y, z);
            }
        }

    }
}
