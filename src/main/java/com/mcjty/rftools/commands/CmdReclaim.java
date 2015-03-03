package com.mcjty.rftools.commands;

import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.DimensionManager;

public class CmdReclaim extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "<dimension>";
    }

    @Override
    public String getCommand() {
        return "reclaim";
    }

    @Override
    public int getPermissionLevel() {
        return 3;
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "The dimension parameters is missing!"));
            return;
        } else if (args.length > 2) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Too many parameters!"));
            return;
        }

        int dim = fetchInt(sender, args, 1, 0);
        EntityPlayer player = (EntityPlayer) sender;

        if (DimensionManager.isDimensionRegistered(dim)) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "This dimension is still in use! You can't reclaim the id!"));
            return;
        }

        RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(player.worldObj);
        dimensionManager.reclaimId(dim);
        dimensionManager.save(player.worldObj);

        sender.addChatMessage(new ChatComponentText("Dimension id " + dim + " reclaimed for future use."));
    }
}
