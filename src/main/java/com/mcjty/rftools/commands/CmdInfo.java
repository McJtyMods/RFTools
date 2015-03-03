package com.mcjty.rftools.commands;

import com.mcjty.rftools.dimension.DimensionInformation;
import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class CmdInfo extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "[<dimension number>]";
    }

    @Override
    public String getCommand() {
        return "info";
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
        int dim = 0;

        if (sender instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) sender;

            if (args.length == 2) {
                dim = fetchInt(sender, args, 1, 0);
            } else if (args.length > 2) {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Too many parameters!"));
                return;
            } else {
                dim = player.worldObj.provider.dimensionId;
            }

            RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(player.worldObj);
            DimensionInformation information = dimensionManager.getDimensionInformation(dim);
            if (information == null) {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Not an RFTools dimension!"));
                return;
            }
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "Description string " + information.getDescriptor().getDescriptionString()));
            information.dump(player);
        }
    }
}
