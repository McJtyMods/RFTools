package com.mcjty.rftools.commands;

import com.mcjty.rftools.blocks.dimlets.DimletConfiguration;
import com.mcjty.rftools.dimension.DimensionInformation;
import com.mcjty.rftools.dimension.DimensionStorage;
import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class CmdSetPower extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "[<rf>]";
    }

    @Override
    public String getCommand() {
        return "setpower";
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
        if (args.length > 2) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Too many parameters!"));
            return;
        }

        int rf = fetchInt(sender, args, 1, DimletConfiguration.MAX_DIMENSION_POWER);

        if (sender instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) sender;

            int dim = player.worldObj.provider.dimensionId;
            RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(player.worldObj);
            DimensionInformation information = dimensionManager.getDimensionInformation(dim);
            if (information == null) {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Not an RFTools dimension!"));
                return;
            }

            DimensionStorage storage = DimensionStorage.getDimensionStorage(player.worldObj);
            storage.setEnergyLevel(dim, rf);
            storage.save(player.worldObj);
        }
    }
}
