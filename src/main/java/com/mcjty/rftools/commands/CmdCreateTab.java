package com.mcjty.rftools.commands;

import com.mcjty.container.InventoryHelper;
import com.mcjty.rftools.blocks.dimlets.DimensionEnscriberTileEntity;
import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import com.mcjty.rftools.dimension.description.DimensionDescriptor;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class CmdCreateTab extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "<dimension>";
    }

    @Override
    public String getCommand() {
        return "createtab";
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
        if (args.length < 2) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "The dimension parameter is missing!"));
            return;
        } else if (args.length > 2) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Too many parameters!"));
            return;
        }

        int dim = fetchInt(sender, args, 1, 0);
        World world = sender.getEntityWorld();

        RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(world);
        DimensionDescriptor dimensionDescriptor = dimensionManager.getDimensionDescriptor(dim);
        if (dimensionDescriptor == null) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Not an RFTools dimension!"));
            return;
        }

        if (sender instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) sender;
            ItemStack tab = DimensionEnscriberTileEntity.createRealizedTab(dimensionDescriptor, sender.getEntityWorld());
            InventoryHelper.mergeItemStack(player.inventory, tab, 0, 35, null);
        } else {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "This command only works as a player!"));
        }
    }
}
