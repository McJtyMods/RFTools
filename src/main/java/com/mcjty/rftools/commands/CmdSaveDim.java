package com.mcjty.rftools.commands;

import com.mcjty.rftools.dimension.DimensionInformation;
import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class CmdSaveDim extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "<dimension> <filename>";
    }

    @Override
    public String getCommand() {
        return "savedim";
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
        if (args.length < 3) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "The dimension and filename parameters are missing!"));
            return;
        } else if (args.length > 3) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Too many parameters!"));
            return;
        }

        int dim = fetchInt(sender, args, 1, 0);
        String filename = fetchString(sender, args, 2, null);

        EntityPlayer player = (EntityPlayer) sender;

        RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(player.worldObj);
        if (dimensionManager.getDimensionDescriptor(dim) == null) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Not an RFTools dimension!"));
            return;
        }

        DimensionInformation information = dimensionManager.getDimensionInformation(dim);
        String error = information.buildJson(filename);
        if (error != null) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Error: "+ error));
        }
    }
}
